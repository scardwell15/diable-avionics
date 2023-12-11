package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.scripts.DAModPlugin;
import java.awt.Color;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class DiableAvionicsUniversalDecksUpgrade extends BaseHullMod {  
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return (int) (DAModPlugin.GANTRY_TIME_MULT*100)+txt("%");
        }
        if (index == 1) {
            return (int) DAModPlugin.GANTRY_DEPLETION_PERCENT+txt("%");
        }        
        return null;
    }
    
    private final Color HL=Global.getSettings().getColor("hColor");
    
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        //title
        tooltip.addSectionHeading(txt("hm_gantry_0"), Alignment.MID, 15);        
        
        if(ship!=null && ship.getVariant()!=null){
            if( ship.getVariant().getNonBuiltInWings().isEmpty()){
                //no wing fitted
                tooltip.addPara(
                        txt("hm_gantry_1")
                        ,10
                        ,HL
                );
            } else if(!allWanzers(ship.getVariant())){
                //non wanzer wings installed
                tooltip.addPara(
                        txt("hm_gantry_2")
                        ,10
                        ,HL
                );
            } else {
                //effect applied
                String depletion = String.valueOf((int)(DAModPlugin.GANTRY_DEPLETION_PERCENT*ship.getVariant().getNonBuiltInWings().size()));
                String wings = String.valueOf((int)ship.getVariant().getNonBuiltInWings().size());
                tooltip.addPara(
                        txt("hm_gantry_3")
                        + depletion
                        + txt("hm_gantry_4")
                        + wings
                        + txt("hm_gantry_5")
                        ,10
                        ,HL
                        ,depletion
                        ,wings
                );

                //list new wanzer replacement rates
                tooltip.addPara(
                        txt("hm_gantry_6")
                        ,10
                        ,HL
                );

                tooltip.setBulletedListMode("    - ");  

                for(String w : ship.getVariant().getNonBuiltInWings()){
                    String wingName = Global.getSettings().getFighterWingSpec(w).getWingName();
                    int newTime = (int)Global.getSettings().getFighterWingSpec(w).getRefitTime()/2;

                    tooltip.addPara(
                            wingName
                            + txt("hm_gantry_7")
                            + newTime
                            + txt("hm_gantry_8")
                            ,3
                            ,HL
                            ,""+newTime
                    );
                }
                tooltip.setBulletedListMode(null);
            }
        }
        
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        boolean all_wanzers=allWanzers(stats.getVariant());
        
        if(all_wanzers){
            //faster repairs
            stats.getFighterRefitTimeMult().modifyMult("wanzer_gantry", DAModPlugin.GANTRY_TIME_MULT, "Wanzer Gantry bonus");
            //faster depletion of replacement rate
            float depletion = DAModPlugin.GANTRY_DEPLETION_PERCENT*stats.getVariant().getNonBuiltInWings().size();
            stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyPercent(id, depletion);
            //debug
//            ship.getMutableStats().getFighterRefitTimeMult().modifyMult("wanzer_gantry", 0.01f, "Wanzer Gantry bonus");
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

    private boolean allWanzers(ShipVariantAPI v){
        boolean all_wanzers=true;
        for(String w : v.getWings()){
            if (!DAModPlugin.WANZERS.contains(w)){
                all_wanzers=false;
            }
        }
        return all_wanzers;
    }
}
