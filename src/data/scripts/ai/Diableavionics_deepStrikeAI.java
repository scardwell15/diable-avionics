//By Tartiflette
package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicTargeting;
import data.scripts.weapons.Diableavionics_deepStrikeEffect;
import java.awt.Color;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_deepStrikeAI implements MissileAIPlugin, GuidedMissileAI {
              
    //////////////////////
    //     SETTINGS     //
    //////////////////////
    
    //Angle with the target beyond which the missile turn around without accelerating. Avoid endless circling.
    //  Set to a negative value to disable
    private final float OVERSHOT_ANGLE=90;
    
    //Damping of the turn speed when closing on the desired aim. The smaller the snappier.
    private final float DAMPING=0.5f;
    
    //range under which the missile start to get progressively more precise in game units.
    private float PRECISION_RANGE=1000;
    
    //////////////////////
    //    VARIABLES     //
    //////////////////////
    
    //max speed of the missile after modifiers.
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private ShipAPI target;
    private Vector2f lead = new Vector2f(), offset= new Vector2f();
    private boolean launch=true, rushing=false;
    private float timer=0, check=0f, lastDist=0;
    private final float MAX_SPEED;
    private final IntervalUtil jitter = new IntervalUtil(0.08f,0.12f);

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public Diableavionics_deepStrikeAI(MissileAPI missile, ShipAPI launchingShip) {
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed();
        //calculate the precision range factor
        PRECISION_RANGE=(float)Math.pow((2*PRECISION_RANGE),2);
        missile.setArmingTime(20);
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused() || missile.isFading()) {return;}
        
        //assigning a target if there is none or it got destroyed
        if (
                target == null
                || !(target instanceof ShipAPI)
                || target.isHulk()
                || !engine.isEntityInPlay(target)
                ||target.getCollisionClass()==CollisionClass.NONE
                ){     
            CombatEntityAPI newTarget=
                MagicTargeting.pickTarget(
                        missile,
                        MagicTargeting.targetSeeking.FULL_RANDOM,
                        5000,
                        360,
                        0,1,1,10,10,
                        true
                    
            );
            if(newTarget instanceof ShipAPI){
                target=(ShipAPI)newTarget;
                if(target.getParentStation()!=null){
                    target=target.getParentStation();
                }
                missile.setCollisionClass(CollisionClass.NONE);
                rushing=true;
                missile.getSpriteAPI().setColor(new Color(0.25f,0.25f,0.5f,0.5f));
            }
            
            if(target!=null){
                offset = MathUtils.getPoint(new Vector2f(), target.getCollisionRadius()*1.5f, VectorUtils.getAngle(missile.getLocation(), target.getLocation())); 
            }
            
            return;
        }
        
        timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer -=check;
            //set the next check time
            check = Math.min(
                    0.5f,
                    Math.max(
                            0.05f,
                            MathUtils.getDistanceSquared(missile.getLocation(), target.getLocation())/PRECISION_RANGE)
            );
            //best intercepting point
            lead = AIUtils.getBestInterceptPoint(
                    missile.getLocation(),
                    MAX_SPEED, //if eccm is intalled the point is accurate, otherwise it's placed closer to the target (almost tailchasing)
                    target.getLocation(),
                    target.getVelocity()
            );                
            //null pointer protection
            if (lead == null) {
                lead = target.getLocation(); 
            }
            
            Vector2f.add(lead, offset, lead);
            
            
            //trigger drop
            float distSqr = MathUtils.getDistanceSquared(missile.getLocation(), lead);
            if(distSqr<10000 || (distSqr<90000 && distSqr>lastDist)){
                dropStrikeTeam(engine, missile, target);
                return;
            } else {
                lastDist=distSqr;
            }
        }
            
        //"phased" visual effect
        
        if(rushing){
            jitter.advance(amount);
        //to do
            if(jitter.intervalElapsed() && MagicRender.screenCheck(0.5f, missile.getLocation())){
                //jitter
                SpriteAPI jitterSprite = Global.getSettings().getSprite("diableavionics","DS_afterimage");
                MagicRender.objectspace(
                        jitterSprite,
                        missile,
                        new Vector2f(-12,0),
                        new Vector2f(),
                        new Vector2f(56,80),
                        new Vector2f(),
                        180,
                        0,
                        true,
                        new Color(1,1,1,0.5f),
                        true,
                        //jitter/flicker
                        5,
                        2,
                        0.5f,
                        0.5f,
                        0.02f,      
                        //time
                        0.05f,
                        0.05f,
                        0.1f, 
                        false, 
                        CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER
                );
                //trail
                Vector2f vel = new Vector2f(missile.getVelocity());
                vel.scale(0.5f);
                SpriteAPI trailSprite = Global.getSettings().getSprite("diableavionics","DS_afterimage");
                MagicRender.battlespace(
                        trailSprite,
                        new Vector2f(missile.getLocation()),
                        new Vector2f(vel),
                        new Vector2f(56,80),
                        new Vector2f(-5.6f,-8f),
                        missile.getFacing()-90,
                        missile.getAngularVelocity(),
                        new Color(1,1,1,0.25f),
                        true,
                        0.05f,
                        0.1f,
                        0.5f
                );
            }
        }
        
        //DEBUG
