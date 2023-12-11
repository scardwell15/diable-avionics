package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_dakkaAI implements ShipSystemAIScript {
    
    private ShipAPI ship;
    private ShipSystemAPI system;
    private final IntervalUtil TICK = new IntervalUtil (1.5f,2.5f);
    private final float RANGE=350;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){
        TICK.advance(amount);
        if(TICK.intervalElapsed()){
            if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship)){
                int suitable = 0;
                int nearby = 0;
                for(ShipAPI s : AIUtils.getNearbyEnemies(ship, RANGE)){
                    nearby++;
                    if(s.isFrigate()){
                        suitable++;
                    } else if(s.isDestroyer()||s.isCruiser()||s.isCapital()){
                        suitable=suitable+2;
                    }
                }
                if(nearby>5 || suitable>1){
                    ship.useSystem();
                }
            }
        }
    }
}