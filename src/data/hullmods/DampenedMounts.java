package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import static data.scripts.util.Diableavionics_stringsManager.txt;

public class DampenedMounts extends DiableAvionicsMountBI {
    public static final String MODULAR_MOUNT_ID = "diableavionics_mount";
    protected static final float MODULAR_FIRERATE_REDUCTION = -20;
    protected static final float MODULAR_WEAPON_DAMAGE_TAKEN = 100;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);

        stats.getWeaponDamageTakenMult().modifyPercent(id, MODULAR_WEAPON_DAMAGE_TAKEN);

        if (!isSMod(stats)) {
            stats.getBallisticRoFMult().modifyPercent(id, MODULAR_FIRERATE_REDUCTION);
            stats.getEnergyRoFMult().modifyPercent(id, MODULAR_FIRERATE_REDUCTION);
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return RANGE_BOOST + " " + txt("su");
        }
        if (index == 1) {
            return 25 + txt("%");
        }
        if (index == 2) {
            return 100 + MODULAR_FIRERATE_REDUCTION + txt("%");
        }
        if (index == 3) {
            return MODULAR_WEAPON_DAMAGE_TAKEN + txt("%");
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null) return false;
        return !ship.getVariant().getHullMods().contains(DiableAvionicsMountBI.BUILT_IN_MOUNT_ID);
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        if (ship == null) return false;
        return !ship.getVariant().getHullMods().contains(DiableAvionicsMountBI.BUILT_IN_MOUNT_ID);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return txt("hm_builtin");
    }
}