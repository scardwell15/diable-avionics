package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class Diableavionics_artassaultFire implements EveryFrameWeaponEffectPlugin {
        
    private boolean runOnce=false, hidden=false;
    private SpriteAPI barrel;
    private float barrelHeight=0, recoil=0;
    private final float maxRecoil=-4;
    CombatEntityAPI soundSource;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1){return;}
        if(!runOnce){
            runOnce=true;
            if(weapon.getSlot().isHidden()){
                hidden=true;
            } else {
                barrel=weapon.getBarrelSpriteAPI();
                if(weapon.getSlot().isTurret()){
                    barrelHeight=barrel.getHeight()/2;
                } else {                    
                    barrelHeight=barrel.getHeight()/4;
                }
            }
            return;
        }
        if(!hidden){
            if(weapon.getChargeLevel()==1){
                recoil=Math.min(1, recoil+amount*2);
            } else {
                recoil=Math.max(0, recoil-(0.5f*amount));
            }
            barrel.setCenterY(barrelHeight-(recoil*maxRecoil));
        }
    }
}