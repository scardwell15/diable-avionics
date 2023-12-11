package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_transitStats extends BaseShipSystemScript {

    private final Integer SPEED_BONUS=100;
    private final Integer ACCEL_BONUS=200;
    private final float DECEL_MALUS=0.9f;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id);
        } else {
            stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
            stats.getAcceleration().modifyPercent(id, ACCEL_BONUS * effectLevel);
            stats.getDeceleration().modifyMult(id, 1-DECEL_MALUS * effectLevel);
        }
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }
    
    private final String TXT = txt("transit1");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        
        int effect = Math.round(SPEED_BONUS*effectLevel);
        
        if (index == 0) {
                return new StatusData(TXT+effect, false);
        }
        return null;
    }
}
