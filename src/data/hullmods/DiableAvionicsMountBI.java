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

    private final float RANGE_BOOST=200;
    private final float EXTRA_RANGE_BOOST=300;
    private final float FIRERATE_REDUCTION=-20;
    private final float RECOIL_REDUCTION=-30;

    private final int SMALL_SIZE_OP_REDCUTION=2;
    private final int MEDIUM_SIZE_OP_REDCUTION=4;
    private final int LARGE_SIZE_OP_REDCUTION=6;

    private final int FRIGATE_DP=2;
    private final int DESTORYER_DP=3;
    private final int CRUSIER_DP=5;
    private final int CAPITAL_DP=7;

    private static final Map<WeaponSize, Integer> DIABLE_WEAPON_OP_REDUCTION_MAP = new EnumMap(WeaponSize.class);

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        DIABLE_WEAPON_OP_REDUCTION_MAP.put(WeaponSize.SMALL, SMALL_SIZE_OP_REDCUTION);
        DIABLE_WEAPON_OP_REDUCTION_MAP.put(WeaponSize.MEDIUM, MEDIUM_SIZE_OP_REDCUTION);
        DIABLE_WEAPON_OP_REDUCTION_MAP.put(WeaponSize.LARGE, LARGE_SIZE_OP_REDCUTION);

        stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        stats.getBeamWeaponRangeBonus().modifyFlat(id, -RANGE_BOOST);

        stats.getRecoilPerShotMult().modifyPercent(id,RECOIL_REDUCTION);

        stats.addListener(new DiableAvionicsMountBI.DiableRangeModifier());
        if(isSMod(stats)){
            switch(hullSize) {
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
            stats.addListener(new DiableAvionicsMountBI.DiableWeaponOPModifier());
        }

    }
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new DiableAvionicsMountBI.DiableRangeModifier());
    }
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return RANGE_BOOST+" "+txt("su");
        }
        if (index == 1) {
            return 25+txt("%");
        }
        if (index == 2) {
            return EXTRA_RANGE_BOOST+" "+txt("su");
        }
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return SMALL_SIZE_OP_REDCUTION+"";    //+“” means int→string
        }
        if (index == 1) {
            return MEDIUM_SIZE_OP_REDCUTION+"";
        }
        if (index == 2) {
            return LARGE_SIZE_OP_REDCUTION+"";
        }
        if (index == 3) {
            return FRIGATE_DP+"";
        }
        if (index == 4) {
            return DESTORYER_DP+"";
        }
        if (index == 5) {
            return CRUSIER_DP+"";
        }
        if (index == 6) {
            return CAPITAL_DP+"";
        }

        return super.getSModDescriptionParam(index, hullSize);
    }
    public static boolean isdiableWeapon(WeaponSpecAPI weapon) {
        return weapon.getWeaponId().startsWith("diable");
    }

    public boolean affectsOPCosts() {
        return true;
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
            if(DiableAvionicsMount.isdiableWeapon(weapon.getSpec())) {
                if (weapon.getSize()==WeaponSize.LARGE){
                    if(weapon.getType()==WeaponType.BALLISTIC|| weapon.getType()==WeaponType.ENERGY &&!weapon.isBeam()&&!weapon.isBurstBeam())
                        return 100f;
                }
            }
            return 0;
        }
    }
    public static class DiableWeaponOPModifier implements WeaponOPCostModifier {
        public DiableWeaponOPModifier() {
        }

        public int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI weapon, int currCost) {
            return !weapon.getType().equals(WeaponAPI.WeaponType.MISSILE) ? currCost - (DiableAvionicsMountBI.isdiableWeapon(weapon) ?(Integer)DiableAvionicsMountBI.DIABLE_WEAPON_OP_REDUCTION_MAP.get(weapon.getSize()) : 0): currCost;
        }
    }
}