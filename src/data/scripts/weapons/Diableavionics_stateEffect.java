package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;

public class Diableavionics_stateEffect implements BeamEffectPlugin {
    
    private boolean runOnce=false;
    private float time=0, offsetA=0, offsetB=0;
    WeaponSpecAPI specs;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        
        if(engine.isPaused() || beam.getWeapon().getShip().getOriginalOwner()==-1){return;}
        if(!runOnce){
            runOnce=true;
            beam.getWeapon().ensureClonedSpec();
            specs=beam.getWeapon().getSpec();
            offsetA=MathUtils.getRandomNumberInRange(0, 100);
            offsetB=MathUtils.getRandomNumberInRange(0, 100);
        }
        
        time+=amount*2;
        
        float A = (float)(FastTrig.sin((time+offsetA)*1.1f)/2+FastTrig.sin((time+offsetA)*2.9)/3);
        float B = (float)(FastTrig.sin((time+offsetB)*1.3f)/2+FastTrig.sin((time+offsetB)*2.6)/3);
        
        specs.getHardpointAngleOffsets().set(0, A);
        specs.getHardpointAngleOffsets().set(1, B);
        
        specs.getTurretAngleOffsets().set(0, A);
        specs.getTurretAngleOffsets().set(1, B);
        
        specs.getHiddenAngleOffsets().set(0, A);
        specs.getHiddenAngleOffsets().set(1, B);
    }
}
