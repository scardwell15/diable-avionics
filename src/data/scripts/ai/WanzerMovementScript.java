package data.scripts.ai;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;

public class WanzerMovementScript implements AdvanceableListener {
    private ShipAPI ship = null;
    private float maxDistance = Float.MAX_VALUE;
    private IntervalUtil decelerateInterval = new IntervalUtil(0.25f, 0.5f);
    private boolean canDecelerate = true;
    private IntervalUtil strafeInterval = new IntervalUtil(4f, 8f);

    private static ShipCommand[] validStrafeStates = new ShipCommand[]{ShipCommand.STRAFE_LEFT, ShipCommand.STRAFE_RIGHT};
    private ShipCommand strafeState = ShipCommand.STRAFE_LEFT;

    public WanzerMovementScript(ShipAPI ship) {
        this.ship = ship;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.isDecorative() || weapon.hasAIHint(WeaponAPI.AIHints.PD)) continue;
            if (weapon.getRange() < maxDistance) {
                maxDistance = weapon.getRange() * 0.9f;
            }
        }
        maxDistance = maxDistance * maxDistance;
    }

    @Override
    public void advance(float amount) {
        strafeInterval.advance(amount);
        if (strafeInterval.intervalElapsed()) {
            strafeState = randomStrafeDir();
        }

        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float distance = MathUtils.getDistanceSquared(ship, target);
            if (distance <= maxDistance) {
                ship.giveCommand(strafeState, null, 0);
            }

            if (distance <= maxDistance * 0.8f) {
                decelerateInterval.advance(amount);
                if (decelerateInterval.intervalElapsed()) {
                    canDecelerate = true;
                }

                if (canDecelerate) {
                     ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
                }
            } else {
                canDecelerate = false;
            }
        }
    }

    private static ShipCommand randomStrafeDir() {
        return validStrafeStates[MathUtils.getRandomNumberInRange(0, validStrafeStates.length - 1)];
    }
}
