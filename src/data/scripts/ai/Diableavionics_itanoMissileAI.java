package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicTargeting;
import data.scripts.weapons.Diableavionics_virtuous_itanoEffect;
import java.awt.Color;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_itanoMissileAI implements MissileAIPlugin, GuidedMissileAI{

    //////////////////////
    //     SETTINGS     //
    //////////////////////
    
    private final float DAMPING=0.1f;
    private final int SEARCH_CONE=360;
    private float PRECISION_RANGE=300;
        
    private final Color EXPLOSION_COLOR = new Color(255, 0, 0, 255);
    private final Color PARTICLE_COLOR = new Color(240, 200, 50, 255);
    private final int NUM_PARTICLES = 20;
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target=null;
    private Vector2f lead = new Vector2f();
    private boolean launch=true;
    private float timer=0, check=0f, correctAngle;
    private final float random;

    public Diableavionics_itanoMissileAI(MissileAPI missile, ShipAPI launchingShip){	
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        
        this.missile = missile;        
        //calculate the precision range factor
        PRECISION_RANGE=(float)Math.pow((4*PRECISION_RANGE),2);      
        random=MathUtils.getRandomNumberInRange(0, 3.14f);
    }

    @Override
    public void advance(float amount) {
        
        //skip the AI if the game is paused
        if (engine.isPaused()) {return;}
        
        //fading failsafe
        if(missile.isFizzling() || missile.isFading()){
            engine.applyDamage(missile, missile.getLocation(), missile.getHitpoints()*2, DamageType.FRAGMENTATION, 0, true, false, missile.getSource());
        }
        
        if(!missile.isArmed()){
            return;
        }
        
        if (target == null && launch){
            //grab the initial target
            for(WeaponAPI w : missile.getSource().getAllWeapons()){
                if(w.getSlot().getId().equals("PAULDRON_L")){
                    Diableavionics_virtuous_itanoEffect script = (Diableavionics_virtuous_itanoEffect)w.getEffectPlugin();
                    target = script.getTarget();
                    //debug
//                    if(target!=null){
//                        engine.addFloatingText(target.getLocation(), "locked", 30, Color.red, target, 0.1f, 0.5f);
//                    }
                }
            }
            return;
            
        } else if(
                //assigning a new target if there is none, it is phased or it got destroyed
                target == null
                ||(target instanceof ShipAPI && ((ShipAPI)target).isHulk())
                || !engine.isEntityInPlay(target)
                || target.getCollisionClass()==CollisionClass.NONE
                ){   
            
            setTarget(MagicTargeting.pickTarget(
                    missile,
                    MagicTargeting.targetSeeking.NO_RANDOM,
                    (int)(missile.getWeapon().getRange()/(missile.getElapsed()/5)),
                    SEARCH_CONE,
                    1,1,1,1,1,
                    true
                )
            );
            launch=true;
            return;
        }
        
        timer+=amount;
        
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            
            timer -=check;
            
            float dist = MathUtils.getDistanceSquared(missile.getLocation(), target.getLocation());
            
            //prox-fuse against fighters
            if (
                    (
                    target instanceof ShipAPI && 
                        ( 
                            ((ShipAPI)target).isFighter() 
                            || 
                            ((ShipAPI)target).isDrone() )
                        ) 
                    && dist<2500
                    ){
                proximityFuse();
                return;
            }
            
            //set the next check time
            dist*=1/PRECISION_RANGE;
            check = Math.min(
                    0.25f,
                    Math.max(
                            0.05f,
                            dist)
            );
            
           
            
            //best intercepting point
            lead = AIUtils.getBestInterceptPoint(
                    missile.getLocation(),
                    missile.getMaxSpeed(), //if eccm is intalled the point is accurate, otherwise it's placed closer to the target (almost tailchasing)
                    target.getLocation(),
                    target.getVelocity()
            );                
            //null pointer protection
            if (lead == null) {
                lead = target.getLocation(); 
            }
        }

        //best velocity vector angle for interception
        correctAngle = VectorUtils.getAngle(
                        missile.getLocation(),
                        lead
                );        
        
        
        
        //OVERSTEER
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
        
        
        //waving
        correctAngle += 60*check*(float)FastTrig.sin(random+missile.getElapsed()*4);
        
        //debug
        //MagicRender.singleframe(Global.getSettings().getSprite("diableavionics", "DIAMOND"), missile.getLocation(), new Vector2f(128,2), correctAngle, Color.white, true, CombatEngineLayers.BELOW_INDICATORS_LAYER);
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( missile.getFacing(), correctAngle);
        
        if (aimAngle < 0) {
            missile.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            missile.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * DAMPING) {
            missile.setAngularVelocity(aimAngle / DAMPING);
        }
        
//        if(Math.abs(aimAngle)<90){
            missile.giveCommand(ShipCommand.ACCELERATE);  
//        }
    }
    
    
    void proximityFuse(){
        
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
        
        DamagingExplosionSpec boom = new DamagingExplosionSpec(
                0.1f,
                50,
                25,
                missile.getDamageAmount(),
                50,
                CollisionClass.PROJECTILE_NO_FF,
                CollisionClass.PROJECTILE_NO_FF,
                2,
                5,
                5,
                25,
                new Color(225,100,0),
                new Color(200,100,25)
        );
        boom.setDamageType(DamageType.FRAGMENTATION);
        boom.setShowGraphic(false);
        boom.setSoundSetId("explosion_flak");
        engine.spawnDamagingExplosion(boom, missile.getSource(), missile.getLocation());
        
        if(MagicRender.screenCheck(0.1f, missile.getLocation())){
            engine.addHitParticle(
                missile.getLocation(),
                new Vector2f(),
                100,
                1,
                0.25f,
                EXPLOSION_COLOR
            );

            for (int i=0; i<NUM_PARTICLES; i++){
                float axis = (float)Math.random()*360;
                float range = (float)Math.random()*100;
                engine.addHitParticle(
                    MathUtils.getPointOnCircumference(missile.getLocation(), range/5, axis),
                    MathUtils.getPointOnCircumference(new Vector2f(), range, axis),
                    2+(float)Math.random()*2,
                    1,
                    1+(float)Math.random(),
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
        }
        engine.removeEntity(missile);
    }

    @Override
    public CombatEntityAPI getTarget(){
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target){
        this.target = target;
    }
}