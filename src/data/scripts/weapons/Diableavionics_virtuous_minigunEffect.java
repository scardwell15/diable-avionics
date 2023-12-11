//By Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

public class Diableavionics_virtuous_minigunEffect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin{
        
    private float delay = 0.1f;
    private float timer = 0;
    private float SPINUP = 0.02f;
    private float SPINDOWN = 10f;
    
    private boolean runOnce=false;
    private boolean sound=false;
    private boolean hidden=false;
    private AnimationAPI theAnim;
    private int maxFrame;
    private int frame;
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused()){return;}
        
        if(!runOnce){
            runOnce=true;
            if(weapon.getSlot().isHidden()){
                hidden=true;
            } else {
                theAnim=weapon.getAnimation();
                maxFrame=theAnim.getNumFrames();
                frame=MathUtils.getRandomNumberInRange(0, maxFrame-1);
            }               
            SPINUP=5f;
            SPINDOWN=10f;
        }
        
        if(!hidden){
            timer+=amount;
            if (timer >= delay){
                
                int frames = (int)(timer/delay);
                
                timer-=frames*delay;
                if (weapon.getChargeLevel()>0){
                    delay = Math.max(
                                delay - delay/SPINUP,
                                0.01f
                            );
                } else {
                    delay = Math.min(
                                delay + delay/SPINDOWN,
                                0.1f
                            );
                }
                if (delay!=0.1f){
                    frame+=frames;
                    if (frame>=maxFrame){
                        frame=Math.min(maxFrame-1,frame-maxFrame);
                    }
                }
            }
        }
        
        if (!hidden){            
            theAnim.setFrame(frame);
        }
        
        //play the spinning sound
        if (weapon.getChargeLevel()>0){
            Global.getSoundPlayer().playLoop(
                    "diableavionics_opfer_spin",
                    weapon,
                    0.25f+Math.min(1.75f, 2f*weapon.getChargeLevel()),
                    0.15f,
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );            
        } 
        //trailing shot
        if(weapon.getChargeLevel()==1){
            sound=true;
        } else if(sound){
            sound=false;
            Global.getSoundPlayer().playSound(
                    "diableavionics_virtuousMinigun_trail",
                    1,
                    2f,
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );
        }
    }
    
    
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
//        if(Math.random()<0.5){
            engine.addNebulaParticle(
                    projectile.getLocation(),
                    MathUtils.getRandomPointInCone(weapon.getShip().getVelocity(),MathUtils.getRandomNumberInRange(25, 75), projectile.getFacing()-15f, projectile.getFacing()+15f),
                    MathUtils.getRandomNumberInRange(15, 30),
                    MathUtils.getRandomNumberInRange(1.1f, 1.5f),
                    0,
                    0.1f,
                    MathUtils.getRandomNumberInRange(0.05f, 1f),
                    new Color(150,125,100,100),
                    true
            );
            engine.addHitParticle(
                    projectile.getLocation(),
                    weapon.getShip().getVelocity(),
                    MathUtils.getRandomNumberInRange(25, 75),
                    1f,
                    0.04f,
                    Color.WHITE
            );
            engine.addHitParticle(
                    projectile.getLocation(),
                    weapon.getShip().getVelocity(),
                    MathUtils.getRandomNumberInRange(50, 100),
                    0.5f,
                    0.1f,
                    new Color(199,93,76,150)
            );
//        }
    }
}
