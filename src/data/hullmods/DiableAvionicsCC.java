package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class DiableAvionicsCC extends BaseHullMod {  
    
    private final float EFFECTS_BONUS=0.5f;
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return Math.round((1-EFFECTS_BONUS)*100) + txt("%");
        }
        return null;
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, EFFECTS_BONUS, txt("hm_cc_1"));
    }
}