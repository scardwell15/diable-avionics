package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_catalystAI implements ShipSystemAIScript {
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private IntervalUtil timer = new IntervalUtil (1,2);
    

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
        
        timer.advance(amount);
        
        if (timer.intervalElapsed()) {
            boolean desired_state=false;
            
            if (AIUtils.getNearestEnemy(ship)==null || !MathUtils.isWithinRange(ship, AIUtils.getNearestEnemy(ship),3000)){
                desired_state=true;
            }
            
            if(ship.isRetreating()){
                desired_state=true;
            }
            
            if(ship.getFluxTracker().getFluxLevel()>0.9){
                desired_state=false;
            }
            boolean on=system.isOn();
            
            if(AIUtils.canUseSystemThisFrame(ship)|| on){
                if(on!=desired_state){
                    ship.useSystem();
                }
            }
        }
    }
}
