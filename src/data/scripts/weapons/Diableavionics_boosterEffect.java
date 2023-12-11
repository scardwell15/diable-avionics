
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class Diableavionics_boosterEffect implements EveryFrameWeaponEffectPlugin {

    private boolean activated=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    
        if(engine.isPaused()) return;
        
        if(!activated){
            //force system deactivation after drop
            if(weapon.getAmmo()<1 && weapon.getShip().getSystem().isActive()){
                weapon.getShip().getSystem().deactivate();
                weapon.getShip().getSystem().setAmmo(0);
                weapon.disable(true);
                activated=true;
            }
            
            //force drop is system deactivation
            if(weapon.getShip().getSystem().getAmmo()==0 && !weapon.getShip().getSystem().isActive()){
                weapon.setAmmo(0);
                engine.spawnProjectile(weapon.getShip(),
                        weapon,
                        weapon.getId(),
                        weapon.getLocation(),
                        weapon.getCurrAngle(),
                        weapon.getShip().getVelocity()
                );
                weapon.disable(true);
                activated=true;
            }
        }
    }
}