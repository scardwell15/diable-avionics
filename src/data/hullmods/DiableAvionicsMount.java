package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.combat.listeners.WeaponOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static data.scripts.util.Diableavionics_stringsManager.txt;

public class DiableAvionicsMount extends BaseHullMod {

    private final float RANGE_BOOST=200;
    private final float DAMAGE_TAKEN=100;
    private final float FIRERATE_REDUCTION=-20;
    private final float RECOIL_REDUCTION=-30;

    private final int SMALL_SIZE_OP_REDCUTION=2;
    private final int MEDIUM_SIZE_OP_REDCUTION=4;
    private final int LARGE_SIZE_OP_REDCUTION=6;

    private final int FRIGATE_DP=2;
    private final int DESTORYER_DP=3;
    private final int CRUSIER_DP=4;
    private final int CAPITAL_DP=5;

    private static final Map<WeaponAPI.WeaponSize, Integer> DIABLE_WEAPON_OP_REDUCTION_MAP = new EnumMap(WeaponAPI.WeaponSize.class);



    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        DIABLE_WEAPON_OP_REDUCTION_MAP.put(WeaponAPI.WeaponSize.SMALL, SMALL_SIZE_OP_REDCUTION);
        DIABLE_WEAPON_OP_REDUCTION_MAP.put(WeaponAPI.WeaponSize.MEDIUM, MEDIUM_SIZE_OP_REDCUTION);
        DIABLE_WEAPON_OP_REDUCTION_MAP.put(WeaponAPI.WeaponSize.LARGE, LARGE_SIZE_OP_REDCUTION);

        stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BOOST);
        stats.getBeamWeaponRangeBonus().modifyFlat(id, -RANGE_BOOST);

        stats.getRecoilPerShotMult().modifyPercent(id,RECOIL_REDUCTION);

        stats.getBallisticRoFMult().modifyPercent(id, FIRERATE_REDUCTION);
        stats.getEnergyRoFMult().modifyPercent(id, FIRERATE_REDUCTION);
        
        stats.getWeaponDamageTakenMult().modifyPercent(id, DAMAGE_TAKEN);

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

            stats.addListener(new DiableAvionicsMount.DiableWeaponOPModifier());
            stats.getBallisticRoFMult().unmodify();
            stats.getEnergyRoFMult().unmodify();
        }

    }


    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return RANGE_BOOST+" "+txt("su");
        }
        if (index == 1) {
            return 25+txt("%");
        }
        if(index  == 2){
            return 100+FIRERATE_REDUCTION+txt("%");
        }
        if (index == 3) {
            return DAMAGE_TAKEN+txt("%");
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


    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if(ship==null) return false;
        return !ship.getVariant().getHullMods().contains("diableavionics_mountBI"); 
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return txt("hm_builtin");
    }




    public static boolean isdiableWeapon(WeaponSpecAPI weapon) {
        return weapon.getWeaponId().startsWith("diable");
    }

    public boolean affectsOPCosts() {
        return true;
    }

    public static class DiableWeaponOPModifier implements WeaponOPCostModifier {
        public DiableWeaponOPModifier() {
        }

        public int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI weapon, int currCost) {
            return !weapon.getType().equals(WeaponAPI.WeaponType.MISSILE) ? currCost - (DiableAvionicsMount.isdiableWeapon(weapon) ?(Integer)DiableAvionicsMount.DIABLE_WEAPON_OP_REDUCTION_MAP.get(weapon.getSize()) : 0): currCost;
        }
    }
}