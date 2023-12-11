package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_transit2Stats extends BaseShipSystemScript {
   
    private final Integer SPEED_BONUS=130;
    private final Integer ACCEL_BONUS=100;
    private final float DECEL_MALUS=0.9f;
    private final float TURN_MALUS=0.5f;
    private final float DAMAGE_REDUCTION=0.5f;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id);
        } else {
            stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
            stats.getAcceleration().modifyPercent(id, ACCEL_BONUS* effectLevel);
            stats.getDeceleration().modifyMult(id, 1 - DECEL_MALUS * effectLevel);
            stats.getTurnAcceleration().modifyMult(id, 1-TURN_MALUS * effectLevel);
            stats.getMaxTurnRate().modifyMult(id, 1-TURN_MALUS * effectLevel);
            stats.getArmorDamageTakenMult().modifyMult(id, 1-DAMAGE_REDUCTION * effectLevel);
            stats.getHullDamageTakenMult().modifyMult(id, 1-DAMAGE_REDUCTION * effectLevel);
        }
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);        
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
    }
    
    private final String TXT1 = txt("transit2");
    private final String TXT2 = txt("transit3");
    private final String TXT3 = txt("%");
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        int speed = Math.round(SPEED_BONUS*effectLevel);
        int dmg = Math.round(100*DAMAGE_REDUCTION*effectLevel);
        
        if (index == 0) {
                return new StatusData(TXT1+speed, false);
        }
        if (index == 1) {
                return new StatusData(TXT2+dmg+TXT3, false);
        }
        return null;
    }
}
