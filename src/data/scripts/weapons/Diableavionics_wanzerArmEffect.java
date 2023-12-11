package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.magiclib.util.MagicAnim;
import org.lazywizard.lazylib.MathUtils;

/**
 *
 * @author Tartiflette
 */
public class Diableavionics_wanzerArmEffect implements EveryFrameWeaponEffectPlugin{    

    private boolean runOnce=false;
    private ShipAPI ship;
    private SpriteAPI arm;
    private float overlap=0, CENTER, OFFSET=0, ARC=0;
    private WeaponAPI shoulder;
    private final float MAX_OVERLAP=4;
    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            arm=weapon.getSprite();
            CENTER=arm.getCenterY();
            for(WeaponAPI w : ship.getAllWeapons()){
                if(w.getSlot().getId().equals("SHOULDER")){
                    shoulder=w;
                    OFFSET=w.getSlot().getAngle();
                    ARC=w.getSlot().getArc();
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
        
        arm.setCenterY(CENTER+overlap);
        
        float aim = MathUtils.getShortestRotation(ship.getFacing(), shoulder.getCurrAngle());
        
        aim-=OFFSET;        
        aim=(aim+(ARC/2))/ARC;
        aim=MagicAnim.smoothNormalizeRange(aim,0,1);
        aim=(aim*ARC)-ARC/2;        
        aim+=OFFSET;
        
        weapon.setCurrAngle(ship.getFacing()+aim);        
    }
}
