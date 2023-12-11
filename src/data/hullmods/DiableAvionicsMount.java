package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class DiableAvionicsMount extends BaseHullMod {

    private final float RANGE_BOOST=200;
    private final float DAMAGE_TAKEN=100;
    private final float FIRERATE_REDUCTION=-20;
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        stats.getBeamWeaponRangeBonus().modifyFlat(id, -RANGE_BOOST);
        
        stats.getBallisticRoFMult().modifyPercent(id, FIRERATE_REDUCTION);
        stats.getEnergyRoFMult().modifyPercent(id, FIRERATE_REDUCTION);
        
        stats.getWeaponDamageTakenMult().modifyPercent(id, DAMAGE_TAKEN);
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return RANGE_BOOST+" "+txt("su");
        }
        if (index == 1) {
            return 100+FIRERATE_REDUCTION+txt("%");
        }
        if (index == 2) {
            return DAMAGE_TAKEN+txt("%");
        }
        return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if(ship==null) return false;
        return !ship.getVariant().getHullMods().contains("diableavionics_mountBI"); 
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return txt("hm_builtin");
    }
}