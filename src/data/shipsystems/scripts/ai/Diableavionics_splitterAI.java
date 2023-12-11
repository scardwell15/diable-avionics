package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import org.lazywizard.lazylib.MathUtils;
//import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_splitterAI implements ShipSystemAIScript {
    
    private ShipAPI ship, theTarget;
    private ShipSystemAPI system;
    private float timer=0;
    private final float TICK=2f;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        timer=(float)Math.random()*2;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){
        timer+=amount;
        if(timer>TICK){
            timer=0;
            if(!system.isActive() ){
                if(ship.getShipTarget()!=null && AIUtils.canUseSystemThisFrame(ship)){
                    theTarget = ship.getShipTarget(); 
                    if(
                            !theTarget.isDrone() 
                            && 
                            MathUtils.isWithinRange(ship.getLocation(), theTarget.getLocation(), 600)
                            ){
                        if(theTarget.getShield()==null){ 
                            if(!theTarget.isFighter()) ship.useSystem();
                        } else if(theTarget.getShield().isOff() || theTarget.isFrigate() || theTarget.isFighter()){                                                                        
                            ship.useSystem();
                        }
                    }
                }
            }
        }
    }
}