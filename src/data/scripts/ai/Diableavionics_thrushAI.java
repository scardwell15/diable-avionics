//By Tartiflette
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
import org.magiclib.util.MagicTargeting;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_thrushAI implements MissileAIPlugin, GuidedMissileAI {
              
    //////////////////////
    //     SETTINGS     //
    //////////////////////
    
    //Angle with the target beyond which the missile turn around without accelerating. Avoid endless circling.
    //  Set to a negative value to disable
    private final float OVERSHOT_ANGLE=90;
    
    //Damping of the turn speed when closing on the desired aim. The smaller the snappier.
    private final float DAMPING=0.1f;
    
    //range under which the missile start to get progressively more precise in game units.
    private float PRECISION_RANGE=1000;
    
    //Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
    //   1: perfect leading with and without ECCM
    //   2: half precision without ECCM
    //   3: a third as precise without ECCM. Default
    //   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
    private float ECCM=3;
    
    private final int SEARCH_CONE=360;
    
    //////////////////////
    //    VARIABLES     //
    //////////////////////
    
    //max speed of the missile after modifiers.
    private final float MAX_SPEED;
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    private boolean launch=true;
    private float timer=0, check=0f;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public Diableavionics_thrushAI(MissileAPI missile, ShipAPI launchingShip) {
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            ECCM=1;
        }        
        //calculate the precision range factor
        PRECISION_RANGE=(float)Math.pow((2*PRECISION_RANGE),2);
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused() || missile.isFading()) {return;}
        
        //assigning a target if there is none or it got destroyed
        if (target == null
                || (target instanceof ShipAPI && ((ShipAPI)target).isHulk())
                || !engine.isEntityInPlay(target)
                ||target.getCollisionClass()==CollisionClass.NONE
                ){     
            setTarget(
                    MagicTargeting.pickTarget(
                            missile,
                            MagicTargeting.targetSeeking.NO_RANDOM,
                            (int)missile.getWeapon().getRange(),
                            SEARCH_CONE,
                            0,1,1,1,1,
                            true
                    )
            );
            //forced acceleration by default
            missile.giveCommand(ShipCommand.ACCELERATE);
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
                    MAX_SPEED*ECCM, //if eccm is intalled the point is accurate, otherwise it's placed closer to the target (almost tailchasing)
                    target.getLocation(),
                    target.getVelocity()
            );                
            //null pointer protection
            if (lead == null) {
                lead = target.getLocation(); 
            }
            
            if((missile.getFlightTime()>3 // missile wasn't just launched
                    && (missile.getVelocity().lengthSquared()>40000 // missile reached a decent speed
                     && Math.abs( MathUtils.getShortestRotation(VectorUtils.getFacing(missile.getVelocity()), VectorUtils.getAngle(missile.getLocation(), lead)))>OVERSHOT_ANGLE) // missile velocity is completely off target
                    )
                    || missile.isFizzling()
                    ){
                for(float i=0; i<=12; i++){
                    engine.spawnProjectile(
                            missile.getSource(),
                            missile.getWeapon(),
                            "diableavionics_micromissile",
                            missile.getLocation(),
                            missile.getFacing() - 90 + 45*i,
                            missile.getVelocity()
                    );
                }                
                engine.applyDamage(
                        missile,
                        missile.getLocation(),
                        missile.getHitpoints()*2,
                        DamageType.FRAGMENTATION,
                        0,
                        true,
                        false,
                        missile.getSource()
                );
            }            
        }
        
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
        
        //target angle for interception        
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
        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
}