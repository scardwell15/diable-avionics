package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.combat.listeners.WeaponOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import java.util.EnumMap;
import java.util.Map;

import static data.scripts.util.Diableavionics_stringsManager.txt;

public class DiableAvionicsMountBI extends BaseHullMod {
    public static final String BUILT_IN_MOUNT_ID = "diableavionics_mountBI";

    protected static final float RANGE_BOOST = 200;
    protected static final float LARGE_MOUNT_EXTRA_RANGE_BOOST = 300;
    protected static final float RECOIL_REDUCTION = -30;

    protected static final int FRIGATE_DP = 2;
    protected static final int DESTORYER_DP = 3;
    protected static final int CRUSIER_DP = 5;
    protected static final int CAPITAL_DP = 7;

    private static final Map<WeaponSize, Integer> DIABLE_WEAPON_OP_REDUCTION_MAP = new EnumMap<>(WeaponSize.class);

    static {
        DIABLE_WEAPON_OP_REDUCTION_MAP.put(WeaponSize.SMALL, 2);
        DIABLE_WEAPON_OP_REDUCTION_MAP.put(WeaponSize.MEDIUM, 4);
        DIABLE_WEAPON_OP_REDUCTION_MAP.put(WeaponSize.LARGE, 6);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        stats.getBeamWeaponRangeBonus().modifyFlat(id, -RANGE_BOOST);

        stats.getRecoilPerShotMult().modifyPercent(id, RECOIL_REDUCTION);

        stats.addListener(new DiableWeaponOPModifier(id));

        if (isSMod(stats)) {
            switch (hullSize) {
                case DEFAULT:
                    break;
                case FIGHTER:
                    break;
                case FRIGATE:
                    stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, FRIGATE_DP);
                    break;
                case DESTROYER:
                    stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, DESTORYER_DP);
                    break;
                case CRUISER:
                    stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, CRUSIER_DP);
                    break;
                case CAPITAL_SHIP:
                    stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, CAPITAL_DP);
                    break;
            }
        }
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (BUILT_IN_MOUNT_ID.equals(id)) {
            ship.addListener(new DiableRangeModifier());
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
            return LARGE_MOUNT_EXTRA_RANGE_BOOST + " " + txt("su");
        }
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return DIABLE_WEAPON_OP_REDUCTION_MAP.get(WeaponSize.SMALL) + "";    //+“” means int→string
        }
        if (index == 1) {
            return DIABLE_WEAPON_OP_REDUCTION_MAP.get(WeaponSize.MEDIUM) + "";
        }
        if (index == 2) {
            return DIABLE_WEAPON_OP_REDUCTION_MAP.get(WeaponSize.LARGE) + "";
        }
        if (index == 3) {
            return FRIGATE_DP + "";
        }
        if (index == 4) {
            return DESTORYER_DP + "";
        }
        if (index == 5) {
            return CRUSIER_DP + "";
        }
        if (index == 6) {
            return CAPITAL_DP + "";
        }

        return super.getSModDescriptionParam(index, hullSize);
    }

    public static boolean isDiableWeapon(WeaponSpecAPI weapon) {
        return weapon.getWeaponId().startsWith("diable");
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null) return false;
        return ship.getVariant().hasHullMod(BUILT_IN_MOUNT_ID);
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return ship.getVariant().hasHullMod(BUILT_IN_MOUNT_ID);
    }

    public static final class DiableRangeModifier implements WeaponBaseRangeModifier {
        public DiableRangeModifier() {
        }

        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            return 0.0F;
        }

        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1.0F;
        }

        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            if (isDiableWeapon(weapon.getSpec())) {
                if (weapon.getSize() == WeaponSize.LARGE) {
                    if (weapon.getType() == WeaponType.BALLISTIC || weapon.getType() == WeaponType.ENERGY && !weapon.isBeam() && !weapon.isBurstBeam())
                        return 100f;
                }
            }
            return 0;
        }
    }

    public static class DiableWeaponOPModifier implements WeaponOPCostModifier {
        private final String hullmodId;
        public DiableWeaponOPModifier(String hullmodId) {
            this.hullmodId = hullmodId;
        }

        public int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI weapon, int currCost) {
            if (!isSModded(stats, hullmodId)) return currCost;

            if (isDiableWeapon(weapon)) {
                if (weapon.getType() == WeaponType.BALLISTIC || weapon.getType() == WeaponType.ENERGY) {
                    return currCost - DiableAvionicsMountBI.DIABLE_WEAPON_OP_REDUCTION_MAP.get(weapon.getSize());
                }
            }
            return currCost;
        }
    }

    public static boolean isSModded(MutableShipStatsAPI stats, String id) {
        if (stats == null || stats.getVariant() == null) return false;
        return stats.getVariant().getSMods().contains(id) ||
                stats.getVariant().getSModdedBuiltIns().contains(id);
    }

    public static boolean isSModded(ShipAPI ship, String id) {
        if (ship == null || ship.getVariant() == null) return false;
        return ship.getVariant().getSMods().contains(id) ||
                ship.getVariant().getSModdedBuiltIns().contains(id);
    }
}