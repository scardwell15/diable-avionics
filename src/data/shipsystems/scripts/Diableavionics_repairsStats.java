package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_repairsStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getArmorDamageTakenMult().modifyPercent(id, effectLevel*100);
        stats.getHullDamageTakenMult().modifyPercent(id, effectLevel*100);
        stats.getAcceleration().modifyPercent(id, effectLevel*200);
        stats.getDeceleration().modifyPercent(id, effectLevel*200);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        
    }

    private final String TXT1 = txt("repair");
    private final String TXT2 = txt("%");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float bonusPercent = 100 + (int) (effectLevel * 100f);
        if (index == 1) {
            return new StatusData(TXT1 + (int) bonusPercent + TXT2, true);
        }
        return null;
    }
}
