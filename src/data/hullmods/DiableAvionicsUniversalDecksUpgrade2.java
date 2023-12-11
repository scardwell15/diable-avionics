package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import static data.scripts.util.Diableavionics_stringsManager.txt;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class DiableAvionicsUniversalDecksUpgrade2 extends BaseHullMod {  
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return txt("hm_gantry_101");
        }
        return null;
    }
    
    private final Color HL=Global.getSettings().getColor("hColor");
        
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        //title
        tooltip.addSectionHeading(txt("hm_gantry_102"), Alignment.MID, 15);        
        
        if(ship!=null && ship.getVariant()!=null){
            if( ship.getVariant().getFittedWings().isEmpty()){
                //no wing fitted
                tooltip.addPara(
                        txt("hm_gantry_103")
                        ,10
                        ,HL
                );
            } else if(noWanzer(ship.getVariant())){
                //no wanzer wings installed
                tooltip.addPara(
                        txt("hm_gantry_104")
                        ,10
                        ,HL
                );
            } else {
                //effect applied
                List <String> wanzers = allWanzers(ship.getVariant());
                
                if(!wanzers.isEmpty()){
                    tooltip.addPara(
                            txt("hm_gantry_105")
                            ,10
                            ,HL
                            ,txt("hm_gantry_105hl")
                    );

                    tooltip.setBulletedListMode("    - ");  

                    for(String w : wanzers){
                        tooltip.addPara(
                                w
                                ,3
                        );
                    }
                    tooltip.setBulletedListMode(null);
                }
            }
        }
        
    }
    
    private final String ID = "wanzer_gantry", TAG = "wanzer";
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        
        if(ship.getOriginalOwner()==-1){
            return; //supress in refit
        }
        
        boolean allDeployed=true, ranOnce=false;
        
        for(FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()){
            if(bay.getWing()!=null){
                ranOnce=true;
                if(bay.getWing().getSpec().hasTag(TAG)){
                    
                    FighterWingSpecAPI wingSpec = bay.getWing().getSpec();
                    int deployed = bay.getWing().getWingMembers().size();
                    int maxTotal = wingSpec.getNumFighters() + 1;
                    int actualAdd = maxTotal - deployed;

                    if (actualAdd > 0) {
                        bay.setExtraDeployments(actualAdd);
                        bay.setExtraDeploymentLimit(maxTotal);
                        bay.setExtraDuration(9999999);
                        allDeployed=false;
                    } else {
                        bay.setExtraDeployments(0);
                        bay.setExtraDeploymentLimit(0);
                        bay.setFastReplacements(0);
                    }
                    
                    if(ship.getMutableStats().getFighterRefitTimeMult().getPercentStatMod(ID)==null && actualAdd!=0){
                        //instantly add all the required fighters upon deployment
                        bay.setFastReplacements(actualAdd);                        
                    }
                    
                    //debug
//                    Global.getCombatEngine().addFloatingText(
//                            bay.getWeaponSlot().computePosition(ship),
//                            "add= "+bay.getExtraDeployments()+" max= "+bay.getExtraDeploymentLimit()+" fast= "+bay.getFastReplacements(),
//                            10, Color.ORANGE, ship, 1, 1);
                }
            }
        }
        
        if (ship.getMutableStats().getFighterRefitTimeMult().getPercentStatMod(ID)==null && allDeployed && ranOnce){
            //used as a check to add all the extra fighters upon deployment
            ship.getMutableStats().getFighterRefitTimeMult().modifyPercent(ID, 1);
        }
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {        
        //reset the "check" mutable stat so that it is applied next deployment
        stats.getFighterRefitTimeMult().unmodify(ID);
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
        //the extra crafts do not deplete the fighter replacement gauge when destroyed, this makes the rate deplete faster from those that do to compensate.
        Integer crafts=0, extraCrafts=0;
        for(String w : ship.getVariant().getFittedWings()){
            if(Global.getSettings().getFighterWingSpec(w).hasTag(TAG)){
                crafts+=Global.getSettings().getFighterWingSpec(w).getNumFighters();
                extraCrafts++;
            }
        }
        if(extraCrafts>0){
            ship.getMutableStats().getDynamic().getMod(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, (crafts+extraCrafts)/crafts);
        }
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if(ship==null) return false;
        return ship.getMutableStats().getNumFighterBays().getModifiedValue()>0 && !ship.getVariant().getHullMods().contains("diableavionics_universaldecksBI"); 
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if(ship.getVariant().getHullMods().contains("diableavionics_universaldecksBI")){
            return txt("hm_builtin");
        } else {
            return txt("hm_noBays");
        }
    }
    
    private boolean noWanzer(ShipVariantAPI variant){
        boolean noWanzer=true;
        for (String w : variant.getFittedWings()){
            if(Global.getSettings().getFighterWingSpec(w).hasTag(TAG)){
                noWanzer=false;
                break;
            }
        }
        return noWanzer;
    }
    
    private List<String> allWanzers(ShipVariantAPI variant){
        List<String>allWanzers = new ArrayList<>();
        for (String w : variant.getFittedWings()){
            FighterWingSpecAPI f = Global.getSettings().getFighterWingSpec(w);
            if(f.hasTag(TAG)){
                allWanzers.add(f.getWingName() +" "+ f.getRoleDesc());
            }
        }
        return allWanzers;
    }
}
