package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import static data.scripts.util.Diableavionics_stringsManager.txt;
import org.magiclib.util.MagicIncompatibleHullmods;
import java.util.HashSet;
import java.util.Set;

public class DiableAvionicsTuning extends BaseHullMod {
    
//    private static float debuff=0;
    
//    private static final Map<String,Float> HULLMOD_DEBUFF = new HashMap<>();
//    static{
//        HULLMOD_DEBUFF.put("safetyoverrides",0.2f);
////        HULLMOD_DEBUFF.put("unstable_injector",0.15f);
////        HULLMOD_DEBUFF.put("auxiliarythrusters",0.15f);
////        HULLMOD_DEBUFF.put("SCY_lightArmor",0.15f);
//    }
    private  final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        BLOCKED_HULLMODS.add("safetyoverrides");
    }
    
    private final Map<Integer,String> LEFT_SELECTOR = new HashMap<>();
    {
        LEFT_SELECTOR.put(0, "diableavionics_versant_harvest_LEFT");
        LEFT_SELECTOR.put(1, "diableavionics_versant_harvestB_LEFT");
        LEFT_SELECTOR.put(2, "diableavionics_versant_harvestC_LEFT");
    }
    
    private final Map<Integer,String> RIGHT_SELECTOR = new HashMap<>();
    {
        RIGHT_SELECTOR.put(0, "diableavionics_versant_harvest_RIGHT");
        RIGHT_SELECTOR.put(1, "diableavionics_versant_harvestB_RIGHT");
        RIGHT_SELECTOR.put(2, "diableavionics_versant_harvestC_RIGHT");
    }
    
    private final Map<String, Integer> SWITCH_TO = new HashMap<>();
    {
        SWITCH_TO.put("diableavionics_versant_harvest_LEFT",1);
        SWITCH_TO.put("diableavionics_versant_harvestB_LEFT",2);
        SWITCH_TO.put("diableavionics_versant_harvestC_LEFT",0);
    }
    
    private final Map<Integer,String> SWITCH = new HashMap<>();
    {
        SWITCH.put(0,"diableavionics_selector_auto");
        SWITCH.put(1,"diableavionics_selector_burst");
        SWITCH.put(2,"diableavionics_selector_semi");
    }    
    
    private final String leftslotID = "GUN_LEFT"; 
    private final String rightslotID = "GUN_RIGHT";     
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
//        debuff=0;
//        for(String h : stats.getVariant().getHullMods()){
//            if(HULLMOD_DEBUFF.containsKey(h)){
//                debuff+=HULLMOD_DEBUFF.get(h);
//            }
//        }
//        stats.getPeakCRDuration().modifyMult(id,1-debuff);
        
        //trigger a weapon switch if none of the selector hullmods are present
        boolean toSwitch=true;
        for(int i=0; i<SWITCH.size(); i++){
            if(stats.getVariant().getHullMods().contains(SWITCH.get(i))){
                toSwitch=false;
            }
        }
        
        //remove the weapons to change and swap the hullmod for the next fire mode
        if(toSwitch){        
            //select new fire mode
            int selected;       
            boolean random=false;
            if(stats.getVariant().getWeaponSpec(leftslotID)!=null){
                selected=SWITCH_TO.get(stats.getVariant().getWeaponSpec(leftslotID).getWeaponId());
                
            } else {
                selected=MathUtils.getRandomNumberInRange(0, SWITCH_TO.size()-1);
                random=true;
            }
            
            //add the proper hullmod
            stats.getVariant().addMod(SWITCH.get(selected));

            //clear the weapons to replace
            stats.getVariant().clearSlot(leftslotID);
            stats.getVariant().clearSlot(rightslotID);
            //select and place the proper weapon
            String toInstallLeft=LEFT_SELECTOR.get(selected);                
            String toInstallRight=RIGHT_SELECTOR.get(selected);

            stats.getVariant().addWeapon(leftslotID, toInstallLeft);
            stats.getVariant().addWeapon(rightslotID, toInstallRight);
            
            if(random){
                stats.getVariant().autoGenerateWeaponGroups();
            }
        }
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
        
        //blocked hullmods
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) { 
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "diableavionics_tuning");
            }
        }
        
        //only check for undo in refit to avoid issues
        if(ship.getOriginalOwner()<0){
            //undo fix for harvests put in cargo
            if(
                    Global.getSector()!=null && 
                    Global.getSector().getPlayerFleet()!=null && 
                    Global.getSector().getPlayerFleet().getCargo()!=null && 
                    Global.getSector().getPlayerFleet().getCargo().getStacksCopy()!=null &&
                    !Global.getSector().getPlayerFleet().getCargo().getStacksCopy().isEmpty()
                    ){
                for (CargoStackAPI s : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()){
                    if(
                            s.isWeaponStack() && (
                                LEFT_SELECTOR.containsValue(s.getWeaponSpecIfWeapon().getWeaponId()) || 
                                RIGHT_SELECTOR.containsValue(s.getWeaponSpecIfWeapon().getWeaponId())
                                ) 
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
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a diableavionics hull id
        return ( ship.getHullSpec().getHullId().startsWith("diableavionics_"));	
    }
}
