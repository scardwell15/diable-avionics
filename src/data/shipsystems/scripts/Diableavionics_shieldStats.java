package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_shieldStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getShieldTurnRateMult().modifyMult(id, 2f);
        stats.getShieldDamageTakenMult().modifyMult(id, 1f - .9f * effectLevel);		
        stats.getShieldUpkeepMult().modifyMult(id, 0f);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().unmodify(id);
        stats.getShieldTurnRateMult().unmodify(id);
        stats.getShieldUpkeepMult().unmodify(id);
    }

    private final String TXT1 = txt("shield");
    private final String TXT2 = txt("%");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        
        int effect = Math.round(1000 * effectLevel);
        
        if (index == 0) {
            return new StatusData(TXT1+ effect +TXT2, false);
        }
        return null;
    }
}
