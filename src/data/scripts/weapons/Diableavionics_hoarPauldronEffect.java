package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;

/**
 *
 * @author Tartiflette
 */
public class Diableavionics_hoarPauldronEffect implements EveryFrameWeaponEffectPlugin{    

    private boolean runOnce=false;
    private WeaponAPI reference;
    private ShipAPI ship;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            for(WeaponAPI w : weapon.getShip().getAllWeapons()){
                if(w!=weapon &&weapon.getSlot().getId().endsWith("L")&&w.getSlot().getId().endsWith("L")){
                    reference=w;
                    break;
                }
                
                if(w!=weapon &&weapon.getSlot().getId().endsWith("R")&&w.getSlot().getId().endsWith("R")){
                    reference=w;
                }
            }
        }
        
        if (engine.isPaused() || reference==null) {
            return;
        }
        
        weapon.setCurrAngle(ship.getFacing() + MathUtils.getShortestRotation(ship.getFacing(),reference.getCurrAngle())*0.6f);
    }
}
