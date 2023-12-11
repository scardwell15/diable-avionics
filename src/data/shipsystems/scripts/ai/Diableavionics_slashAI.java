package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_slashAI implements ShipSystemAIScript {
    
    private ShipAPI ship;
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
            if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship)){
                ShipAPI theTarget;
                if(ship.getShipTarget()!=null && ship.getShipTarget().getOwner()!=ship.getShipTarget().getOwner()){
                    theTarget = target;
                } else {
                    theTarget = AIUtils.getNearestEnemy(ship);
                }                
                if(theTarget!=null && !theTarget.isFighter() && !theTarget.isDrone() && MathUtils.isWithinRange(ship, theTarget, 100)){
                    ship.setShipTarget(theTarget);
                    ship.useSystem();
                }
            }
        }
    }
}