package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class DiableAvionicsVirtuous_broken extends BaseHullMod {
    
    @Override
    public int getDisplaySortOrder() {
        return 0;
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxCombatReadiness().modifyMult(id, 0);
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return txt("hm_warning"); 
        return null;
    }
}
