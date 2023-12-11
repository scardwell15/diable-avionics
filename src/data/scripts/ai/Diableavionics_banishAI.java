//By Tartiflette, fast and highly customizable Missile AI.
package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicTargeting;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_banishAI implements MissileAIPlugin, GuidedMissileAI {
          
    
    //////////////////////
    //     SETTINGS     //
    //////////////////////
    
    //Damping of the turn speed when closing on the desired aim. The smaller the snappier.
    private final float DAMPING=0.1f;
    
    //Does the missile switch its target if it has been destroyed?
    private final boolean TARGET_SWITCH=true;
    
    //Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
    //   1: perfect leading with and without ECCM
    //   2: half precision without ECCM
    //   3: a third as precise without ECCM. Default
    //   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
    private float ECCM=2;   //A VALUE BELOW 1 WILL PREVENT THE MISSILE FROM EVER HITTING ITS TARGET!    
    
    //////////////////////
    //    VARIABLES     //
    //////////////////////
    
    //max speed of the missile after modifiers.
    private final float MAX_SPEED;
    //Max range of the missile after modifiers.
    private final float DELAY;
    
    private final int  MAX_RANGE, SEARCH_CONE=360;
    private CombatEngineAPI engine;
    private final MissileAPI MISSILE;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    private IntervalUtil timer = new IntervalUtil(0.1f,0.15f);

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public Diableavionics_banishAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
        MAX_SPEED = 500*MISSILE.getSource().getMutableStats().getMissileMaxSpeedBonus().getBonusMult();
        MAX_RANGE = (int)missile.getWeapon().getRange();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            ECCM=1;
            DELAY=0.75f;
        } else {
            DELAY=1.5f;
        }
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (Global.getCombatEngine().isPaused() || MISSILE.isFading() || MISSILE.isFizzling()) {return;}
        
        //assigning a target if there is none or it got destroyed
        if (target == null
                || target.getOwner()==MISSILE.getOwner()
                || (TARGET_SWITCH && (target instanceof ShipAPI && ((ShipAPI) target).isHulk())
                                  || !engine.isEntityInPlay(target)
                   )
                ){
            setTarget(MagicTargeting.pickTarget(MISSILE, MagicTargeting.targetSeeking.NO_RANDOM, MAX_RANGE, SEARCH_CONE, 0,0,1,1,1, false));
            //forced acceleration by default
            MISSILE.giveCommand(ShipCommand.DECELERATE); 
            timer.forceIntervalElapsed();
            return;
        }
        
        
        timer.advance(amount);
        //finding lead point to aim to
        if(timer.intervalElapsed()){
            //best intercepting point
            lead = AIUtils.getBestInterceptPoint(
                    MISSILE.getLocation(),
                    MAX_SPEED*ECCM, //if eccm is intalled the point is accurate, otherwise it's placed closer to the target (almost tailchasing)
                    target.getLocation(),
                    target.getVelocity()
            );                
            //null pointer protection
            if (lead == null) {
                lead = target.getLocation(); 
            }
        }        
        
        if(MISSILE.getElapsed()>DELAY && Math.abs(MathUtils.getShortestRotation(MISSILE.getFacing(), VectorUtils.getAngle(MISSILE.getLocation(), lead)))<1){
            
            engine.spawnProjectile(MISSILE.getSource(), MISSILE.getWeapon(), "diableavionics_banishWarhead", MISSILE.getLocation(), MISSILE.getFacing(), MISSILE.getVelocity());
            engine.addHitParticle(MISSILE.getLocation(), MISSILE.getVelocity(), 50, 0.5f, 0.5f, Color.cyan);
            engine.addHitParticle(MISSILE.getLocation(), MISSILE.getVelocity(), 100, 0.5f, 0.1f, Color.white);
            Global.getSoundPlayer().playSound("diableavionics_banish_stage", 1, 1, MISSILE.getLocation(), MISSILE.getVelocity());
            engine.removeEntity(MISSILE);
            
//            engine.applyDamage(MISSILE, MISSILE.getLocation(), MISSILE.getHitpoints()*2, DamageType.FRAGMENTATION,0,true,false,MISSILE.getSource());
            return;
        } else {            
            MISSILE.giveCommand(ShipCommand.DECELERATE); 
        }
        
        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                        MISSILE.getLocation(),
                        lead
                );
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( MISSILE.getFacing(), correctAngle);
         
        
        if (aimAngle < 0) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
        }
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(MISSILE.getAngularVelocity()) * DAMPING) {
            MISSILE.setAngularVelocity(aimAngle / DAMPING);            
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