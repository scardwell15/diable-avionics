package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import data.scripts.ai.WanzerMovementScript;

public class DiableAvionicsWanzer extends BaseHullMod {

    private final float EMP_RESIST = 33, DISABLE_RESIST = 66;

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) EMP_RESIST + "%";
        }
        if (index == 1) {
            return "" + (int) DISABLE_RESIST + "%";
        }
        return null;
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getMutableStats().getEmpDamageTakenMult().modifyMult(id, (100 - EMP_RESIST) / 100);
        ship.getMutableStats().getEngineDamageTakenMult().modifyMult(id, (100 - DISABLE_RESIST) / 100);
        ship.getMutableStats().getWeaponDamageTakenMult().modifyMult(id, (100 - DISABLE_RESIST) / 100);

        ship.addListener(new WanzerMovementScript(ship));
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a diableavionics hull id
        return (ship.getHullSpec().getHullId().startsWith("diableavionics_"));
    }
}
