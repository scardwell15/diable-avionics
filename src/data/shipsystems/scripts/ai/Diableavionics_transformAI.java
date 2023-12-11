package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_transformAI implements ShipSystemAIScript {
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private float nominalRange=0;
    private boolean runOnce = false;
    private final float checkAgain=0.25f;
    private float delay=0f, timer=0f;
    private final float foldRange=1.25f, unfoldRange=0.9f;
    

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {  
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        if (engine.isPaused() || ship.getShipAI()==null) {
            return;
        }        

        if(!runOnce){
            runOnce=true;
            //calculate the nominal range: the average range of all non missiles, non PD weapons
            List<WeaponAPI> weapons=ship.getAllWeapons();
            int i = 0;
            for (WeaponAPI w : weapons) {
                 if ((w.getType()==WeaponAPI.WeaponType.ENERGY || w.getType()==WeaponAPI.WeaponType.BALLISTIC || w.getType()!=WeaponAPI.WeaponType.MISSILE) && w.getRange()>200 && !w.hasAIHint(WeaponAPI.AIHints.PD)) {
                    nominalRange = nominalRange + w.getRange();
                    i++;
                }        
            }
            nominalRange = nominalRange/i;
            delay=(float)Math.random()/4+5;
        }
        
        timer+=amount;        
        
        if (timer>(delay+checkAgain)) {
            timer=0;
            
//            if (ship.getShipTarget()!=null){
//                float tspeed = ship.getShipTarget().getMutableStats().getMaxSpeed().getBaseValue();
//                float sspeed = ship.getMutableStats().getMaxSpeed().getBaseValue();
//                if (tspeed>1.5f*sspeed){                    
//                    foldRange=0.9f; 
//                    unfoldRange=0.75f;                  
//                } else if (tspeed>0.9f*sspeed){                    
//                    foldRange=1f; 
//                    unfoldRange=0.85f;
//                } else {                   
//                    foldRange=1.25f; 
//                    unfoldRange=1f;                    
//                }                
//            } else {
//                foldRange=1.25f; 
//                unfoldRange=0.9f;                
//            }
            
//            ShipAPI leader=ship;
            //transform in transit form when retreating, or the target is too far, or the ship's flux is too high, and not when landing or surrounded
//            if (ship.isFighter()){
//                leader=ship.getWing().getLeader();            
//            }
//            
//            if(leader!=ship && MathUtils.getDistanceSquared(leader, ship)<1000000){
//                if(ship.getSystem().isActive()!=leader.getSystem().isActive()){
//                    ship.useSystem();
//                    delay = 2f;
//                }                
//            } else {
                if (
                        !system.isActive() //the system is off
                        && !ship.isLanding() // and the fighter is in flight
                        && ( // and
                            ship.isRetreating() // the fighter is either retreating
                            || AIUtils.getNearbyEnemies(ship, nominalRange*foldRange).isEmpty() // or is alone
                            || (ship.getFluxTracker().getFluxLevel()>0.9f && Math.random()>0.8f) // or is caping it's flux, with a chance to fail miserably for balance purposes
                        )
                    ) {
                    // then the system should activate, but can it?
                    if (AIUtils.canUseSystemThisFrame(ship)){
                        //if the system can be activated, activate and set a minimum delay of 2 seconds before checking again
                        ship.useSystem();
                        delay = 2f;
                        return;
                    }
                }      

                if ( 
                        system.isActive() // the system is on
                        && !ship.isRetreating() // and the fighter is not retreating
                        && ( // and
                            ship.isLanding() // the fighter is either landing
                            || (!AIUtils.getNearbyEnemies(ship, nominalRange*unfoldRange).isEmpty() && ship.getFluxTracker().getFluxLevel()<0.75f) // or is near an enemy and has flux to spare
                        ) 
                    ) {
                    // then the system should be deactivated, and set a minimum delay of 2 seconds before checking again
                    ship.useSystem();
                    delay = 2f;
                    return;
                }
                //remove the delay
                delay = 0f;         
//            }
        }
    }
}
