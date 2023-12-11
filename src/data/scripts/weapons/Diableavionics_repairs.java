package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicUI;
import java.awt.Color;
import org.lazywizard.lazylib.combat.AIUtils;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_repairs implements EveryFrameWeaponEffectPlugin {
	
    private boolean runOnce=false, check=false, usable=true;
    private ShipSystemAPI system;
    private ShipAPI armor;
    private float MAX,repairable;
    private final IntervalUtil timer = new IntervalUtil(0.45f,0.55f);
    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!weapon.getShip().isAlive()) return;
        if(weapon.getShip().getOwner()==-1) return;
        
        if(!runOnce){
            if(!weapon.getShip().getChildModulesCopy().isEmpty()){
                armor=weapon.getShip().getChildModulesCopy().get(0);
                system=weapon.getShip().getSystem();
                MAX=armor.getHitpoints();
                runOnce=true;
            }
            return;
        }
        
        if(!usable || armor==null || !armor.isAlive()){
            usable=false;
            if(system.isActive()&&AIUtils.canUseSystemThisFrame(weapon.getShip()))weapon.getShip().useSystem();
            system.setAmmo(0);
            return;
        }
        
//            MagicUI.drawInterfaceStatusBar(
//                    weapon.getShip(), 
//                    armor.getHullLevel(), 
//                    Color.GREEN, 
//                    Color.GREEN,
//                    MAX/armor.getMaxHitpoints(),
//                    "Armor HP",
//                    999
//            );
        if(Global.getCombatEngine().getCombatUI()!=null){
            MagicUI.drawInterfaceStatusBar(
                    weapon.getShip(), 
                    armor.getHullLevel(), 
                    null, 
                    null,
                    1,
                    "ARMOR",
                    (int)(getRepairable())
            );
        }
        
        if (system.getEffectLevel()>0){
            if(!check){
                check=true;
                repairable=getRepairable();
            }
            repairable-=100*amount*system.getEffectLevel();
            if(repairable>0){
                armor.setHitpoints(armor.getHitpoints()+(100*amount*system.getEffectLevel()));
                armor.setJitterUnder(
                        engine,
                        Color.yellow, 
                        0.5f,
                        4, 
                        30*system.getEffectLevel()
                );
                armor.setJitter(
                        engine,
                        Color.yellow, 
                        0.1f,
                        2, 
                        30*system.getEffectLevel()
                );
                if(armor.getParentStation()==engine.getPlayerShip()){
                    engine.maintainStatusForPlayerShip("diableavionics_repair",
                            "graphics/icons/hullsys/temporal_shell.png",
                            txt("stm_repair0"),                            
                            txt("stm_repair1")+(int)repairable,
                            false
                    );
                }
                
            } else {
                if(system.isActive()) system.deactivate();
            }
        } else if(check){
            check=false;
            MAX=armor.getHitpoints();
        }
    }
    
    public float getRepairable(){
        float repair = (MAX-armor.getHitpoints())/2;
        return repair;
    }
    
    public boolean getUsable(){
        return usable;
    }
}
