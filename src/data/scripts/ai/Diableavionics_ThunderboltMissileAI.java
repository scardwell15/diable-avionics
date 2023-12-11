package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.magiclib.util.MagicTargeting;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_ThunderboltMissileAI implements MissileAIPlugin, GuidedMissileAI
{
    //////////////////////
    //     SETTINGS     //
    //////////////////////
    
    private final float OVERSHOT_ANGLE=90, WAVE_TIME=2, WAVE_AMPLITUDE=5, DAMPING=0.1f, MAX_SPEED, OFFSET;
    private final int SEARCH_CONE=360;
    private float  PRECISION_RANGE=600;
    
    //Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
    //   1: perfect leading with and without ECCM
    //   2: half precision without ECCM
    //   3: a third as precise without ECCM. Default
    //   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
    private float ECCM=2;   //A VALUE BELOW 1 WILL PREVENT THE MISSILE FROM EVER HITTING ITS TARGET!
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    private boolean launch=true;
    private float timer=0, check=0f;

    public Diableavionics_ThunderboltMissileAI(MissileAPI missile, ShipAPI launchingShip) {	
        
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
        OFFSET=(float)(Math.random()*MathUtils.FPI*2);
        
        //halve the damage due to the onHit effect
        missile.setDamageAmount(missile.getBaseDamageAmount()/2);
    }

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
            setTarget(MagicTargeting.pickTarget(missile,MagicTargeting.targetSeeking.NO_RANDOM,(int)missile.getWeapon().getRange(),SEARCH_CONE,0,1,1,1,1,true));
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
        }
        
        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                        missile.getLocation(),
                        lead
                );
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( missile.getFacing(), correctAngle);
        
        if(Math.abs(aimAngle)<OVERSHOT_ANGLE){
            missile.giveCommand(ShipCommand.ACCELERATE);  
        }
        
        //waving
        aimAngle+=WAVE_AMPLITUDE*check*ECCM*Math.cos(OFFSET+missile.getElapsed()*(2*MathUtils.FPI/WAVE_TIME));    
        
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
    public CombatEntityAPI getTarget()
    {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target)
    {
        this.target = target;
    }
}