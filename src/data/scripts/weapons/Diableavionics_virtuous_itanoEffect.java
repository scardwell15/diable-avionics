//by Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.plugins.DiableAvionics_itanoCircusDrone;
import org.lazywizard.lazylib.VectorUtils;
import org.magiclib.util.MagicRender;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_virtuous_itanoEffect implements EveryFrameWeaponEffectPlugin {

    private WeaponAPI lpauldronglow;
    private WeaponAPI rpauldronglow;
    private ShipAPI ship;
    private ShipSystemAPI system;

    private final String lpauldronglowID = "PAULDRON_LG";
    private final String rpauldronglowID = "PAULDRON_RG";

    private final String lsystemID = "SYSTEM_L";
    private final String rsystemID = "SYSTEM_R";
    private WeaponAPI lsystem;
    private WeaponAPI rsystem;
    private float tick = 0;

    private boolean runOnce = false, activated = false, playerShip = false;

    private static final List<ShipAPI> TARGETS = new ArrayList<>();
    private int select = -1;
    private final int MAX_RANGE = 1500;
    private float beep = 0;

    //pass a target to the missile looking for it
    public ShipAPI getTarget() {
        if (TARGETS == null || TARGETS.isEmpty()) {
            return null;
        }

        //select all targets in order (that's why it starts at -1)
        select++;

        if (select >= TARGETS.size()) {
            //if all nearby targets are getting a missile already then shuffle and start again
            Collections.shuffle(TARGETS);
            select = 0;
        }

        return TARGETS.get(select);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        //make sure it doesn't leak!
        if (!weapon.getShip().isAlive() || engine.isCombatOver()) {
            TARGETS.clear();
        }

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        //initialise the variables
        if (!runOnce || ship == null || system == null) {
            ship = weapon.getShip();
            if (ship == engine.getPlayerShip()) {
                playerShip = true;
            }
            system = ship.getSystem();
            List<WeaponAPI> weapons = ship.getAllWeapons();
            for (WeaponAPI w : weapons) {
                switch (w.getSlot().getId()) {
                    case lpauldronglowID:
                        lpauldronglow = w;
                        break;
                    case rpauldronglowID:
                        rpauldronglow = w;
                        break;
                    case lsystemID:
                        lsystem = w;
                        break;
                    case rsystemID:
                        rsystem = w;
                        break;
                }
            }
            runOnce = true;
            //return to avoid a null error on the ship
            return;
        }

        if (system.getEffectLevel() > 0) {

            if (!activated) {
                //add a targeting ring
                MagicRender.objectspace(
                        Global.getSettings().getSprite("diableavionics", "RING"),
                        ship, //anchor
                        new Vector2f(), //offset
                        new Vector2f(), //velocity
                        new Vector2f(64, 64), //size
                        new Vector2f(2000, 2000), //growth
                        MathUtils.getRandomNumberInRange(-180, 180), //angle
                        0, //spin
                        false, //parented
                        new Color(0, 200, 255, 128),
                        true, //additive
                        0, 0, //jitter
                        0.5f, 1f, 0.2f, //flicker
                        0.05f, 0.45f, 1f, //timing
                        true,
                        CombatEngineLayers.UNDER_SHIPS_LAYER
                );
            }

            activated = true;
            effectOn(engine, amount);

            if (system.isChargeup() && system.getEffectLevel() < 1) {
                //targeting diamond effect
                pickTargets(engine, system.getEffectLevel(), ship, amount);

            }

        } else if (activated && system.getEffectLevel() == 0) {
            activated = false;
            //reset target list
            TARGETS.clear();
        }
    }

    private void pickTargets(CombatEngineAPI engine, Float level, ShipAPI ship, float amount) {
        beep -= amount;
        //adding ships
        List<ShipAPI> nearby = AIUtils.getNearbyEnemies(ship, MAX_RANGE * level);
        for (ShipAPI s : nearby) {
            if (!TARGETS.contains(s)) {
                TARGETS.add(s);

                Global.getCombatEngine().addPlugin(new DiableAvionics_itanoCircusDrone(ship, s));

                //sound
                if (playerShip && beep <= 0) {
                    beep = 0.075f;
                    Global.getSoundPlayer().playSound("diableavionics_virtuousTarget_beep", 1, 1, ship.getLocation(), ship.getVelocity());
                }
                if (engine.isUIShowingHUD()) {
                    //add a targeting diamond
                    MagicRender.objectspace(
                            Global.getSettings().getSprite("diableavionics", "DIAMOND"),
                            s, //anchor
                            new Vector2f(), //offset
                            new Vector2f(), //velocity
                            new Vector2f(64, 64), //size
                            new Vector2f(0, 0), //growth
                            45, //angle
                            0, //spin
                            false, //parented
                            Color.orange,
                            false, //additive
                            0, 0, //jitter
                            2, 1, 0.2f, //flicker
                            0.5f, 4 - system.getChargeUpDur() * level, 0.5f, //timing
                            true,
                            CombatEngineLayers.BELOW_INDICATORS_LAYER
                    );
                    //exclude the swirly one if it is too far off screen
                    if (MagicRender.screenCheck(0.2f, s.getLocation())) {
                        MagicRender.objectspace(
                                Global.getSettings().getSprite("diableavionics", "DIAMOND"),
                                s, //anchor
                                new Vector2f(), //offset
                                new Vector2f(), //velocity
                                new Vector2f(192, 192), //size
                                new Vector2f(-256, -256), //growth
                                45, //angle
                                360, //spin
                                false, //parented
                                Color.orange,
                                false, //additive
                                0, 0, //jitter
                                0, 0, 0, //flicker
                                0.35f, 0.05f, 0.1f, //timing
                                true,
                                CombatEngineLayers.BELOW_INDICATORS_LAYER
                        );
                    }
                }
            }
        }
    }

    private void effectOn(CombatEngineAPI engine, float amount) {
        float level = system.getEffectLevel();

        lsystem.setCurrAngle(lpauldronglow.getCurrAngle() - 45);
        rsystem.setCurrAngle(rpauldronglow.getCurrAngle() + 45);

        tick += amount;
        if (tick > 0.1f) {
            tick -= 0.1;
            int grey = (int) (255 * Math.random());
            engine.addNebulaParticle(
                    MathUtils.getRandomPointInCircle(lsystem.getLocation(), 20),
                    MathUtils.getRandomPointInCone(lsystem.getShip().getVelocity(), 5 + 10 * level, lsystem.getCurrAngle() - 90, lsystem.getCurrAngle() + 90),
                    MathUtils.getRandomNumberInRange(20, 40),
                    MathUtils.getRandomNumberInRange(1.5f, 2.5f),
                    0.1f,
                    0.1f,
                    MathUtils.getRandomNumberInRange(0.5f + 0.5f * level, 1f + 3f * level),
                    new Color(grey, grey, grey, MathUtils.getRandomNumberInRange(32, 196)),
                    true
            );

            engine.addNebulaParticle(
                    MathUtils.getRandomPointInCircle(rsystem.getLocation(), 20),
                    MathUtils.getRandomPointInCone(rsystem.getShip().getVelocity(), 5 + 10 * level, rsystem.getCurrAngle() - 90, rsystem.getCurrAngle() + 90),
                    MathUtils.getRandomNumberInRange(20, 40),
                    MathUtils.getRandomNumberInRange(1.5f, 2.5f),
                    0.1f,
                    0.1f,
                    MathUtils.getRandomNumberInRange(0.5f + 0.5f * level, 1f + 3f * level),
                    new Color(grey, grey, grey, MathUtils.getRandomNumberInRange(32, 196)),
                    true
            );
        }
    }
}
