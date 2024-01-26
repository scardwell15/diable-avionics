package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class Diableavionics_cicadaAI implements MissileAIPlugin, GuidedMissileAI {

    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private final IntervalUtil blink = new IntervalUtil(0.4f, 0.4f);
    private final String SOUND_SMALL = "diableavionics_cicada_hit", SOUND_LARGE = "diableavionics_virtuousGrenade_hit";
    private boolean armed = false;
    private int mult = 1;

    public Diableavionics_cicadaAI(MissileAPI missile, ShipAPI launchingShip) {
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        this.missile = missile;

        //check if the grenade is from the Blizzaia or the Virtuous
        if (missile.getSpec().getArmingTime() > 1.5f) {
            mult = 2;
        }

        missile.setArmingTime((missile.getArmingTime() - (float) (Math.random() / 2)) * launchingShip.getMutableStats().getMissileWeaponRangeBonus().getBonusMult());
    }

    @Override
    public void advance(float amount) {
        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused() || missile.isFading()) {
            return;
        }

        if (missile.getVelocity().lengthSquared() > 400) {
            missile.giveCommand(ShipCommand.DECELERATE);
        }

        ShipAPI enemy = null;
        float distance, closestDistance = Float.MAX_VALUE;
        for (ShipAPI tmp : AIUtils.getEnemiesOnMap(missile)) {
            distance = MathUtils.getDistance(tmp, missile.getLocation());
            if (distance < closestDistance) {
                enemy = tmp;
                closestDistance = distance;
            }
        }

        //if the grenade is within the collision circle of the nearest enemy, set the arming time to a random time of 0.75s at most
        if (enemy != null && closestDistance <= (enemy.getCollisionRadius() * 1.1f) + missile.getCollisionRadius()) {
            if (!armed) {
                missile.setArmingTime(missile.getElapsed() + MathUtils.getRandomNumberInRange(0.5f, 0.75f));
                armed = true;
            }
        }

        if (missile.isArmed()) {
            /*
            public DamagingExplosionSpec(
                float duration,
                float radius,
                float coreRadius,
                float maxDamage, 
                float minDamage, 
                CollisionClass collisionClass,
                CollisionClass collisionClassByFighter,
                float particleSizeMin,
                float particleSizeRange,
                float particleDuration,
                int particleCount,
                Color particleColor,
                Color explosionColor
            )
            */
            DamagingExplosionSpec boom = new DamagingExplosionSpec(
                    0.1f,
                    100 * mult,
                    50 * mult,
                    missile.getDamageAmount(),
                    missile.getDamageAmount() / 5,
                    CollisionClass.PROJECTILE_NO_FF,
                    CollisionClass.PROJECTILE_FIGHTER,
                    2,
                    5,
                    5,
                    25,
                    new Color(225, 100, 0),
                    new Color(200, 100, 25)
            );
            boom.setDamageType(DamageType.HIGH_EXPLOSIVE);
            boom.setShowGraphic(false);
            if (mult < 2) {
                boom.setSoundSetId(SOUND_SMALL);
            } else {
                boom.setSoundSetId(SOUND_LARGE);
            }
            engine.spawnDamagingExplosion(boom, missile.getSource(), missile.getLocation(), false);

            //visual effect
            Vector2f vel = new Vector2f(missile.getVelocity());
            vel.scale(0.15f);
            engine.addSmoothParticle(missile.getLocation(), vel, 300 * mult, 2, 0.1f, Color.white);
            engine.addHitParticle(missile.getLocation(), vel, 200 * mult, 1, 0.4f, new Color(200, 100, 25));
            engine.spawnExplosion(missile.getLocation(), vel, Color.DARK_GRAY, 75 * mult, 2);

            for (int i = 0; i < 25; i++) {
                engine.addHitParticle(
                        missile.getLocation(),
                        MathUtils.getRandomPointInCircle(vel, 500),
                        MathUtils.getRandomNumberInRange(3, 6),
                        1,
                        MathUtils.getRandomNumberInRange(0.15f, 0.3f),
                        Color.ORANGE);
            }

            //destroy grenade
//            engine.applyDamage(missile, missile.getLocation(), 1000, DamageType.FRAGMENTATION, 0, true, false, missile.getSource());
            engine.removeEntity(missile);
        } else {
            blink.advance(amount);
            if (blink.intervalElapsed()) {
                float ramp = 0.4f * missile.getArmingTime();
                blink.setInterval(Math.max(0.1f, blink.getMinInterval() * ramp), Math.max(0.1f, blink.getMinInterval() * ramp));
                engine.addHitParticle(missile.getLocation(), missile.getVelocity(), 50 * mult, Math.min(1, missile.getElapsed() / 2f), 0.1f, Color.red);
            }
        }
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
}