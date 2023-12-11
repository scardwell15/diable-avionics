package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_quantumimpulseAI implements ShipSystemAIScript
{
    private ShipAPI ship;
    private ShipSystemAPI system;
    private float timer=0;
    private final float TICK=0.25f;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
    {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){
        timer+=amount;
        if(timer>TICK){
            timer-=TICK;
            if(AIUtils.getNearbyEnemyMissiles(ship, 1500).size()>3 && !system.isActive() && AIUtils.canUseSystemThisFrame(ship)){
                ship.useSystem();
            }        
        }
    }
}
