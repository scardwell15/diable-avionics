package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class DiableAvionicsFlavor extends BaseHullMod {
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return true;	
    }
}
