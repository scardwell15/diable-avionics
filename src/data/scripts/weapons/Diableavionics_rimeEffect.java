package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;

public class Diableavionics_rimeEffect implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce=false;
    private ShipAPI theShip;
    private ShipSystemAPI theSystem;
    private final IntervalUtil timer=new IntervalUtil(0.25f,1f);
    private final float DANGER_RANGE=1000;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused()){
            return;
        }
        
        //System use a timed vanilla AI instead of a Toggle, these are the turn off conditions
        
        if(!runOnce){
            runOnce=true;
            theShip=weapon.getShip();
            theSystem=theShip.getSystem();
        }        
        
        if(theShip.getAIFlags()!=null){
            timer.advance(amount);
            if (!theSystem.isOn()){

                if (timer.intervalElapsed()){
                    if(
                            AIUtils.canUseSystemThisFrame(theShip) 
                            && (
                                theShip.isRetreating() 
                                || (
                                    AIUtils.getNearbyEnemies(theShip, DANGER_RANGE).isEmpty() 
                                    && AIUtils.getNearbyEnemyMissiles(theShip, DANGER_RANGE).isEmpty())) ){
                        theShip.useSystem();
                    }
                }
            } else if(!theShip.isRetreating()){
                if (timer.intervalElapsed()){

                    CombatEntityAPI closest = AIUtils.getNearestEnemy(theShip);
                    if(closest!=null && MathUtils.isWithinRange(closest, theShip, DANGER_RANGE*0.75f)){
                        theShip.useSystem();
                        return;
                    }

                    List <MissileAPI> missiles=AIUtils.getNearbyEnemyMissiles(theShip, DANGER_RANGE*0.75f);
                    if(!missiles.isEmpty()){
                        float damage=0;
                        for(MissileAPI m : missiles){
                            damage+=m.getDamageAmount();                    
                        }                    
                        if (damage>=750){
                            theShip.useSystem();
                        }
                    }
                }
            }
        }
    } 
}