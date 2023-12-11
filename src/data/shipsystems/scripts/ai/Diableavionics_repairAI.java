package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.weapons.Diableavionics_repairs;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_repairAI implements ShipSystemAIScript {
    
    private boolean runOnce=true, disabled=false;
    private ShipAPI ship, armor;
    private ShipSystemAPI system;
    private Diableavionics_repairs script;
    private final IntervalUtil TICK = new IntervalUtil (1.5f,2.5f);
    private final float RANGE=1000;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){
        
        
        if(runOnce){
            if(!ship.getChildModulesCopy().isEmpty()){
                armor = ship.getChildModulesCopy().get(0);
                for(WeaponAPI w : ship.getAllWeapons()){
                    if(w.getSlot().getId().equals("SYSTEM")){
                        script = (Diableavionics_repairs) w.getEffectPlugin();
                    }
                }
                
                runOnce=false;
            } 
            return;
        }
        
        if(!ship.isAlive() || armor==null || script==null || disabled) return;
        
        TICK.advance(amount);
        if(TICK.intervalElapsed()){
            
            //alive check
            if(!armor.isAlive()){
                disabled=true;
                return;
            }
            
            float repair = script.getRepairable();
            
            if(!system.isActive()){
                if(repair>200 && AIUtils.getNearbyEnemies(ship, RANGE).size()<(repair/100)){
                    if(AIUtils.canUseSystemThisFrame(ship)){
                        ship.useSystem();
                    }
                }
            } else {
                if(AIUtils.getNearbyEnemies(ship, RANGE).size()>(repair/50)){
                    if(AIUtils.canUseSystemThisFrame(ship)){
                        ship.useSystem();
                    }
                }
            }
        }
    }
}