//        engine.addHitParticle(lead, new Vector2f(), 30, 0.5f, 0.1f, Color.GREEN);
        
        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                        missile.getLocation(),
                        lead
                );
        
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
        correctAngle = correctAngle+correction;
        float aimAngle = MathUtils.getShortestRotation( missile.getFacing(), correctAngle);
        
        if(Math.abs(aimAngle)<OVERSHOT_ANGLE){
            missile.giveCommand(ShipCommand.ACCELERATE);  
        }
        
        if (aimAngle < 0) {
            missile.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            missile.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * DAMPING) {
            missile.setAngularVelocity(aimAngle / DAMPING);
        }
    }
    
    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
//        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
    
    //spawns a Wanzer drop pod
    private void dropStrikeTeam(CombatEngineAPI engine, MissileAPI missile, ShipAPI target){
        
        
//        ShipAPI teamate = engine.getFleetManager(missile.getOwner()).spawnShipOrWing(WANZERS.get(MathUtils.getRandomNumberInRange(0, WANZERS.size()-1)), missile.getLocation(), missile.getFacing());
//        engine.getFleetManager(-1).setSuppressDeploymentMessages(true);
        engine.getFleetManager(missile.getOwner()).setSuppressDeploymentMessages(true);
        ShipAPI dropPod = engine.getFleetManager(missile.getOwner()).spawnShipOrWing(
                "diableavionics_deepStrike_"+MathUtils.getRandomNumberInRange(1, 5),
                missile.getLocation(),
                missile.getFacing()
        );
//        engine.getFleetManager(-1).setSuppressDeploymentMessages(false);
        engine.getFleetManager(missile.getOwner()).setSuppressDeploymentMessages(false);
        
        
        /*
        ShipAPI dropPod = engine.getFleetManager(100).spawnShipOrWing(
                "diableavionics_deepStrike_"+MathUtils.getRandomNumberInRange(1, 5),
                missile.getLocation(),
                missile.getFacing()
        );
        dropPod.setOwner(missile.getOwner());
        */
        
        //inherit some velocity
        Vector2f vel = new Vector2f(missile.getVelocity());
        vel.scale(0.5f);
        
        Vector2f.add(vel,dropPod.getVelocity(), dropPod.getVelocity());
        dropPod.setAngularVelocity(missile.getAngularVelocity()*0.5f);
        
        dropPod.setOwner(missile.getOwner());
        dropPod.getCaptain().setPersonality("reckless");
        dropPod.setShipTarget(target);
        
        ((Diableavionics_deepStrikeEffect)missile.getWeapon().getEffectPlugin()).NewDeepStrikePod(dropPod);
        
        //visual pop
        if(MagicRender.screenCheck(0.5f, missile.getLocation())){
            
            SpriteAPI popSprite = Global.getSettings().getSprite("diableavionics","DS_afterimage");
            MagicRender.battlespace(
                    popSprite,
                    dropPod.getLocation(),
                    new Vector2f(),
                    new Vector2f(56,80),
                    new Vector2f(128,160),
                    dropPod.getFacing()-90,
                    dropPod.getAngularVelocity(),
                    Color.white,
                    false,
                    0,0,0,0,0,
                    0.05f,
                    0.1f,
                    0.25f,
                    CombatEngineLayers.BELOW_SHIPS_LAYER
            );
            SpriteAPI jitterSprite = Global.getSettings().getSprite("diableavionics","DS_afterimage");
            MagicRender.objectspace(
                    jitterSprite,
                    dropPod,
                    new Vector2f(-12,0),
                    new Vector2f(),
                    new Vector2f(56,80),
                    new Vector2f(),
                    180,
                    0,
                    true,
                    Color.white,
                    false,
                    //jitter/flicker
                    20,
                    5,
                    0.5f,
                    0.5f,
                    0.02f,      
                    //time
                    0.05f,
                    0.70f,
                    1.25f, 
                    false, 
                    CombatEngineLayers.BELOW_SHIPS_LAYER
            );  
        }
        
        Global.getSoundPlayer().playSound("diableavionics_deepStrike_emerge", 1, 0.75f, dropPod.getLocation(), dropPod.getVelocity());
        
        engine.removeEntity(missile);
    }
}