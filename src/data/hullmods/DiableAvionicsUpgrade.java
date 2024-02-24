package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class DiableAvionicsUpgrade extends BaseHullMod {
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponDamageMult().modifyPercent(id, 30);
        stats.getEnergyWeaponDamageMult().modifyPercent(id, 30);
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, 30);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, 30);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "30%";
        }
        if (index == 1) {
            return "30%";
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getHullSpec().getHullId().startsWith("diableavionics_");
    }
}
