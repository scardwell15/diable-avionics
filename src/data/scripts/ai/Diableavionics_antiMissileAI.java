//by Tartiflette, Anti-missile missile AI: precise and able to randomly choose a target between nearby enemy missiles.
//feel free to use it, credit is appreciated but not mandatory
//V2 done
package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicTargeting;

import java.awt.*;
import java.util.List;

public class Diableavionics_antiMissileAI implements MissileAIPlugin, GuidedMissileAI {
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    //    private float timer=0, delay=0.05f;
    //data
    private final float MAX_SPEED;
    //    private final int SEARCH_RANGE = 1000;
    private final float DAMPING = 0.05f;
    private final Color EXPLOSION_COLOR = new Color(255, 0, 0, 255);
    private final Color PARTICLE_COLOR = new Color(240, 200, 50, 255);
    private final int NUM_PARTICLES = 20;

    public Diableavionics_antiMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed() * 1.25f; //slight over lead
    }

    @Override
    public void advance(float amount) {

        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        if (Global.getCombatEngine().isPaused() || missile.isFading() || missile.isFizzling()) {
            return;
        }

        // if there is no target, assign one
        if (target == null
                || !Global.getCombatEngine().isEntityInPlay(target)
                || target.getOwner() == missile.getOwner()
        ) {
            missile.giveCommand(ShipCommand.ACCELERATE);

            int targetSearchRange = (int) (missile.getWeapon().getRange() * 1.5f * (missile.getMaxFlightTime() - missile.getFlightTime()) / missile.getMaxFlightTime());
            WeightedRandomPicker<MissileAPI> targetPicker = getTargetPicker(missile.getSource(), missile.getLocation(), false, targetSearchRange);
            List<MissileAPI> near = CombatUtils.getMissilesWithinRange(missile.getLocation(), targetSearchRange);
            if (!near.isEmpty()) {
                for (MissileAPI m : near) {
                    if (m == missile) continue;
                    if (m.getOwner() == missile.getOwner()) {
                        if (m.getWeaponSpec() != null && m.getWeaponSpec().getWeaponId().equals(missile.getWeaponSpec().getWeaponId())) {
                            if (m.getMissileAI() instanceof Diableavionics_antiMissileAI) {
                                Diableavionics_antiMissileAI ai = (Diableavionics_antiMissileAI) m.getMissileAI();
                                if (ai.target instanceof MissileAPI) {
                                    MissileAPI otherMissileTarget = (MissileAPI) ai.target;
                                    int index = targetPicker.getItems().indexOf(otherMissileTarget);
                                    if (index >= 0) {
                                        targetPicker.setWeight(index, targetPicker.getWeight(index) * 0.66f);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            target = targetPicker.pick();

            return;
        }

        //finding lead point to aim to    
        float dist = MathUtils.getDistanceSquared(missile.getLocation(), target.getLocation());
        if (dist < 2500) {
            proximityFuse();
            return;
        }
        lead = AIUtils.getBestInterceptPoint(
                missile.getLocation(),
                MAX_SPEED,
                target.getLocation(),
                target.getVelocity()
        );
        if (lead == null) {
            lead = target.getLocation();
        }

        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                missile.getLocation(),
                lead
        );
        /*
        //velocity angle correction        
        float offCourseAngle = MathUtils.getShortestRotation(
                VectorUtils.getFacing(missile.getVelocity()),
                correctAngle
                );
        
        float correction = MathUtils.getShortestRotation(                
                correctAngle,
                VectorUtils.getFacing(missile.getVelocity())+180
                ) 
                * 0.5f * //oversteer
                (float)((FastTrig.sin(MathUtils.FPI/90*(Math.min(Math.abs(offCourseAngle),45))))); //damping when the correction isn't important
        
        //modified optimal facing to correct the velocity vector angle as soon as possible
        correctAngle += correction;
        */

        float correction = MathUtils.getShortestRotation(VectorUtils.getFacing(missile.getVelocity()), correctAngle);
        if (correction > 0) {
            correction = -11.25f * ((float) Math.pow(FastTrig.cos(MathUtils.FPI * correction / 90) + 1, 2) - 4);
        } else {
            correction = 11.25f * ((float) Math.pow(FastTrig.cos(MathUtils.FPI * correction / 90) + 1, 2) - 4);
        }
        correctAngle += correction;

        //turn the missile
        float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), correctAngle);
        if (aimAngle < 0) {
            missile.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            missile.giveCommand(ShipCommand.TURN_LEFT);
        }
        if (Math.abs(aimAngle) < 45) {
            missile.giveCommand(ShipCommand.ACCELERATE);
        }

        // Damp angular velocity if we're getting close to the target angle
        if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * DAMPING) {
            missile.setAngularVelocity(aimAngle / DAMPING);
        }
    }
    
    /*
    private CombatEntityAPI findRandomMissileWithinRange(MissileAPI missile){        
        
        CombatEntityAPI theTarget = AIUtils.getNearestEnemyMissile(missile);
        
        //Check if there is a missile nearby. If not, find another target.
        if (theTarget==null || !MathUtils.isWithinRange(theTarget, missile, SEARCH_RANGE*1.5f)){
            theTarget = AIUtils.getNearestEnemy(missile);
            if (theTarget==null || !MathUtils.isWithinRange(theTarget, missile, SEARCH_RANGE*1.5f)){
                return null;
            } else {
                return theTarget;
            }
        }
        
        //If there are missiles around, let's find the best one.
        WeaponAPI weapon = missile.getWeapon();
        Map<Integer, MissileAPI> PRIORITYLIST = new HashMap<>();
        Map<Integer, MissileAPI> OTHERSLIST = new HashMap<>();
        int i=1, u=1;      
        List<MissileAPI> potentialTargets = AIUtils.getNearbyEnemyMissiles(missile, SEARCH_RANGE);
        
        for(MissileAPI m : potentialTargets){
            if(Math.abs(
                    MathUtils.getShortestRotation(
                            weapon.getCurrAngle(),
                            VectorUtils.getAngle(
                                    weapon.getLocation(),
                                    m.getLocation()
                                )
                            )
                        )<10 
                    ){
                PRIORITYLIST.put(u, m);
                u++;
            } else {
                OTHERSLIST.put(i, m);
                i++;
            }
        }
        
        if(!PRIORITYLIST.containsValue((MissileAPI)theTarget)){      
            if (!PRIORITYLIST.isEmpty()){
                int chooser=Math.round((float)Math.random()*(i-1)+0.5f);
                theTarget=PRIORITYLIST.get(chooser);
            } else if (!OTHERSLIST.isEmpty()){                    
                int chooser=Math.round((float)Math.random()*(u-1)+0.5f);
                theTarget=OTHERSLIST.get(chooser);
            }
        }
        return theTarget;
    }
    */

    void proximityFuse() {
        engine.applyDamage(
                target,
                target.getLocation(),
                missile.getDamageAmount(),
                DamageType.FRAGMENTATION,
                0f,
                false,
                false,
                missile.getSource()
        );
        /*
        List<MissileAPI> closeMissiles = AIUtils.getNearbyEnemyMissiles(missile, 100); 
        for (MissileAPI cm : closeMissiles){
            if (cm!=target){
                float dist = (float) MathUtils.getDistanceSquared(missile.getLocation(), target.getLocation());
                engine.applyDamage(
                        cm,
                        cm.getLocation(),
                        (2*missile.getDamageAmount()/3) - (missile.getDamageAmount()/3) * ((float)FastTrig.cos(3000 / (dist+1000))+1),
                        DamageType.FRAGMENTATION,
                        0,
                        false,
                        true,
                        missile.getSource()
                );
            }
        }
        */
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
                100,
                50,
                missile.getDamageAmount(),
                50,
                CollisionClass.PROJECTILE_NO_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                2,
                5,
                5,
                25,
                new Color(225, 100, 0),
                new Color(200, 100, 25)
        );
        boom.setDamageType(DamageType.FRAGMENTATION);
        boom.setShowGraphic(false);
        boom.setSoundSetId("explosion_flak");
        engine.spawnDamagingExplosion(boom, missile.getSource(), missile.getLocation());

        if (MagicRender.screenCheck(0.1f, missile.getLocation())) {
            engine.addHitParticle(
                    missile.getLocation(),
                    new Vector2f(),
                    100,
                    1,
                    0.25f,
                    EXPLOSION_COLOR
            );
            for (int i = 0; i < NUM_PARTICLES; i++) {
                float axis = (float) Math.random() * 360;
                float range = (float) Math.random() * 100;
                engine.addHitParticle(
                        MathUtils.getPointOnCircumference(missile.getLocation(), range / 5, axis),
                        MathUtils.getPointOnCircumference(new Vector2f(), range, axis),
                        2 + (float) Math.random() * 2,
                        1,
                        1 + (float) Math.random(),
                        PARTICLE_COLOR
                );
            }
            engine.applyDamage(
                    missile,
                    missile.getLocation(),
                    missile.getHitpoints() * 2f,
                    DamageType.FRAGMENTATION,
                    0f,
                    false,
                    false,
                    missile
            );
        } else {
            engine.removeEntity(missile);
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

    public void init(CombatEngineAPI engine) {
    }

    public WeightedRandomPicker<MissileAPI> getTargetPicker(CombatEntityAPI source, Vector2f searchPos, boolean ignoreFlares, float maxRange) {
        CombatEngineAPI engine = Global.getCombatEngine();
        WeightedRandomPicker<MissileAPI> missilePicker = new WeightedRandomPicker<>();

        List<MissileAPI> missiles = engine.getMissiles();
        for (MissileAPI m : missiles) {
            if (!ignoreFlares || !m.isFlare()) {
                if (!m.isFading() && m.getOwner() != source.getOwner() && m.getCollisionClass() != CollisionClass.NONE && m.getSpec().isRenderTargetIndicator()) { //is the missile alive, hittable and hostile
                    if (CombatUtils.isVisibleToSide(m, source.getOwner()) && MathUtils.isPointWithinCircle(searchPos, m.getLocation(), maxRange)) { //is it around
                        missilePicker.add(m, m.getDamageAmount() * (1 + m.getMoveSpeed() / MathUtils.getDistance(source, m)));
                    }
                }
            }
        }

        return missilePicker;
    }
}
