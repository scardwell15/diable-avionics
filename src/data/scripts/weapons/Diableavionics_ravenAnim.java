package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class Diableavionics_ravenAnim implements EveryFrameWeaponEffectPlugin {
        
    private boolean runOnce=false, hidden=false;
    private SpriteAPI barrel;
    private float barrelwidth=0, recoil=0;
    private final float maxRecoil=5;
    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || hidden || weapon.getShip().getOriginalOwner()==-1){return;}
        
        if(!runOnce){
            runOnce=true;
            if(weapon.getSlot().isHidden()){
                hidden=true;
                return;
            } else {
                barrel=weapon.getBarrelSpriteAPI();
                barrelwidth=barrel.getWidth()/2;            
                return;
            }
        }
        if(weapon.getChargeLevel()==1){
            recoil=1;
        } else {
            recoil=Math.max(0, recoil-(amount));
        }
        barrel.setCenterX(barrelwidth-(recoil*maxRecoil));        
    }
}