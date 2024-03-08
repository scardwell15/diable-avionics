package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicAutoTrails;
import org.magiclib.plugins.MagicTrailPlugin;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DiableAvionicsUpgrade extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponDamageMult().modifyPercent(id, 30);
        stats.getEnergyWeaponDamageMult().modifyPercent(id, 30);
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, 30);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, 30);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new AvionicsTracker(ship));
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "30%";
        }
        if (index == 1) {
            return "30%";
        }
        if (index == 2) {
            return "5%";
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getHullSpec().getHullId().startsWith("diableavionics_")
                && !ship.getVariant().hasHullMod(DampenedMounts.MODULAR_MOUNT_ID)
                && !ship.getVariant().getPermaMods().contains(DampenedMounts.MODULAR_MOUNT_ID)
                && !ship.getVariant().getSMods().contains(DampenedMounts.MODULAR_MOUNT_ID);
    }

    private static final Color ENGINE_COLOR = new Color(190, 220, 255);
    private static final Color CONTRAIL_COLOR = new Color(255, 100, 100);
    public class AvionicsTracker implements AdvanceableListener {
        private IntervalUtil effectInterval = new IntervalUtil(0.05f, 0.05f);
        private Map<ShipEngineControllerAPI.ShipEngineAPI, Float> trailIDMap = new HashMap<>();
        private SpriteAPI trailSprite = Global.getSettings().getSprite("fx", "beamRough2Core");

        private final ShipAPI ship;
        private final IntervalUtil interval = new IntervalUtil(1f, 1f);
        private float lastFluxCap = 0f;
        private float engineEffectLevel = 0f;
        private boolean avionicsBoost = false;

        public AvionicsTracker(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount) {
            interval.advance(amount);
            if (interval.intervalElapsed()) {
                interval.setInterval(1f, 1f);

                float newFlux = ship.getCurrFlux() / ship.getMaxFlux();
                if (avionicsBoost) {
                    if (newFlux - lastFluxCap >= 0.05f) {
                        interval.setInterval(5f, 5f);

                        avionicsBoost = false;
                        ship.getMutableStats().getZeroFluxMinimumFluxLevel().unmodify("diableavionics_advancedAvionics");
                    }
                } else {
                    if (newFlux - lastFluxCap < 0.05f) {
                        avionicsBoost = true;
                        // set to two, meaning boost is always on
                        ship.getMutableStats().getZeroFluxMinimumFluxLevel().modifyFlat("diableavionics_advancedAvionics", 2f);
                    }
                }

                lastFluxCap = newFlux;
            }

            if (avionicsBoost) {
                engineEffectLevel += amount;

                effectInterval.advance(amount);
                if (effectInterval.intervalElapsed()) {
                    float angle = Misc.getAngleInDegrees(new Vector2f(ship.getVelocity()));
                    float opacity = ship.getVelocity().length() / ship.getMaxSpeed();
                    float startSize = 4f;
                    float endSize = 4f;
                    float duration = 2f;
                    float opacityMult = 1f;

                    for (ShipEngineControllerAPI.ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
                        if (!trailIDMap.containsKey(engine)) {
                            trailIDMap.put(engine,  MagicTrailPlugin.getUniqueID());
                        }

                        MagicTrailPlugin.addTrailMemberSimple(
                                ship,
                                trailIDMap.get(engine),
                                trailSprite,
                                engine.getLocation(),
                                0f,
                                angle,
                                startSize,
                                endSize,
                                Color.RED,
                                opacity * opacityMult,
                                0f,
                                0f,
                                duration,
                                true
                        );
                    }
                }
            } else {
                engineEffectLevel -= amount;
            }

            engineEffectLevel = MathUtils.clamp(engineEffectLevel, 0f, 1f);
            ship.getEngineController().fadeToOtherColor(this, ENGINE_COLOR, new Color(0, 0, 0, 0), engineEffectLevel, 0.33f);
        }
    }

    public DiableAvionicsUpgrade() {
    }
}
