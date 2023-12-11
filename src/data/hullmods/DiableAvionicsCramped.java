package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashSet;
import java.util.Set;
import static data.scripts.util.Diableavionics_stringsManager.txt;
import org.magiclib.util.MagicIncompatibleHullmods;

public class DiableAvionicsCramped extends BaseHullMod {

    private final float MISSILES_DEBUFF = 0.5f;
//    private final String ERROR="IncompatibleHullmodWarning";    
    private final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        BLOCKED_HULLMODS.add("converted_hangar");
        BLOCKED_HULLMODS.add("magazines");
        BLOCKED_HULLMODS.add("missleracks");
        BLOCKED_HULLMODS.add("VEmagazines");
        BLOCKED_HULLMODS.add("VEmissleracks");
        BLOCKED_HULLMODS.add("cargo_expansion");
        BLOCKED_HULLMODS.add("additional_crew_quarters");
        BLOCKED_HULLMODS.add("fuel_expansion");
        BLOCKED_HULLMODS.add("additional_berthing");
        BLOCKED_HULLMODS.add("auxiliary_fuel_tanks");
        BLOCKED_HULLMODS.add("expanded_cargo_holds");
        BLOCKED_HULLMODS.add("surveying_equipment");
        BLOCKED_HULLMODS.add("recovery_shuttles");
        BLOCKED_HULLMODS.add("operations_center");
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //missiles fire-rate
        stats.getMissileRoFMult().modifyMult(id, MISSILES_DEBUFF);	
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
        //blocked hullmods
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) { 
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "diableavionics_cramped");
            }
        }
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return txt("hm_cramped_0");
        }
        if (index == 1) {
            return txt("hm_cramped_1");
        }
        if (index == 2) {
            return txt("hm_cramped_2");
        }
        if (index == 3) {
            return (int)(MISSILES_DEBUFF*100)+txt("%");
        }        
        return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a diableavionics hull id
        return ( ship.getHullSpec().getHullId().startsWith("diableavionics_"));	
    }
}
