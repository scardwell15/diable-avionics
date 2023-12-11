package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

/**
 *
 * @author Tartiflette
 */
public class Diableavionics_tuskAnim implements EveryFrameWeaponEffectPlugin{    

    private boolean runOnce=false;
    private AnimationAPI anim;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (engine.isPaused()) {
            return;
        }

        if(!runOnce){
            runOnce=true;
            anim = weapon.getAnimation();
        }
        
        if (anim!=null && (weapon.getCooldownRemaining()>0 || anim.getFrame()!=0)) {            
            int currentFrame;
            float cool = weapon.getCooldownRemaining()/weapon.getCooldown();
            
            if (cool==0f){
                currentFrame=0;                
            } else {
                if (cool>=0.98f){
                    currentFrame=1;
                } else if (cool<=0.25f){
                currentFrame=Math.max(2, Math.min(15, Math.round(-75*(cool-0.25f))));
                if (currentFrame==15) currentFrame=0;
                } else {
                    currentFrame=2;
                }
            }
            anim.setFrame(currentFrame);
        }
    }            
}