package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_boosterStats extends BaseShipSystemScript {

    private final Integer SPEED_BONUS=150;
    private final float ACCEL_BONUS=2;
    private final float DECEL_DEBUFF=0.1f;
    private final float TURN_DEBUFF=0.25f;
    
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        
        stats.getTurnAcceleration().modifyMult(id, TURN_DEBUFF);
        stats.getMaxTurnRate().modifyMult(id, TURN_DEBUFF);
        
        stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
        
        stats.getAcceleration().modifyMult(id, ACCEL_BONUS);
        stats.getDeceleration().modifyMult(id, DECEL_DEBUFF);     
        
        stats.getBallisticWeaponRangeBonus().modifyMult(id, 0.01f);
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getTurnAcceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        
        stats.getMaxSpeed().unmodify(id);
        
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        
        stats.getBallisticWeaponRangeBonus().unmodify(id);
    }
    
    
    private final String TXT = txt("booster");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
                return new StatusData(SPEED_BONUS+TXT, false);
        }
        return null;
    }
}
