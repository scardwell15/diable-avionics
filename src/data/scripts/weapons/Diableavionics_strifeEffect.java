package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicAnim;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

/**
 *
 * @author Tartiflette
 */
public class Diableavionics_strifeEffect implements EveryFrameWeaponEffectPlugin{    

    private boolean runOnce=false, lockNloaded=false;
    private ShipSystemAPI system;
    private ShipAPI ship;
    private AnimationAPI anim, aGlow;
    private WeaponAPI armL, armR, pauldronL, pauldronR, torso, wGlow;
    
    private float delay = 0.1f;
    private float timer = 0;
    private float SPINUP = 0.02f;
    private float SPINDOWN = 10f;
    private int maxFrame, frame;
    
    private float overlap=0, heat=0;
    private final float TORSO_OFFSET=-45, LEFT_ARM_OFFSET=-75, RIGHT_ARM_OFFSET=-25, MAX_OVERLAP=10;
    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system = ship.getSystem();
            anim=weapon.getAnimation();
            maxFrame=anim.getNumFrames();
            frame=MathUtils.getRandomNumberInRange(0, maxFrame-1);
            for(WeaponAPI w : ship.getAllWeapons()){
                switch (w.getSlot().getId()){
                    case "B_TORSO" :
                        torso=w;
                        break;
                    case "C_ARML" :
                        armL=w;
                        break;
                    case "C_ARMR" :
                        armR=w;
                        break;
                    case "D_PAULDRONL" :
                        pauldronL=w;
                        break;
                    case "D_PAULDRONR" :
                        pauldronR=w;
                        break;
                    case "F_GLOW" :
                        wGlow=w;
                        aGlow=w.getAnimation();
                        break;
                }
            }
        }
        
        if (engine.isPaused()) {
            return;
        }
        
        if(ship.getEngineController().isAccelerating()){
            if(overlap>(MAX_OVERLAP-0.1f)){
                overlap=MAX_OVERLAP;
            } else {
                overlap=Math.min(MAX_OVERLAP, overlap +((MAX_OVERLAP-overlap)*amount*5));
            }
        } else if(ship.getEngineController().isDecelerating()|| ship.getEngineController().isAcceleratingBackwards()){         
            if(overlap<-(MAX_OVERLAP-0.1f)){
                overlap=-MAX_OVERLAP;
            } else {   
                overlap=Math.max(-MAX_OVERLAP, overlap +((-MAX_OVERLAP+overlap)*amount*5));
            }
        } else {
            if(Math.abs(overlap)<0.1f){
                overlap=0;   
            }else{
                overlap-=(overlap/2)*amount*3;   
            }
        }
        
        float sineA=0, sinceB=0;   
        if(system.isActive()){
            if(system.getEffectLevel()<1){
                if(!lockNloaded){
                    lockNloaded=true;
                    weapon.setAmmo(weapon.getAmmo()+100);
                    aGlow.setFrame(1);                    
                    wGlow.getSprite().setColor(Color.BLACK);
                    heat=0;
                }
                sineA=MagicAnim.smoothNormalizeRange(system.getEffectLevel(),0,0.7f);
                sinceB=MagicAnim.smoothNormalizeRange(system.getEffectLevel(),0.3f,1f);
            } else {                
                sineA =1;
                sinceB =1;
                if(weapon.isFiring()){
                    heat=Math.min(1, heat+amount*0.5f);
                } else {                    
                    heat=Math.max(0, heat-amount*0.33f);
                }
            }
        } else if(lockNloaded){
            lockNloaded=false;
            if(weapon.getAmmo()>weapon.getMaxAmmo()){
                weapon.setAmmo(weapon.getMaxAmmo());
            }          
        }        
        
        if(heat>0){
            wGlow.getSprite().setColor(new Color((float)heat,(float)heat,(float)heat,0.99f));
            if(!system.isActive()){
                heat-=amount*0.33f;
                if(heat<=0){
                    heat=0;
                    aGlow.setFrame(0);
                }
            }
        }
        
        
        float global=ship.getFacing();
        float aim=MathUtils.getShortestRotation(global, weapon.getCurrAngle());
        
        torso.setCurrAngle(global + sineA*TORSO_OFFSET + aim*0.3f);
        
        armR.setCurrAngle(weapon.getCurrAngle() + RIGHT_ARM_OFFSET);
        
        pauldronR.setCurrAngle(global + sineA*TORSO_OFFSET*0.5f + aim*0.75f + RIGHT_ARM_OFFSET*0.5f);
             
        armL.setCurrAngle(
                        global
                        +   
                        ((aim+LEFT_ARM_OFFSET)*sinceB)
                        +
                        ((overlap+aim*0.25f)*(1-sinceB))
        );
        
        pauldronL.setCurrAngle(torso.getCurrAngle()+MathUtils.getShortestRotation(torso.getCurrAngle(),armL.getCurrAngle())*0.6f);        
        
        wGlow.setCurrAngle(weapon.getCurrAngle());
        
        //ROTARY ANIMATION
        
        timer+=amount;
        if (timer >= delay){
            timer-=delay;
            if (weapon.getChargeLevel()>0){
                delay = Math.max(
                            delay - SPINUP,
                            0.02f
                        );
            } else {
                delay = Math.min(
                            delay + delay/SPINDOWN,
                            0.1f
                        );
            }
            if (delay!=0.1f){
                frame++;
                if (frame==maxFrame){
                    frame=0;
                }
            }
        }        
        anim.setFrame(frame);
    }
}
