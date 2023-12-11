package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_boosterAI implements ShipSystemAIScript{    
    
    private Logger log = Global.getLogger(Diableavionics_boosterAI.class);    
    
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private WeaponAPI booster;
    private float timer=0;
    private final float TICK=0.25f;
    private boolean OFF=false;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine){
        this.ship = ship;
        this.system = system;
        this.engine = engine;
        for(WeaponAPI w : ship.getAllWeapons()){
            if(w.getSlot().getId().equals("WS0002")){                
                this.booster = w;
            }
        }
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){
        if(engine.isPaused()||OFF){
            return;
        }
        timer+=amount;
        if(timer>TICK){
            timer=0;
            if(booster.getAmmo()>0){
//                log.info("Booster present");
                if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship)){                    
//                    log.info("Booster ON");
                    ship.useSystem();
                }
            } else {
//                log.info("Booster absent");
                if(system.isOn() && AIUtils.canUseSystemThisFrame(ship)){  
//                    log.info("Booster OFF");              
                    ship.useSystem();
                    OFF=true; 
                }
            }
        }
    }
}