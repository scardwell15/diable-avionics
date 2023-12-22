package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class DiableAvionics_itanoCircusDrone extends BaseEveryFrameCombatPlugin  {
    public enum State {
        WAIT,
        SIGNAL,
        FIRE,
        DONE,
    }

    protected State state = State.WAIT;
    protected ShipAPI ship;
    protected ShipAPI target;

    protected ShipAPI demDrone;

    protected float targetingTime = 0.5f;
    protected float elapsedTargeting = 0f;

    protected int numBursts = 5;
    protected int missilesPerBurst = 1;

    protected float burstDelay = 0.2f;
    protected float elapsedBurst = 0f;

    protected String targetingLaserId = "targetinglaser1";
    protected String payloadWeaponId = "diableavionics_virtuous_mmDump_DEM";

    public DiableAvionics_itanoCircusDrone(ShipAPI ship, ShipAPI target) {
        this.ship = ship;
        this.target = target;
        this.targetingTime = MathUtils.getRandomNumberInRange(0.5f, 0.75f);

        if (target.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) {
            numBursts = 20;
            burstDelay = 0.1f;
        } else if (target.getHullSize() == ShipAPI.HullSize.CRUISER) {
            numBursts = 15;
            burstDelay = 0.15f;
        } else if (target.getHullSize() != ShipAPI.HullSize.FIGHTER) {
            numBursts = 10;
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (Global.getCombatEngine().isPaused()) return;

        boolean doCleanup = state == State.DONE;
        if (doCleanup) {
            if (demDrone != null) {
                Global.getCombatEngine().removeEntity(demDrone);
            }
            Global.getCombatEngine().removePlugin(this);
            return;
        }

        if (state == State.WAIT) {
            if (target != null) {
                ShipHullSpecAPI spec = Global.getSettings().getHullSpec("dem_drone");
                ShipVariantAPI v = Global.getSettings().createEmptyVariant("dem_drone", spec);
                v.addWeapon("WS 000", targetingLaserId);
                WeaponGroupSpec g = new WeaponGroupSpec(WeaponGroupType.LINKED);
                g.addSlot("WS 000");
                v.addWeaponGroup(g);
                v.addWeapon("WS 001", payloadWeaponId);
                g = new WeaponGroupSpec(WeaponGroupType.LINKED);
                g.addSlot("WS 001");
                v.addWeaponGroup(g);

                demDrone = Global.getCombatEngine().createFXDrone(v);
                demDrone.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
                demDrone.setOwner(ship.getOriginalOwner());
                demDrone.getMutableStats().getBeamWeaponRangeBonus().modifyFlat("dem", 5000f);
                demDrone.getMutableStats().getMissileWeaponFluxCostMod().modifyMult("dem", 0f);
                demDrone.getMutableStats().getHullDamageTakenMult().modifyMult("dem", 0f); // so it's non-targetable
                demDrone.setDrone(true);
                demDrone.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP, 100000f, ship);
                demDrone.getMutableStats().getMissileWeaponDamageMult().applyMods(ship.getMutableStats().getMissileWeaponDamageMult());
                demDrone.setCollisionClass(CollisionClass.NONE);
                demDrone.giveCommand(ShipCommand.SELECT_GROUP, null, 0);
                Global.getCombatEngine().addEntity(demDrone);

                state = State.SIGNAL;
            }
        } else if (state == State.SIGNAL) {
            demDrone.giveCommand(ShipCommand.SELECT_GROUP, null, 0);
            demDrone.giveCommand(ShipCommand.FIRE, target.getLocation(), 0);

            elapsedTargeting += amount;
            if (elapsedTargeting >= targetingTime) {
                state = State.FIRE;
            }
        } else if (state == State.FIRE) {
            // use payload's normal range as defined in weapon_data.csv
            demDrone.getMutableStats().getBeamWeaponRangeBonus().unmodifyFlat("dem");
            demDrone.setShipTarget(target);

            elapsedBurst += amount;
            if (elapsedBurst >= burstDelay) {
                elapsedBurst = 0f;

                for (int i = 0; i < missilesPerBurst; i++) {
                    float facing = VectorUtils.getAngle(ship.getLocation(), target.getLocation()) + MathUtils.getRandomNumberInRange(-30f, 30f);
                    CombatEntityAPI missile = Global.getCombatEngine().spawnProjectile(ship, demDrone.getWeaponGroupsCopy().get(1).getWeaponsCopy().get(0), "diableavionics_virtuous_mmDump_DEM", ship.getLocation(), facing, ship.getVelocity());
                }
                numBursts--;
            }

            if (numBursts == 0) {
                state = State.DONE;
            }
        }

        updateDroneState(amount);
    }

    protected void updateDroneState(float amount) {
        if (demDrone != null) {
            demDrone.setOwner(ship.getOwner());
            demDrone.getLocation().set(ship.getLocation());
            demDrone.setFacing(ship.getFacing());
            demDrone.getVelocity().set(ship.getVelocity());
            demDrone.setAngularVelocity(ship.getAngularVelocity());

            Vector2f dir = VectorUtils.getDirectionalVector(ship.getLocation(), target.getLocation());
            dir.scale(1000f);
            Vector2f.add(dir, ship.getLocation(), dir);
            demDrone.getMouseTarget().set(dir);

            WeaponAPI tLaser = demDrone.getWeaponGroupsCopy().get(0).getWeaponsCopy().get(0);
            WeaponAPI payload = demDrone.getWeaponGroupsCopy().get(1).getWeaponsCopy().get(0);
            tLaser.setFacing(VectorUtils.getAngle(ship.getLocation(), target.getLocation()));
            payload.setFacing(VectorUtils.getAngle(ship.getLocation(), target.getLocation()));
            tLaser.setKeepBeamTargetWhileChargingDown(true);
            tLaser.setScaleBeamGlowBasedOnDamageEffectiveness(false);
            tLaser.updateBeamFromPoints();
        }
    }
}