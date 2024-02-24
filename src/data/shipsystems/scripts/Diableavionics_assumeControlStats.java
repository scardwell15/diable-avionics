package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.MagicSubsystem;
import org.magiclib.subsystems.MagicSubsystemsManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_assumeControlStats extends BaseShipSystemScript {
    private static final Color JITTER_UNDER_COLOR = new Color(150, 100, 50, 50);
    private static final float MAX_TIME_MULT = 1.25f;
    public static final String SYSTEM_ID = "diableavionics_assumeControl";

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) continue;

            //accelerate fighters
            float mult = stats.getSystemRangeBonus().getBonusMult();
            float shipTimeMult = 1f + (MAX_TIME_MULT + mult - 2f) * effectLevel;
            fighter.getMutableStats().getTimeMult().modifyMult(SYSTEM_ID, shipTimeMult);
            fighter.getMutableStats().getHullDamageTakenMult().modifyMult(SYSTEM_ID, 0.66f);
            fighter.getMutableStats().getArmorDamageTakenMult().modifyMult(SYSTEM_ID, 0.66f);
            fighter.getMutableStats().getShieldDamageTakenMult().modifyMult(SYSTEM_ID, 0.66f);

            //visual effect
            fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 50), EnumSet.allOf(WeaponType.class));
            fighter.setJitterUnder(fighter, JITTER_UNDER_COLOR, effectLevel, (int) (5 * effectLevel), 3, 3 + 3 * effectLevel);
        }

        if (!hasControlSubsystem(ship)) {
            MagicSubsystemsManager.addSubsystemToShip(ship, new ControlFighterSwitch(ship, ship));
        }
    }

    private static boolean hasControlSubsystem(ShipAPI ship) {
        List<MagicSubsystem> subsystems = MagicSubsystemsManager.getSubsystemsForShipCopy(ship);
        if (subsystems != null) {
            for (MagicSubsystem subsystem : subsystems) {
                if (subsystem instanceof ControlCarrierSwitch || subsystem instanceof ControlFighterSwitch) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();
        if (!carrier.getAllWings().isEmpty()) {
            for (FighterWingAPI w : carrier.getAllWings()) {
                result.addAll(w.getWingMembers());
            }
        }
        return result;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;

        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) continue;
            fighter.setWeaponGlow(0, Color.BLACK, EnumSet.allOf(WeaponType.class));
            fighter.getMutableStats().getTimeMult().unmodify(id);
            fighter.getMutableStats().getHullDamageTakenMult().unmodify(id);
            fighter.getMutableStats().getArmorDamageTakenMult().unmodify(id);
            fighter.getMutableStats().getShieldDamageTakenMult().unmodify(id);

            MagicSubsystemsManager.removeSubsystemFromShip(fighter, ControlCarrierSwitch.class);
            MagicSubsystemsManager.removeSubsystemFromShip(fighter, ControlFighterSwitch.class);

            if (Global.getCombatEngine().getPlayerShip() == fighter) {
                switchToCarrier(ship, fighter);
            }
        }

        if (hasControlSubsystem(ship)) {
            MagicSubsystemsManager.removeSubsystemFromShip(ship, ControlFighterSwitch.class);
        }

        List<MagicSubsystem> subsystems = MagicSubsystemsManager.getSubsystemsForShipCopy(ship);
        if (subsystems != null && subsystems.isEmpty()) {
            ship.getCustomData().remove(MagicSubsystemsManager.CUSTOM_DATA_KEY);
        }
    }

    private final String TXT = txt("assumeControl");

    @Override
    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData(TXT, false);
        }
        return null;
    }

    private final String READY = txt("ready");
    private final String TARGET = txt("target");

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        ShipAPI target = findTarget(ship, ship);
        if (target != null) {
            return READY;
        } else {
            return TARGET;
        }
    }

    private static boolean isFighterReturning(FighterWingAPI wing, ShipAPI fighter) {
        for (FighterWingAPI.ReturningFighter returningFighter : wing.getReturning()) {
            if (returningFighter.fighter == fighter) {
                return true;
            }
        }
        return false;
    }

    protected static ShipAPI findTarget(ShipAPI carrier, ShipAPI currentFighter) {
        Vector2f worldCursorPosition = CombatUtils.toWorldCoordinates(new Vector2f(Mouse.getX(), Mouse.getY()));
        ShipAPI closestValidFighter = null;
        float closestDist = Float.MAX_VALUE;

        for (FighterWingAPI w : carrier.getAllWings()) {
            for (ShipAPI f : w.getWingMembers()) {
                if (f == currentFighter) continue;
                if (f.isAlive() && !f.isLanding() && !isFighterReturning(w, f)) {
                    float dist = MathUtils.getDistanceSquared(f, worldCursorPosition);
                    if (dist < closestDist) {
                        closestValidFighter = f;
                        closestDist = dist;
                    }
                }
            }
        }

        return closestValidFighter;
    }

    private void switchToCarrier(ShipAPI carrier, ShipAPI fighter) {
        Global.getCombatEngine().addPlugin(new GradualTimeFlowScript(carrier, fighter, SYSTEM_ID, 0.25f, 1f, 0.5f));
        Global.getCombatEngine().setPlayerShipExternal(carrier);

        MagicSubsystemsManager.removeSubsystemFromShip(fighter, ControlCarrierSwitch.class);
        MagicSubsystemsManager.removeSubsystemFromShip(fighter, ControlFighterSwitch.class);

        Global.getCombatEngine().spawnEmpArcVisual(fighter.getLocation(), fighter, carrier.getLocation(), carrier, 3f, Color.RED, Color.WHITE);
    }

    private void switchToFighter(ShipAPI carrier, ShipAPI oldFighter, ShipAPI fighter) {
        Global.getCombatEngine().setPlayerShipExternal(fighter);

        ControlFighterSwitch subsystem1 = new ControlFighterSwitch(fighter, carrier);
        ControlCarrierSwitch subsystem2 = new ControlCarrierSwitch(fighter, carrier);
        MagicSubsystemsManager.addSubsystemToShip(fighter, subsystem1);
        MagicSubsystemsManager.addSubsystemToShip(fighter, subsystem2);
        subsystem1.setState(MagicSubsystem.State.COOLDOWN);
        subsystem2.setState(MagicSubsystem.State.COOLDOWN);

        if (oldFighter != null) {
            Global.getCombatEngine().addPlugin(new GradualTimeFlowScript(fighter, oldFighter, SYSTEM_ID, 0.25f, 0.75f, 0.5f));
            Global.getCombatEngine().spawnEmpArcVisual(oldFighter.getLocation(), oldFighter, fighter.getLocation(), fighter, 3f, Color.RED, Color.WHITE);
            MagicSubsystemsManager.removeSubsystemFromShip(oldFighter, ControlFighterSwitch.class);
            MagicSubsystemsManager.removeSubsystemFromShip(oldFighter, ControlCarrierSwitch.class);
        } else {
            Global.getCombatEngine().addPlugin(new GradualTimeFlowScript(fighter, carrier, SYSTEM_ID, 0.25f, 0.75f, 0.5f));
            Global.getCombatEngine().spawnEmpArcVisual(carrier.getLocation(), carrier, fighter.getLocation(), fighter, 3f, Color.RED, Color.WHITE);
        }

        ShipSystemAPI system = fighter.getSystem();
        if (system != null) {
            if (system.getCooldownRemaining() > 0f && system.isCoolingDown()) {
                system.setCooldownRemaining(system.getCooldownRemaining() / 2f);
            }
            if (system.getAmmo() < system.getMaxAmmo() && system.getAmmoPerSecond() > 0) {
                system.setAmmoReloadProgress(system.getAmmoReloadProgress() * 2f);
            }
        }

    }

    private class GradualTimeFlowScript extends BaseEveryFrameCombatPlugin {
        private final String id;
        private final float startMult;
        private final float endMult;
        private final float startTime;
        private float time;
        private boolean inited = false;
        private final ShipAPI ship;
        private final ShipAPI fromShip;
        private float distance = 0f;

        private GradualTimeFlowScript(ShipAPI ship, ShipAPI fromShip, String id, float startMult, float endMult, float time) {
            this.ship = ship;
            this.fromShip = fromShip;
            this.id = id;
            this.startMult = startMult;
            this.endMult = endMult;
            this.startTime = time;
            this.time = time;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (!inited) {
                inited = true;
                ship.giveCommand(ShipCommand.SELECT_GROUP, null, 0);
            }

            time -= amount * Global.getCombatEngine().getTimeMult().getModifiedValue();
            if (time < 0) {
                if (endMult == 1) {
                    Global.getCombatEngine().getTimeMult().unmodify(id);
                } else {
                    Global.getCombatEngine().getTimeMult().modifyMult(id, endMult);
                }
                Global.getCombatEngine().removePlugin(this);
            } else {
                float newMult = endMult - (endMult - startMult) * (time / startTime);
                Global.getCombatEngine().getTimeMult().modifyMult(id, newMult);
            }
        }
    }

    private class ControlFighterSwitch extends MagicSubsystem {
        private ShipAPI carrier;
        public ControlFighterSwitch(ShipAPI ship, ShipAPI carrier) {
            super(ship);
            this.carrier = carrier;
        }

        @Override
        public int getOrder() {
            return ORDER_SHIP_UNIQUE;
        }

        @Override
        public String getDisplayText() {
            return txt("wanzerLinkSubsystemName");
        }

        @Override
        public float getBaseActiveDuration() {
            return 1f;
        }

        @Override
        public float getBaseCooldownDuration() {
            return 4f;
        }

        @Override
        public boolean isToggle() {
            return false;
        }

        @Override
        public boolean shouldActivateAI(float amount) {
            return false;
        }

        @Override
        public boolean canActivate() {
            return carrier.getSystem().isActive();
        }

        @Override
        public void onActivate() {
            ShipAPI target = findTarget(carrier, ship);
            if (target != null) {
                if (carrier == ship) {
                    switchToFighter(carrier, null, target);
                } else {
                    switchToFighter(carrier, ship, target);
                }
            }
        }
    }

    private class ControlCarrierSwitch extends MagicSubsystem {
        private ShipAPI carrier;

        public ControlCarrierSwitch(ShipAPI ship, ShipAPI carrier) {
            super(ship);
            this.carrier = carrier;
        }

        //fighters don't usually have subsystems
        @Override
        public int getOrder() {
            return ORDER_SHIP_MODULAR;
        }

        @Override
        public String getDisplayText() {
            return txt("wanzerLinkedSubsystemName");
        }

        @Override
        public float getBaseActiveDuration() {
            return 1f;
        }

        @Override
        public float getBaseCooldownDuration() {
            return 1f;
        }

        @Override
        public boolean isToggle() {
            return false;
        }

        @Override
        public boolean shouldActivateAI(float amount) {
            return false;
        }

        @Override
        public boolean canActivate() {
            return true;
        }

        @Override
        public void onActivate() {
            switchToCarrier(carrier, ship);
        }


        @Override
        public void advanceInternal(float amount) {
            if (!ship.isAlive() || !carrier.isAlive()) {
                switchToCarrier(carrier, ship);
                return;
            }

            super.advanceInternal(amount);
        }

        @Override
        public void advance(float amount, boolean isPaused) {
            ShipAPI closest = AIUtils.getNearestEnemy(ship);

            if (closest != null && MathUtils.getDistance(closest, ship) <= 0) {
                Vector2f force = MathUtils.getPointOnCircumference(new Vector2f(), closest.getCollisionRadius(), VectorUtils.getAngle(closest.getLocation(), ship.getLocation()));
                Vector2f dist = new Vector2f();
                Vector2f.sub(closest.getLocation(), ship.getLocation(), dist);
                Vector2f.add(force, dist, force);
                force.scale(amount * 2);

                Vector2f vel = ship.getVelocity();
                Vector2f.add(vel, force, vel);
            }
        }
    }
}