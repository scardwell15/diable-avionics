package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import static data.scripts.util.Diableavionics_stringsManager.txt;

import data.campaign.ids.Diableavionics_ids;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.awt.Color;
import java.util.*;

public class DiableAvionicsVirtuous_system extends BaseHullMod {
    private final Set<String> BLOCKED_HULLMODS = new HashSet<>();

    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        BLOCKED_HULLMODS.add("safetyoverrides");
    }

    private final Map<String, Integer> SWITCH_SYSTEM_TO = new HashMap<>();
    {
        SWITCH_SYSTEM_TO.put("diableavionics_unlockedFlicker", 1);
        SWITCH_SYSTEM_TO.put("diableavionics_temporalshell", 2);
        SWITCH_SYSTEM_TO.put("diableavionics_citadel", 3);
        SWITCH_SYSTEM_TO.put("diableavionics_circus", 0);
    }

    private final Map<Integer, String> SWITCH_HULLSPECS = new HashMap<>();
    {
        SWITCH_HULLSPECS.put(0, "diableavionics_virtuous_skirmisher");
        SWITCH_HULLSPECS.put(1, "diableavionics_virtuous_brawler");
        SWITCH_HULLSPECS.put(2, "diableavionics_virtuous_defender");
        SWITCH_HULLSPECS.put(3, "diableavionics_virtuous_scout");
    }

    private static final String LEFT = "_L";
    private static final String RIGHT = "_R";
    private final List<String> WEAPON_LIST = new LinkedList<>();
    {
        WEAPON_LIST.add("diableavionics_virtuous_scicle");
        WEAPON_LIST.add("diableavionics_virtuous_snowblast");
        WEAPON_LIST.add("diableavionics_virtuous_excision");
        WEAPON_LIST.add("diableavionics_virtuous_glaux");
        WEAPON_LIST.add("diableavionics_virtuous_grasshopper");
        WEAPON_LIST.add("diableavionics_virtuous_beast");
        WEAPON_LIST.add("diableavionics_virtuous_roar");
    }

    private final Map<String, Integer> SWITCH_HEAD_TO = new HashMap<>();

    {
        SWITCH_HEAD_TO.put("diableavionics_virtuous_headA", 1);
        SWITCH_HEAD_TO.put("diableavionics_virtuous_headB", 2);
        SWITCH_HEAD_TO.put("diableavionics_virtuous_headC", 3);
        SWITCH_HEAD_TO.put("diableavionics_virtuous_headD", 0);
    }

    private final Map<Integer, String> SWITCH_HEAD = new HashMap<>();

    {
        SWITCH_HEAD.put(0, "diableavionics_virtuous_headA");
        SWITCH_HEAD.put(1, "diableavionics_virtuous_headB");
        SWITCH_HEAD.put(2, "diableavionics_virtuous_headC");
        SWITCH_HEAD.put(3, "diableavionics_virtuous_headD");
    }

    private final String leftslotID = "LEFT";
    private final String rightslotID = "RIGHT";
    private final String headslotID = "HEAD";

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats.getFleetMember() == null
                || (stats.getFleetMember().getFleetData() == null || stats.getFleetMember().getFleetData().getFleet() != Global.getSector().getPlayerFleet())) {
            //prevent direct recovery from Last Line fleet because we spawn a derelict after combat

            if (!stats.getVariant().hasTag(Tags.VARIANT_UNBOARDABLE))
                stats.getVariant().addTag(Tags.VARIANT_UNBOARDABLE);

            if (stats.getVariant().hasTag(Tags.VARIANT_ALWAYS_RECOVERABLE))
                stats.getVariant().removeTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
        } else if (stats.getFleetMember() != null
                && stats.getFleetMember().getFleetData() != null
                && stats.getFleetMember().getFleetData().getFleet() == Global.getSector().getPlayerFleet()) {

            //always recover if in player fleet
            if (stats.getVariant().hasTag(Tags.VARIANT_UNBOARDABLE))
                stats.getVariant().removeTag(Tags.VARIANT_UNBOARDABLE);

            if (!stats.getVariant().hasTag(Tags.VARIANT_ALWAYS_RECOVERABLE))
                stats.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
        }

        //trigger a system switch if none of the selector hullmods are present
        boolean switchSystem = true;
        for (String h : SWITCH_HULLSPECS.values()) {
            if (stats.getVariant().getHullMods().contains(h)) {
                switchSystem = false;
                break;
            }
        }

        //swap the source variant and add the proper hullmod
        if (switchSystem && stats.getEntity() != null && ((ShipAPI) stats.getEntity()).getHullSpec() != null) {
            int switchToIndex = SWITCH_SYSTEM_TO.get(((ShipAPI) stats.getEntity()).getHullSpec().getShipSystemId());
            String switchTo = SWITCH_HULLSPECS.get(switchToIndex);

            ShipHullSpecAPI ship = Global.getSettings().getHullSpec(switchTo);
            ((ShipAPI) stats.getEntity()).getVariant().setHullSpecAPI(ship);

            //add the proper hullmod
            stats.getVariant().addMod(switchTo);
        }

        //WEAPONS
        //trigger a weapon switch if none of the selector hullmods are present for left or right
        boolean switchLoadoutLeft = true;
        boolean switchLoadoutRight = true;
        for (String h : WEAPON_LIST) {
            if (stats.getVariant().getHullMods().contains(h + LEFT)) {
                switchLoadoutLeft = false;
            }

            if (stats.getVariant().getHullMods().contains(h + RIGHT)) {
                switchLoadoutRight = false;
            }

            if (!switchLoadoutLeft && !switchLoadoutRight) break;
        }

        //remove the weapons to change and swap the hullmod for the next fire mode
        if (switchLoadoutLeft) {
            String switchWeaponTo = WEAPON_LIST.get(0) + LEFT;
            for (int i = 0; i < WEAPON_LIST.size(); i++) {
                String weaponPrefix = WEAPON_LIST.get(i);
                if (stats.getVariant().getWeaponId(leftslotID) != null && stats.getVariant().getWeaponId(leftslotID).startsWith(weaponPrefix)) {
                    int weaponIndex = i + 1;
                    if (weaponIndex >= WEAPON_LIST.size())
                        weaponIndex = 0;
                    switchWeaponTo = WEAPON_LIST.get(weaponIndex) + LEFT;
                }
            }

            stats.getVariant().addMod(switchWeaponTo);
            stats.getVariant().clearSlot(leftslotID);
            stats.getVariant().addWeapon(leftslotID, switchWeaponTo);
            stats.getVariant().autoGenerateWeaponGroups();
        }

        //remove the weapons to change and swap the hullmod for the next fire mode
        if (switchLoadoutRight) {
            String switchWeaponTo = WEAPON_LIST.get(0) + RIGHT;
            for (int i = 0; i < WEAPON_LIST.size(); i++) {
                String weaponPrefix = WEAPON_LIST.get(i);
                if (stats.getVariant().getWeaponId(rightslotID) != null && stats.getVariant().getWeaponId(rightslotID).startsWith(weaponPrefix)) {
                    int weaponIndex = i + 1;
                    if (weaponIndex >= WEAPON_LIST.size())
                        weaponIndex = 0;
                    switchWeaponTo = WEAPON_LIST.get(weaponIndex) + RIGHT;
                }
            }

            stats.getVariant().addMod(switchWeaponTo);
            stats.getVariant().clearSlot(rightslotID);
            stats.getVariant().addWeapon(rightslotID, switchWeaponTo);
            stats.getVariant().autoGenerateWeaponGroups();
        }

        //trigger a head switch if none of the selector hullmods are present
        boolean switchHead = true;
        for (String h : SWITCH_HEAD.values()) {
            if (stats.getVariant().getHullMods().contains(h)) {
                switchHead = false;
                break;
            }
        }

        //remove the weapons to change and swap the hullmod for the next fire mode
        if (switchHead) {

            int switchHeadTo = 0;
            if (stats.getVariant().getWeaponId(headslotID) != null) {
                switchHeadTo = SWITCH_HEAD_TO.get(stats.getVariant().getWeaponId(headslotID));
            }

            //add the proper hullmod
            stats.getVariant().addMod(SWITCH_HEAD.get(switchHeadTo));

            //clear the weapons to replace
            stats.getVariant().clearSlot(headslotID);

            //select and place the proper weapon 
            String toInstallHead = SWITCH_HEAD.get(switchHeadTo);
            stats.getVariant().addWeapon(headslotID, toInstallHead);

            stats.getVariant().autoGenerateWeaponGroups();
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        //blocked hullmods
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "diableavionics_virtuousMasterHullmod");
            }
        }

        if (ship.getOriginalOwner() < 0) {
            //undo fix for weapons put in cargo
            if (Global.getSector() != null &&
                            Global.getSector().getPlayerFleet() != null &&
                            Global.getSector().getPlayerFleet().getCargo() != null &&
                            Global.getSector().getPlayerFleet().getCargo().getStacksCopy() != null &&
                            !Global.getSector().getPlayerFleet().getCargo().getStacksCopy().isEmpty()) {

                for (CargoStackAPI s : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()) {
                    if (s.isWeaponStack() && s.getWeaponSpecIfWeapon().getWeaponId().startsWith("diableavionics_virtuous")) {
                        Global.getSector().getPlayerFleet().getCargo().removeStack(s);
                    }
                }
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return txt("hm_warning");
        if (index == 1) return Global.getSettings().getHullModSpec("safetyoverrides").getDisplayName();
        return null;
    }

    private final Color HL = Global.getSettings().getColor("hColor");

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        //title
        tooltip.addSectionHeading(txt("hm_virtuousRefresh1"), Alignment.MID, 15);
        tooltip.addPara(txt("hm_virtuousRefresh2"), 10, HL, txt("hm_virtuousRefresh3"), txt("hm_virtuousRefresh4"));
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a diableavionics hull id
        return (ship.getHullSpec().getHullId().startsWith("diableavionics_"));
    }
}
