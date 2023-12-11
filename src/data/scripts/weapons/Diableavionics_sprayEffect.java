package data.scripts.weapons;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.FastTrig;

/**
 *
 * @author Tartiflette
 */

public class Diableavionics_sprayEffect implements EveryFrameWeaponEffectPlugin{    

    private float time=0;
    private int mult=1;
    private final int FREQUENCY=10, MAX_OFFSET=2;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (engine.isPaused()) {
            return;
        }

        if(weapon.isFiring()){
            time+=amount*FREQUENCY;
            
            for(int i=0; i<weapon.getSpec().getTurretAngleOffsets().size(); i++){            
                weapon.getSpec().getHardpointAngleOffsets().set(i, MAX_OFFSET*mult+mult*MAX_OFFSET*(float)FastTrig.cos(time));            
                weapon.getSpec().getTurretAngleOffsets().set(i, MAX_OFFSET*mult+mult*MAX_OFFSET*(float)FastTrig.cos(time));            
                weapon.getSpec().getHiddenAngleOffsets().set(i, MAX_OFFSET*mult+mult*MAX_OFFSET*(float)FastTrig.cos(time));  
                mult*=-1;
            }            
        }
    }            
}
