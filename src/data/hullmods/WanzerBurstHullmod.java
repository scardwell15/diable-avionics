package data.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.combat.RealityDisruptorChargeGlow;
import org.lazywizard.lazylib.MathUtils;
import org.magiclib.subsystems.MagicSubsystem;
import org.magiclib.subsystems.MagicSubsystemsManager;

import java.awt.*;
import java.util.List;

import static data.scripts.util.Diableavionics_stringsManager.txt;

public class WanzerBurstHullmod extends BaseHullMod {
    public static float REPAIR_RATE_MULT = 0.5f;
    public static float REPAIR_RATE_DEBUFF_DUR = 10f;
    public static Object STATUS_KEY = new Object();

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        //MagicSubsystemsManager.addSubsystemToShip(ship, new WanzerBurstSubsystem(ship));
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }

    private class WanzerBurstSubsystem extends MagicSubsystem {
        public WanzerBurstSubsystem(ShipAPI ship) {
            super(ship);
        }

        @Override
        public float getBaseActiveDuration() {
            return 0;
        }

        @Override
        public float getBaseCooldownDuration() {
            return 30;
        }

        @Override
        public boolean shouldActivateAI(float amount) {
            return canActivate();
        }

        @Override
        public String getDisplayText() {
            return txt("wanzerBurstSubsystemName");
        }

        @Override
        public boolean canActivate() {
            ShipAPI shipTarget = null;

            for (FighterWingAPI wing : ship.getAllWings()) {
                for (ShipAPI fighter : wing.getWingMembers()) {
                    if (fighter.getShipTarget() == null || fighter.getShipTarget().isFighter() || fighter.getShipTarget().isHulk()) continue;
                    if (MathUtils.getDistance(fighter,  fighter.getShipTarget()) > 750f) continue;
                    shipTarget = fighter.getShipTarget();
                    break;
               }
            }

            return shipTarget != null;
        }

        @Override
        public void onActivate() {
            for (FighterWingAPI wing : ship.getAllWings()) {
                for (ShipAPI fighter : wing.getWingMembers()) {
                    ShipAPI target = fighter.getShipTarget();
                    if (target == null) continue;
                    Global.getCombatEngine().spawnEmpArc(ship, fighter.getLocation(), fighter, target, DamageType.FRAGMENTATION, 0f, 250f, 750f, "realitydisruptor_emp_impact", 20f, Color.RED, Color.WHITE);
                    List<RealityDisruptorChargeGlow.RDRepairRateDebuff> listeners = target.getListeners(RealityDisruptorChargeGlow.RDRepairRateDebuff.class);
                    if (listeners.isEmpty()) {
                        target.addListener(new RDRepairRateDebuff(target));
                    } else {
                        listeners.get(0).resetDur();
                    }
                }
            }
        }
    }

    public static class RDRepairRateDebuff implements AdvanceableListener {
        public static String DEBUFF_ID = "reality_disruptor_repair_debuff";

        public ShipAPI ship;
        public float dur = REPAIR_RATE_DEBUFF_DUR;
        public RDRepairRateDebuff(ShipAPI ship) {
            this.ship = ship;

            ship.getMutableStats().getCombatEngineRepairTimeMult().modifyMult(DEBUFF_ID, 1f/REPAIR_RATE_MULT);
            ship.getMutableStats().getCombatWeaponRepairTimeMult().modifyMult(DEBUFF_ID, 1f/REPAIR_RATE_MULT);
        }

        public void resetDur() {
            dur = REPAIR_RATE_DEBUFF_DUR;
        }

        public void advance(float amount) {
            dur -= amount;

            if (Global.getCurrentState() == GameState.COMBAT &&
                    Global.getCombatEngine() != null && Global.getCombatEngine().getPlayerShip() == ship) {
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY,
                        Global.getSettings().getSpriteName("ui", "icon_tactical_reality_disruptor"),
                        "WANZER DISRUPTION", "SLOWER REPAIRS: " + Math.max(1, Math.round(dur)) + " SEC", true);
            }

            if (dur <= 0) {
                ship.removeListener(this);
                ship.getMutableStats().getCombatEngineRepairTimeMult().unmodify(DEBUFF_ID);
                ship.getMutableStats().getCombatWeaponRepairTimeMult().unmodify(DEBUFF_ID);
            }
        }
    }
}
