package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import static data.scripts.util.Diableavionics_stringsManager.txt;
import data.scripts.util.MagicIncompatibleHullmods;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DiableAvionicsVirtuous_system extends BaseHullMod {
    private final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        BLOCKED_HULLMODS.add("safetyoverrides");
    }
    
    private final Map<String, Integer> SWITCH_SYSTEM_TO = new HashMap<>();
    {
        SWITCH_SYSTEM_TO.put("diableavionics_virtuous_skirmisher",1);
        SWITCH_SYSTEM_TO.put("diableavionics_virtuous_brawler",2);
        SWITCH_SYSTEM_TO.put("diableavionics_virtuous_defender",3);
        SWITCH_SYSTEM_TO.put("diableavionics_virtuous_scout",0);
    }
    
    private final Map<Integer,String> SWITCH_SYSTEM = new HashMap<>();
    {
        SWITCH_SYSTEM.put(0,"diableavionics_virtuous_skirmisher");
        SWITCH_SYSTEM.put(1,"diableavionics_virtuous_brawler");
        SWITCH_SYSTEM.put(2,"diableavionics_virtuous_defender");
        SWITCH_SYSTEM.put(3,"diableavionics_virtuous_scout");
    }  
    
    private final Map<String, Integer> SWITCH_LOADOUT_TO = new HashMap<>();
    {
        SWITCH_LOADOUT_TO.put("A_L",1);
        SWITCH_LOADOUT_TO.put("B_L",2);
        SWITCH_LOADOUT_TO.put("C_L",3);
        SWITCH_LOADOUT_TO.put("D_L",0);
    }
    
    private final Map<Integer,String> SWITCH_LOADOUT = new HashMap<>();
    {
        SWITCH_LOADOUT.put(0,"diableavionics_virtuous_armA");
        SWITCH_LOADOUT.put(1,"diableavionics_virtuous_armB");
        SWITCH_LOADOUT.put(2,"diableavionics_virtuous_armC");
        SWITCH_LOADOUT.put(3,"diableavionics_virtuous_armD");
    } 
    
    private final Map<String, Integer> SWITCH_HEAD_TO = new HashMap<>();
    {
        SWITCH_HEAD_TO.put("diableavionics_virtuous_headA",1);
        SWITCH_HEAD_TO.put("diableavionics_virtuous_headB",2);
        SWITCH_HEAD_TO.put("diableavionics_virtuous_headC",3);
        SWITCH_HEAD_TO.put("diableavionics_virtuous_headD",0);
    }
    
    private final Map<Integer,String> SWITCH_HEAD = new HashMap<>();
    {
        SWITCH_HEAD.put(0,"diableavionics_virtuous_headA");
        SWITCH_HEAD.put(1,"diableavionics_virtuous_headB");
        SWITCH_HEAD.put(2,"diableavionics_virtuous_headC");
        SWITCH_HEAD.put(3,"diableavionics_virtuous_headD");
    } 
    
    private final String leftslotID = "LEFT"; 
    private final String rightslotID = "RIGHT";     
    private final String headslotID = "HEAD";   
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        //trigger a system switch if none of the selector hullmods are present
        boolean switchSystem=true;
        for(String h : SWITCH_SYSTEM_TO.keySet()){
            if(stats.getVariant().getHullMods().contains(h)){
                switchSystem=false;
                break;
            }
        }
        
        //swap the source variant and add the proper hullmod
        if(switchSystem && stats.getEntity()!=null && ((ShipAPI)stats.getEntity()).getHullSpec()!=null){        
            
            int switchTo = SWITCH_SYSTEM_TO.get(((ShipAPI)stats.getEntity()).getHullSpec().getHullId());

            ShipHullSpecAPI ship = Global.getSettings().getHullSpec(SWITCH_SYSTEM.get(switchTo));
            ((ShipAPI)stats.getEntity()).getVariant().setHullSpecAPI(ship);
            
            //add the proper hullmod
            stats.getVariant().addMod(SWITCH_SYSTEM.get(switchTo));
        }
        
        //WEAPONS
        
        //trigger a weapon switch if none of the selector hullmods are present
        boolean switchLoadout=true;
        for(String h : SWITCH_LOADOUT.values()){
            if(stats.getVariant().getHullMods().contains(h)){
                switchLoadout=false;
                break;
            }
        }
        
        //remove the weapons to change and swap the hullmod for the next fire mode
        if(switchLoadout){        
            
            int switchWeaponTo=0;
            for(String h : SWITCH_LOADOUT_TO.keySet()){
                if(stats.getVariant().getWeaponId(leftslotID)!=null && stats.getVariant().getWeaponId(leftslotID).endsWith(h)){
                    switchWeaponTo = SWITCH_LOADOUT_TO.get(h);
                }
            }
            
            //add the proper hullmod
            stats.getVariant().addMod(SWITCH_LOADOUT.get(switchWeaponTo));
            
            //clear the weapons to replace
            stats.getVariant().clearSlot(leftslotID);
            stats.getVariant().clearSlot(rightslotID);
            
            //select and place the proper weapon
            String toInstallLeft=SWITCH_LOADOUT.get(switchWeaponTo)+"_L";                
            String toInstallRight=SWITCH_LOADOUT.get(switchWeaponTo)+"_R";

            stats.getVariant().addWeapon(leftslotID, toInstallLeft);
            stats.getVariant().addWeapon(rightslotID, toInstallRight);

            stats.getVariant().autoGenerateWeaponGroups();
        }
        
        //trigger a head switch if none of the selector hullmods are present
        boolean switchHead=true;
        for(String h : SWITCH_HEAD.values()){
            if(stats.getVariant().getHullMods().contains(h)){
                switchHead=false;
                break;
            }
        }
        
        //remove the weapons to change and swap the hullmod for the next fire mode
        if(switchHead){        
            
            int switchHeadTo=0;
            if(stats.getVariant().getWeaponId(headslotID)!=null){
                switchHeadTo = SWITCH_HEAD_TO.get(stats.getVariant().getWeaponId(headslotID));
            }
            
            //add the proper hullmod
            stats.getVariant().addMod(SWITCH_HEAD.get(switchHeadTo));
            
            //clear the weapons to replace
            stats.getVariant().clearSlot(headslotID);
            
            //select and place the proper weapon 
            String toInstallHead=SWITCH_HEAD.get(switchHeadTo);   
            stats.getVariant().addWeapon(headslotID, toInstallHead);
            
            stats.getVariant().autoGenerateWeaponGroups();
        }
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){

        //blocked hullmods
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) { 
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "diableavionics_virtuousMasterHullmod");
            }
        }
        
        if(ship.getOriginalOwner()<0){
            //undo fix for weapons put in cargo
            if(
                    Global.getSector()!=null && 
                    Global.getSector().getPlayerFleet()!=null && 
                    Global.getSector().getPlayerFleet().getCargo()!=null && 
                    Global.getSector().getPlayerFleet().getCargo().getStacksCopy()!=null &&
                    !Global.getSector().getPlayerFleet().getCargo().getStacksCopy().isEmpty()
                    ){
                for (CargoStackAPI s : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()){
                    if(
                            s.isWeaponStack() 
                            && s.getWeaponSpecIfWeapon().getWeaponId().startsWith("diableavionics_virtuous")
                            ){
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
    
    private final Color HL=Global.getSettings().getColor("hColor");    
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        //title
        tooltip.addSectionHeading(txt("hm_virtuousRefresh1"), Alignment.MID, 15);        
        
        tooltip.addPara(
                txt("hm_virtuousRefresh2")
                ,10
                ,HL
                ,txt("hm_virtuousRefresh3"),txt("hm_virtuousRefresh4")
        );
        
        
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a diableavionics hull id
        return ( ship.getHullSpec().getHullId().startsWith("diableavionics_"));	
    }
}
