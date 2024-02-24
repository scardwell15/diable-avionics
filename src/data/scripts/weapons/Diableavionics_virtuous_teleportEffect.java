//by Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Diableavionics_virtuous_teleportEffect implements EveryFrameWeaponEffectPlugin {

    private ShipAPI ship;
    private ShipSystemAPI system;

    private final String ID = "diableavionics_flicker_boost";
    private final String section = "diableavionics";

    //FLICKER VALUES
    private final String trail = "BUBBLE_trail";
    private float currAngle = 0f;
    private final Map<Integer, String> bottomBubble = new HashMap<>();

    {
        bottomBubble.put(0, "BUBBLE_bottom00");
        bottomBubble.put(1, "BUBBLE_bottom01");
        bottomBubble.put(2, "BUBBLE_bottom02");
        bottomBubble.put(3, "BUBBLE_bottom03");
        bottomBubble.put(4, "BUBBLE_bottom04");
        bottomBubble.put(5, "BUBBLE_bottom05");
        bottomBubble.put(6, "BUBBLE_bottom06");
        bottomBubble.put(7, "BUBBLE_bottom07");
        bottomBubble.put(8, "BUBBLE_bottom08");
        bottomBubble.put(9, "BUBBLE_bottom09");
        bottomBubble.put(10, "BUBBLE_bottom10");
        bottomBubble.put(11, "BUBBLE_bottom11");
        bottomBubble.put(12, "BUBBLE_bottom12");
        bottomBubble.put(13, "BUBBLE_bottom13");
        bottomBubble.put(14, "BUBBLE_bottom14");
        bottomBubble.put(15, "BUBBLE_bottom15");
        bottomBubble.put(16, "BUBBLE_bottom16");
        bottomBubble.put(17, "BUBBLE_bottom17");
        bottomBubble.put(18, "BUBBLE_bottom18");
        bottomBubble.put(19, "BUBBLE_bottom19");
        bottomBubble.put(20, "BUBBLE_bottom20");
        bottomBubble.put(21, "BUBBLE_bottom21");
        bottomBubble.put(22, "BUBBLE_bottom22");
        bottomBubble.put(23, "BUBBLE_bottom23");
        bottomBubble.put(24, "BUBBLE_bottom24");
        bottomBubble.put(25, "BUBBLE_bottom25");
        bottomBubble.put(26, "BUBBLE_bottom26");
        bottomBubble.put(27, "BUBBLE_bottom27");
        bottomBubble.put(28, "BUBBLE_bottom28");
        bottomBubble.put(29, "BUBBLE_bottom29");
    }

    private final Map<Integer, String> topBubble = new HashMap<>();

    {
        topBubble.put(0, "BUBBLE_top00");
        topBubble.put(1, "BUBBLE_top01");
        topBubble.put(2, "BUBBLE_top02");
        topBubble.put(3, "BUBBLE_top03");
        topBubble.put(4, "BUBBLE_top04");
        topBubble.put(5, "BUBBLE_top05");
        topBubble.put(6, "BUBBLE_top06");
        topBubble.put(7, "BUBBLE_top07");
        topBubble.put(8, "BUBBLE_top08");
        topBubble.put(9, "BUBBLE_top09");
        topBubble.put(10, "BUBBLE_top10");
        topBubble.put(11, "BUBBLE_top11");
        topBubble.put(12, "BUBBLE_top12");
        topBubble.put(13, "BUBBLE_top13");
        topBubble.put(14, "BUBBLE_top14");
        topBubble.put(15, "BUBBLE_top15");
        topBubble.put(16, "BUBBLE_top16");
        topBubble.put(17, "BUBBLE_top17");
        topBubble.put(18, "BUBBLE_top18");
        topBubble.put(19, "BUBBLE_top19");
        topBubble.put(20, "BUBBLE_top20");
        topBubble.put(21, "BUBBLE_top21");
        topBubble.put(22, "BUBBLE_top22");
        topBubble.put(23, "BUBBLE_top23");
        topBubble.put(24, "BUBBLE_top24");
        topBubble.put(25, "BUBBLE_top25");
        topBubble.put(26, "BUBBLE_top26");
        topBubble.put(27, "BUBBLE_top27");
        topBubble.put(28, "BUBBLE_top28");
        topBubble.put(29, "BUBBLE_top29");
    }

    private Vector2f from = new Vector2f();
    private boolean runOnce = false;
    private float boost = 0f;
    private float boostAcceleration = 1f;
    private float boostMax = 100f;
    private float boostAccelerationMax = 20f;


    private float getActiveBoostRatio() {
        return 0.5f + boost / boostMax;
    }
    private float getDamageRange() {
        return 768f * getActiveBoostRatio();
    }

    private float getDamage() {
        return 250f * getActiveBoostRatio();
    }

    private List<CombatEntityAPI> findTargets() {
        float damageRange = getDamageRange();
        Vector2f shipPos = ship.getLocation();
        return CombatUtils.getEntitiesWithinRange(shipPos, damageRange);
    }

    private Vector2f getFlickerDest() {
        if (ship.getVelocity().lengthSquared() <= 0) {
            return ship.getLocation();
        }
        return Vector2f.add(ship.getLocation(), (Vector2f) ship.getVelocity().normalise(null).scale(ship.getSystem().getSpecAPI().getRange(ship.getMutableStats())), null);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //initialise the variables
        if ((!runOnce || ship == null || system == null) && !Global.getCombatEngine().isPaused()) {
            runOnce = true;
            ship = weapon.getShip();
            system = ship.getSystem();
        }

        if (ship == null) return;

        MagicUI.drawInterfaceStatusBar(ship, ID, Math.min(1f, boost / boostMax), null, null, 0f, "BOOST", (int) boost);

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        if (system.isActive()) {
            flickerEffect();
        } else {
            boostAcceleration += Math.min(amount, boostAccelerationMax);
            boost = Math.min(boost + amount * boostAcceleration, boostMax);
        }

        currAngle += amount * 3f;

        Vector2f teleportPos = getFlickerDest();
        MagicRender.singleframe(
                Global.getSettings().getSprite(section, "RING"),
                teleportPos,
                new Vector2f(256, 256),
                VectorUtils.getFacing(ship.getVelocity()) + currAngle,
                new Color(0.25f, 0.5f, 1f, 0.5f),
                true,
                CombatEngineLayers.ABOVE_PARTICLES
        );


        List<CombatEntityAPI> targets = findTargets();
        for (CombatEntityAPI target : targets) {
            if (target.getOwner() == ship.getOwner()) continue;
            if (target instanceof ShipAPI && (!((ShipAPI) target).isAlive() || ((ShipAPI) target).isHulk())) continue;
            if (target instanceof MissileAPI && ((MissileAPI) target).getDamageAmount() < 100f) continue;
            if (target instanceof DamagingProjectileAPI && ((DamagingProjectileAPI) target).getDamageAmount() < 100f) continue;

            MagicRender.singleframe(
                    Global.getSettings().getSprite(section, "DIAMOND"),
                    target.getLocation(),
                    new Vector2f(63, 63),
                    45,
                    new Color(0.1f, 0.1f, 0.1f, 0.75f),
                    true,
                    CombatEngineLayers.ABOVE_PARTICLES
            );

            MagicRender.singleframe(
                    Global.getSettings().getSprite(section, "DIAMOND"),
                    target.getLocation(),
                    new Vector2f(64, 64),
                    45,
                    new Color(0.75f, 0.25f, 1f, 0.75f),
                    false,
                    CombatEngineLayers.ABOVE_PARTICLES
            );
        }
    }

    private void flickerEffect() {
        float level = system.getEffectLevel();

        //bubble
        if (MagicRender.screenCheck(1, ship.getLocation())) {

            //extract animation frame from system level
            float anim;
            if (system.isChargeup()) {
                anim = level * 14.5f;
                from = new Vector2f(ship.getLocation());
            } else if (system.isChargedown()) {
                anim = (1 - level) * 14.5f + 14.5f;
            } else {
                anim = 14.5f;
            }

            //bubble top
            MagicRender.singleframe(
                    Global.getSettings().getSprite(section, topBubble.get((int) (anim))),
                    ship.getLocation(),
                    new Vector2f(256, 256),
                    VectorUtils.getFacing(ship.getVelocity()),
                    new Color(1f, 1f, 1f, 0.5f + 0.25f * level),
                    true,
                    CombatEngineLayers.ABOVE_SHIPS_LAYER
            );

            //bubble bottom
            MagicRender.singleframe(
                    Global.getSettings().getSprite(section, bottomBubble.get((int) (anim))),
                    ship.getLocation(),
                    new Vector2f(256, 256),
                    VectorUtils.getFacing(ship.getVelocity()),
                    new Color(1f, 1f, 1f, 1 - 0.5f * level),
                    false,
                    CombatEngineLayers.BELOW_SHIPS_LAYER
            );

            //trail on  displacement
            if (level == 1) {
                MagicRender.battlespace(
                        Global.getSettings().getSprite(section, trail),
                        MathUtils.getMidpoint(ship.getLocation(), from),
                        new Vector2f(),
                        new Vector2f(512, 256),
                        new Vector2f(-128, 0),
                        VectorUtils.getAngle(from, ship.getLocation()),
                        0,
                        Color.white,
                        true,
                        0, 0, 0, 0, 0,
                        0,
                        0.05f,
                        0.1f,
                        CombatEngineLayers.BELOW_SHIPS_LAYER
                );

                spawnExplosion(ship);
            }
        }
    }

    private void spawnExplosion(ShipAPI ship) {
        float activeBoostRatio = getActiveBoostRatio();
        float damage = getDamage();
        float damageRange = getDamageRange();
        Vector2f shipPos = ship.getLocation();

        int banishedProjectiles = 0;
        List<CombatEntityAPI> entities = findTargets();
        for (CombatEntityAPI target : entities) {
            if (target.getOwner() == ship.getOwner()) continue;

            float arcFacing = VectorUtils.getAngle(shipPos, target.getLocation());
            Vector2f arcPos = MathUtils.getPointOnCircumference(shipPos, ship.getShield().getRadius(), arcFacing + MathUtils.getRandomNumberInRange(-15, 15));

            //MissileAPI is an instance of DamagingProjectileAPI, but we still want to do damage to it
            if (target instanceof MissileAPI || !(target instanceof DamagingProjectileAPI)) {
                EmpArcEntityAPI banishedArc = null;
                for (int i = 0; i < MathUtils.getRandomNumberInRange(2, 5); i++) {
                    EmpArcEntityAPI arc = Global.getCombatEngine().spawnEmpArc(
                            ship,
                            arcPos,
                            ship,
                            target,
                            DamageType.ENERGY,
                            damage,
                            damage,
                            damageRange,
                            null,
                            MathUtils.getRandomNumberInRange(12, 16),
                            new Color(255, 20, 255, 255),
                            new Color(80, 10, 200, 255));

                    EmpArcEntityAPI arc2 = Global.getCombatEngine().spawnEmpArcPierceShields(
                            ship,
                            arcPos,
                            ship,
                            target,
                            DamageType.ENERGY,
                            0f,
                            damage / 5f,
                            damageRange,
                            null,
                            MathUtils.getRandomNumberInRange(4, 8),
                            new Color(20, 200, 255, 156),
                            new Color(100, 255, 255, 255));

                    if (banishedArc == null || MathUtils.getRandomNumberInRange(1, 2) == 1) {
                        if (MathUtils.getRandomNumberInRange(1, 2) == 1) {
                            banishedArc = arc;
                        } else {
                            banishedArc = arc2;
                        }
                    }
                }

                if (target instanceof ShipAPI && ((ShipAPI) target).isAlive() && !((ShipAPI) target).isHulk()) {
                    if (((ShipAPI) target).isFighter()) {
                        boost -= 5f;
                    } else {
                        boost -= 20f;
                    }
                    banish(target, banishedArc != null ? banishedArc.getTargetLocation() : target.getLocation());
                } else {
                    if (target instanceof MissileAPI) {
                        boost -= Math.min(((MissileAPI) target).getDamageAmount() / 500f, 5f);
                    } else {
                        boost -= 3f;
                    }
                    if (MathUtils.getRandomNumberInRange(1, banishedProjectiles + 1) == 1) {
                        banishedProjectiles++;
                        banish(target, target.getLocation());
                    }
                }
            } else {
                if (MathUtils.getRandomNumberInRange(0f, activeBoostRatio) > 0.25f) {
                    if (MathUtils.getRandomNumberInRange(1, banishedProjectiles + 5) == 1) {
                        banishedProjectiles++;

                        Global.getCombatEngine().spawnEmpArcVisual(
                                arcPos,
                                ship,
                                target.getLocation(),
                                null,
                                MathUtils.getRandomNumberInRange(12, 16),
                                new Color(255, 20, 255, 255),
                                new Color(80, 10, 200, 255));

                        banish(target, target.getLocation());
                    }

                    boost -= Math.min(((DamagingProjectileAPI) target).getDamageAmount() / 500f, 5f);
                    Global.getCombatEngine().removeEntity(target);
                }
            }
        }

        for (int i = 0; i < MathUtils.getRandomNumberInRange(10 * activeBoostRatio, 20 * activeBoostRatio); i++) {
            Vector2f targetPos = MathUtils.getPointOnCircumference(shipPos, damageRange * MathUtils.getRandomNumberInRange(0.5f, 1f), MathUtils.getRandomNumberInRange(15f, 60f) * i);
            Global.getCombatEngine().spawnEmpArcVisual(
                    shipPos,
                    ship,
                    targetPos,
                    null,
                    MathUtils.getRandomNumberInRange(2, 4),
                    new Color(150, 20, 200, 78),
                    new Color(50, 10, 150, 160));
        }

        boost = Math.max(0f, boost);
        boostAcceleration *= Math.min(activeBoostRatio, 0.5f);
    }

    private void banish(CombatEntityAPI target, Vector2f point) {
        if(MagicRender.screenCheck(0.25f, point)){
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","banishSharp"),
                    new Vector2f(point),
                    new Vector2f(),
                    new Vector2f(128,128),
                    new Vector2f(-384,-384),
                    MathUtils.getRandomNumberInRange(0, 360),
                    MathUtils.getRandomNumberInRange(-1, 1),
                    Color.PINK,
                    false,
                    0,0,0,0,0,
                    0,
                    0.1f,
                    0.2f,
                    CombatEngineLayers.JUST_BELOW_WIDGETS
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","banishDiffuse"),
                    new Vector2f(point),
                    new Vector2f(),
                    new Vector2f(194,194),
                    new Vector2f(-196,-196),
                    MathUtils.getRandomNumberInRange(0, 360),
                    MathUtils.getRandomNumberInRange(-1, 1),
                    Color.BLUE,
                    false,
                    0,0,0,0,0,
                    0f,
                    0.15f,
                    0.45f,
                    CombatEngineLayers.JUST_BELOW_WIDGETS
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","banishDiffuse"),
                    new Vector2f(point),
                    new Vector2f(),
                    new Vector2f(156,156),
                    new Vector2f(-128,-128),
                    MathUtils.getRandomNumberInRange(0, 360),
                    MathUtils.getRandomNumberInRange(-1, 1),
                    Color.WHITE,
                    false,
                    0,0,0,0,0,
                    0.2f,
                    0.05f,
                    0.35f,
                    CombatEngineLayers.JUST_BELOW_WIDGETS
            );

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","banishFlash"),
                    new Vector2f(point),
                    new Vector2f(),
                    new Vector2f(128,128),
                    new Vector2f(),
                    MathUtils.getRandomNumberInRange(0, 360),
                    MathUtils.getRandomNumberInRange(-1, 1),
                    Color.WHITE,
                    true,
                    0,0,0,0,0,
                    0,
                    0.05f,
                    0.05f,
                    CombatEngineLayers.JUST_BELOW_WIDGETS
            );

            for(int i=0; i<MathUtils.getRandomNumberInRange(4, 8); i++){

                int size = MathUtils.getRandomNumberInRange(16, 54);
                float fade = MathUtils.getRandomNumberInRange(0.15f, 0.5f);
                CombatEngineLayers layer = CombatEngineLayers.JUST_BELOW_WIDGETS;
                if(Math.random()<fade){
                    layer = CombatEngineLayers.BELOW_INDICATORS_LAYER;
                }

                MagicRender.battlespace(
                        Global.getSettings().getSprite("fx","banishSharp"),
                        MathUtils.getRandomPointOnCircumference(
                                point,
                                MathUtils.getRandomNumberInRange(32, 128-size)
                        ),
                        new Vector2f(),
                        new Vector2f(size,size),
                        new Vector2f(-size/fade,-size/fade),
                        MathUtils.getRandomNumberInRange(0, 360),
                        MathUtils.getRandomNumberInRange(-1, 1),
                        new Color(128,24,200,255),
                        false,
                        0,0,0,0,0,
                        0,
                        3*fade/4,
                        fade/4,
                        layer
                );
            }
        }

        Global.getSoundPlayer().playSound(
                "diableavionics_banish_blast",
                1f,
                1,
                point,
                target.getLocation()
        );
    }
}
