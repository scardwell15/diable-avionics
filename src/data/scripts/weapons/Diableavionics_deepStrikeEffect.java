package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Diableavionics_deepStrikeEffect implements EveryFrameWeaponEffectPlugin {
	
    private boolean runOnce=false;
    private final IntervalUtil timer = new IntervalUtil(0.4f,0.6f);
    private static final List<ShipAPI> DEEP_STRIKE_MEMBERS = new ArrayList<>();
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            DEEP_STRIKE_MEMBERS.clear();
        }
        
        if(weapon.getCooldownRemaining()==0){
            timer.advance(amount);
            if(timer.intervalElapsed()){
                if(DEEP_STRIKE_MEMBERS.isEmpty()){
                    //replenish one ammo if no deep strike team is active
                    if(weapon.getAmmo()<1){
                        weapon.setAmmo(1);
                    }
                } else {
                    //if there is a team registered, check its status and remove it accordingly
                    for(Iterator<ShipAPI> iter = DEEP_STRIKE_MEMBERS.iterator(); iter.hasNext();){
                        ShipAPI check = (ShipAPI)iter.next();
                        if(!engine.isEntityInPlay(check) || !check.isAlive()){
                            iter.remove();
                        }
                    }
                }
            }
        }
    }
    
    public static void NewDeepStrikeMembers( List<ShipAPI> members){
        for(ShipAPI m : members){
            DEEP_STRIKE_MEMBERS.add(m);
        }
    }
    public static void NewDeepStrikePod( ShipAPI member){
        DEEP_STRIKE_MEMBERS.add(member);
    }
}
