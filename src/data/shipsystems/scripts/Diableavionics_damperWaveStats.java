package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import static data.scripts.util.Diableavionics_stringsManager.txt;
import java.awt.Color;

public class Diableavionics_damperWaveStats extends BaseShipSystemScript {

    private final Integer TURN_ACC_BUFF = 1000;
    private final Integer TURN_RATE_BUFF = 100;
    private final Integer ACCEL_BUFF = 5000;
    private final Integer DECCEL_BUFF = 5000;
    private final Integer SPEED_BUFF = 500;
    private final Float DAMAGE_RESISTANCE = 0.33f;
    
    private final IntervalUtil tick = new IntervalUtil(0.1f,0.1f);
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        
        switch (state) {
            case IN:
                //mobility boost
                stats.getMaxSpeed().modifyPercent(id, effectLevel*SPEED_BUFF);
                stats.getAcceleration().modifyPercent(id, ACCEL_BUFF);
                stats.getDeceleration().modifyPercent(id, DECCEL_BUFF);
                
                stats.getMaxTurnRate().modifyPercent(id, effectLevel*TURN_RATE_BUFF);
                stats.getTurnAcceleration().modifyPercent(id, TURN_ACC_BUFF);
                
                //damage reduction
                stats.getArmorDamageTakenMult().modifyMult(id, DAMAGE_RESISTANCE);
                stats.getHullDamageTakenMult().modifyMult(id, DAMAGE_RESISTANCE);
                stats.getEmpDamageTakenMult().modifyMult(id, DAMAGE_RESISTANCE);
                break;
                
            case ACTIVE:
                //mobility boost
                stats.getMaxSpeed().modifyPercent(id, SPEED_BUFF);
                stats.getAcceleration().modifyPercent(id, 0);
                stats.getDeceleration().modifyPercent(id, 0);
                stats.getAcceleration().modifyMult(id, 0);
                stats.getDeceleration().modifyMult(id, 0);
                
                stats.getMaxTurnRate().modifyPercent(id, TURN_RATE_BUFF);
                stats.getTurnAcceleration().modifyPercent(id, 0);                
                //damage reduction
                stats.getArmorDamageTakenMult().modifyMult(id, DAMAGE_RESISTANCE);
                stats.getHullDamageTakenMult().modifyMult(id, DAMAGE_RESISTANCE);
                stats.getEmpDamageTakenMult().modifyMult(id, DAMAGE_RESISTANCE);
                break;
                
            case OUT:
                //mobility boost
                stats.getMaxSpeed().modifyPercent(id, effectLevel*SPEED_BUFF);
                stats.getAcceleration().modifyPercent(id, 0);
                stats.getDeceleration().modifyPercent(id, 0);
                stats.getAcceleration().modifyMult(id, 1-effectLevel);
                stats.getDeceleration().modifyMult(id, 1-effectLevel);
                
                stats.getMaxTurnRate().modifyPercent(id, effectLevel*TURN_RATE_BUFF);
                stats.getTurnAcceleration().modifyPercent(id, 0);     
                
                //damage reduction
                stats.getArmorDamageTakenMult().modifyMult(id, DAMAGE_RESISTANCE+(1-DAMAGE_RESISTANCE)*(1-effectLevel));
                stats.getHullDamageTakenMult().modifyMult(id, DAMAGE_RESISTANCE+(1-DAMAGE_RESISTANCE)*(1-effectLevel));
                stats.getEmpDamageTakenMult().modifyMult(id, DAMAGE_RESISTANCE+(1-DAMAGE_RESISTANCE)*(1-effectLevel));
                break;
        }
        
        //visual trail
        if(!Global.getCombatEngine().isPaused()){
            tick.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if(tick.intervalElapsed()){
                ShipAPI ship = (ShipAPI) stats.getEntity();
                if(ship!=null){
                    ship.addAfterimage(new Color(60,255,150,64), 0, 0, -ship.getVelocity().x, -ship.getVelocity().y, 0, 0, 0, effectLevel, false, false, false);
                }
            }
        }
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
    }
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(txt("damper0"), false);
        }
        if (index == 1) {
            return new StatusData(txt("damper1") + Math.round((1-DAMAGE_RESISTANCE)*100*effectLevel)+ txt("%"), false);
        }
        return null;
    }
}