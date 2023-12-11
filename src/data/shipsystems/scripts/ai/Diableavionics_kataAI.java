package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_kataAI implements ShipSystemAIScript {
    
    private ShipAPI ship;
    private ShipSystemAPI system;
    private final IntervalUtil TICK = new IntervalUtil (1.5f,2.5f);
    private final float RANGE=450;

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
                int nearby=0;
                nearby+=AIUtils.getNearbyEnemyMissiles(ship, RANGE).size();
                nearby+=AIUtils.getNearbyEnemies(ship, RANGE).size()*2;
                if(nearby>10){
                    ship.useSystem();
                }
            }
        }
    }
}