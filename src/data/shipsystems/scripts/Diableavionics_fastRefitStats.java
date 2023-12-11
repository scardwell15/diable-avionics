package data.shipsystems.scripts;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_fastRefitStats extends BaseShipSystemScript {

    private final float BOOST=50, CONSUMPTION=100;	

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(effectLevel>0){            
            stats.getFighterRefitTimeMult().modifyPercent(id, BOOST*effectLevel);
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyPercent(id, CONSUMPTION*effectLevel);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {        
        stats.getFighterRefitTimeMult().unmodify(id);
        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).unmodify(id);
    }	

    private final String PLUS = txt("+");
    private final String MINUS = txt("-");
    private final String TXT1 = txt("refit1");
    private final String TXT2 = txt("refit2");
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(MINUS+(int)(BOOST * effectLevel)+TXT1, false);
        }
        if (index == 1) {
            return new StatusData(PLUS+(int)(CONSUMPTION * effectLevel)+TXT2, true);
        }
        return null;
    }
}