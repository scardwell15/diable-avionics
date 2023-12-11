package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
//import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;
import org.magiclib.util.MagicAnim;
import java.awt.Color;

public class Diableavionics_driftStats extends BaseShipSystemScript {

    private final Integer TURN_ACC_BUFF = 1000;
    private final Integer TURN_RATE_BUFF = 500;
    private final Integer ACCEL_BUFF = 500;
    private final Integer DECCEL_BUFF = 300;
    private final Integer SPEED_BUFF = 200;
    private final Integer TIME_BUFF = 1000;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        
        float effect = Math.min(1, Math.max(0, MagicAnim.smoothReturnNormalizeRange(effectLevel, 0, 1)/2 + MagicAnim.smoothReturnNormalizeRange(effectLevel*1.5f, 0, 1)/2 + MagicAnim.smoothReturnNormalizeRange(effectLevel*2, 0, 1)/2));
        
        //visual effect
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if(ship!=null){
            ship.setJitterUnder(
                    ship, 
                    Color.CYAN,
                    0.5f*effect,
                    5, 
                    5+5f*effect, 
                    5+10f*effect
            );
            if(Math.random()>0.9f){
                ship.addAfterimage(new Color(0,200,255,64), 0, 0, -ship.getVelocity().x, -ship.getVelocity().y, 5+50*effect, 0, 0, 2*effect, false, false, false);
            }  
            
            if(!stats.getTimeMult().getPercentMods().containsKey(id)){
                Global.getSoundPlayer().playSound("diableavionics_drift", 1, 1.66f, ship.getLocation(), ship.getVelocity());
                
                
                //protection against burst weapons abuse
                ship.setPhased(true);
//                for (WeaponAPI w : ship.getAllWeapons()){
//                    if(w.getChargeLevel()==1){
//                        w.setRemainingCooldownTo(w.getCooldown());
//                    }
//                }
            } else if(ship.isPhased()){
                ship.setPhased(false);
            }
        }
        
        //ship can reorient
        stats.getTurnAcceleration().modifyPercent(id, TURN_ACC_BUFF * effect);
        stats.getMaxTurnRate().modifyPercent(id, TURN_RATE_BUFF * effect);
        
        //ship can slightly jump forward
        stats.getMaxSpeed().modifyPercent(id, SPEED_BUFF * effect);
        stats.getAcceleration().modifyPercent(id, ACCEL_BUFF);
        stats.getDeceleration().modifyPercent(id, DECCEL_BUFF);
        
        //time drift
        stats.getTimeMult().modifyPercent(id, TIME_BUFF * effect);
        
        
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        
        stats.getTimeMult().unmodify(id);
    }
    
    private final String TXT = txt("drift");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(TXT, false);
        }
        return null;
    }
}