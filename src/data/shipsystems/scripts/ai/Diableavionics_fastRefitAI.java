package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_fastRefitAI implements ShipSystemAIScript{    
    
    private Logger log = Global.getLogger(Diableavionics_fastRefitAI.class);    
    
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
        if(engine.isPaused()||system.isActive()||ship.getFullTimeDeployed()<5){
            return;
        }
        
        timer+=amount;
        if(timer>TICK){
            timer=0;
            
            int busyBays=0;
            for(FighterLaunchBayAPI b : ship.getLaunchBaysCopy()){
                if(b.getWing()!=null){
                    if(b.getWing().getWingMembers().size()<b.getWing().getSpec().getNumFighters()){
                        busyBays++;
                    }
                }
            }            
            if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship) && busyBays>=ship.getAllWings().size()){
                ship.useSystem();
                for(FighterLaunchBayAPI b : ship.getLaunchBaysCopy()){
                    if(b.getWing()!=null){
                        float time = b.getWing().getSpec().getRefitTime();
                    }
                }
            }
        }
    }
}