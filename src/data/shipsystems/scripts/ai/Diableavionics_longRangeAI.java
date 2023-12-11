package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_longRangeAI implements ShipSystemAIScript{    
        
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private float timer=0;
    private final float TICK=1f;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine){
        this.ship = ship;
        this.system = system;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){
        if(engine.isPaused()||system.isActive()){
            return;
        }
        
        timer+=amount;
        if(timer>TICK){
            timer=0;
            if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship) && !AIUtils.getNearbyEnemies(ship, 8000).isEmpty() && AIUtils.getNearbyEnemies(ship, 4000).isEmpty()){
                ship.useSystem();
            }
        }
    }
}