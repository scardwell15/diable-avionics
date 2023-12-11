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
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicTargeting;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_SrabAI implements MissileAIPlugin, GuidedMissileAI {
    //////////////////////
    //     SETTINGS     //
    //////////////////////
    
    //Damping of the turn speed when closing on the desired aim. The smaller the snappier.
    private final float DAMPING=0.1f;    
    
    //max speed of the missile after modifiers.
    private final float MAX_SPEED;
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    private boolean launch=true;
    private IntervalUtil timer= new IntervalUtil(0.2f,0.3f);

    public Diableavionics_SrabAI(MissileAPI missile, ShipAPI launchingShip) {	
	
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }        
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed();
        missile.setCollisionClass(CollisionClass.MISSILE_NO_FF);        

    }

    @Override
    public void advance(float amount) {
        
        if(missile.isFizzling() || missile.isFading()){
            if(MagicRender.screenCheck(0.25f, missile.getLocation())){
                engine.addSmoothParticle(missile.getLocation(), missile.getVelocity(), 100, 0.5f, 0.25f, Color.blue);
                engine.addHitParticle(missile.getLocation(), missile.getVelocity(), 100, 1f, 0.1f, Color.white);
            }
            engine.removeEntity(missile);
            return;
        }
        
        missile.giveCommand(ShipCommand.ACCELERATE);        
        
        if(launch){            
            setTarget(MagicTargeting.pickTarget(missile,MagicTargeting.targetSeeking.NO_RANDOM,(int)missile.getWeapon().getRange(),90,0,1,1,1,1,false));
            timer.forceIntervalElapsed();
            launch=false;
        }
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused() 
                || target == null 
                || (target instanceof ShipAPI && !((ShipAPI)target).isAlive()) 
                || !engine.isEntityInPlay(target) ){
            return;
        }
        
        
        timer.advance(amount);
        //finding lead point to aim to        
        if(timer.intervalElapsed()){
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
        }
        
        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                        missile.getLocation(),
                        lead
                );
        
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