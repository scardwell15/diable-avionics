
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_assumeControlEffect implements EveryFrameWeaponEffectPlugin {
    private boolean runOnce = false, activated = false;
    private int switchShip = 0;
    private ShipAPI theCarrier, theFighter = null;
    private ShipSystemAPI theSystem;
    private final String id = "diableavionics_assumeControl";
    private final IntervalUtil timer = new IntervalUtil(0.9f, 1.1f);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused()) return;

        if (!runOnce) {
            runOnce = true;
            theCarrier = weapon.getShip();
            theSystem = theCarrier.getSystem();
        }

        if (theSystem.isStateActive() && theCarrier.isAlive()) {

            timer.advance(amount);

            if (!activated) {
                activated = true;
                //check if the ship is the player ship
                if (engine.getPlayerShip() == theCarrier
                        && theCarrier.getAI() == null
                        //and that there are wings fitted
                        && !theCarrier.getAllWings().isEmpty()
                ) {
                    switchShip = 2;
                }
                timer.setElapsed(0.75f);
            }

            //timer check for CPU saving

            if (timer.intervalElapsed()) {

                //switch control to lead fighter
                if (switchShip > 1) {
                    for (FighterWingAPI w : theCarrier.getAllWings()) {
                        if (w.getLeader() != null
                                && w.getLeader().isAlive()
                                && !w.getLeader().isLanding()
                                && !w.getLeader().isLiftingOff()
                        ) {
                            theFighter = w.getLeader();
                        }
                    }
                    //there is a lead fighter to switch to
                    if (theFighter != null) {
                        switchShip = 1;
                        Global.getCombatEngine().setPlayerShipExternal(theFighter);
                        Global.getCombatEngine().getTimeMult().modifyMult(id, 0.75f);
                    }
                }

                //switch control to carrier on piloted fighter death or autopilot
                else if (switchShip > 0) {
                    if (!theFighter.isAlive() || theFighter.isLanding() || theFighter.getAI() != null) {
                        //switch back to the carrier if the piloted fighter is dead or in autopilot
                        switchShip = 0;
                        Global.getCombatEngine().setPlayerShipExternal(theCarrier);
                        Global.getCombatEngine().getTimeMult().unmodify(id);
                    }
                }
            }

            //anti hoover for player piloted fighters
            if (theFighter != null) {
                ShipAPI closest = AIUtils.getNearestEnemy(theFighter);

                if (closest != null && MathUtils.getDistanceSquared(closest, theFighter) <= 0) {
                    Vector2f force = MathUtils.getPointOnCircumference(new Vector2f(), closest.getCollisionRadius(), VectorUtils.getAngle(closest.getLocation(), theFighter.getLocation()));
                    Vector2f dist = new Vector2f();
                    Vector2f.sub(closest.getLocation(), theFighter.getLocation(), dist);
                    Vector2f.add(force, dist, force);
                    force.scale(amount * 2);

                    Vector2f vel = theFighter.getVelocity();
                    Vector2f.add(vel, force, vel);
                }
            }

        } else if (activated) {
            activated = false;
            //switch control to carrier on system deactivation
            if (switchShip > 0) {
                switchShip = 0;
                theFighter = null;
                Global.getCombatEngine().setPlayerShipExternal(theCarrier);
                Global.getCombatEngine().getTimeMult().unmodify(id);
            }
        }
    }
}