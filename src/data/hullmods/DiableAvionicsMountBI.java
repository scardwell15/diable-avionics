package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class DiableAvionicsMountBI extends BaseHullMod {

    private final float RANGE_BOOST=200;
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        stats.getBeamWeaponRangeBonus().modifyFlat(id, -RANGE_BOOST);
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return RANGE_BOOST+" "+txt("su");
        }
        return null;
    }
}