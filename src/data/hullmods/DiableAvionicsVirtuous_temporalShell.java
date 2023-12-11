package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import static data.scripts.util.Diableavionics_stringsManager.txt;
import java.awt.Color;

public class DiableAvionicsVirtuous_temporalShell extends BaseHullMod {
    
    private final int VENTING = 33, AUTO_AIM=50, PPT=120;
    
    @Override
    public int getDisplaySortOrder() {
        return 2002;
    }

    @Override
    public int getDisplayCategoryIndex() {
        return 3;
    }
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
        stats.getVentRateMult().modifyPercent(id, VENTING);
        stats.getAutofireAimAccuracy().modifyPercent(id, AUTO_AIM);
        stats.getPeakCRDuration().modifyFlat(id, PPT);
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }    
    
    private final Color HL=Global.getSettings().getColor("hColor");    
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        
        //title shipsystem
        tooltip.addSectionHeading(txt("hm_virtuousSystem0"), Alignment.MID, 15);    
        //ship system
        tooltip.addPara(
                txt("hm_virtuousSystem1")
                + ship.getSystem().getDisplayName()
                ,10
                ,HL
                ,ship.getSystem().getDisplayName()
        );

        //title modifiers
        tooltip.addSectionHeading(txt("hm_virtuousEffect"), Alignment.MID, 15);    
        //stats modified
        tooltip.setBulletedListMode(" - ");  
        
        tooltip.addPara(
                txt("hm_virtuousEffect_vent")
                + txt("+")
                + VENTING
                + " "
                + txt("percent")
                ,10
                ,HL
                ,txt("+")+VENTING+" "+txt("percent")
        );
        
        tooltip.addPara(
                txt("hm_virtuousEffect_aim")
                + txt("+")
                + AUTO_AIM
                + " "
                + txt("percent")
                ,3
                ,HL
                ,txt("+")+AUTO_AIM+" "+txt("percent")
        );
        
        tooltip.addPara(
                txt("hm_virtuousEffect_ppt")
                + txt("+")
                + PPT
                + " "
                + txt("s")
                ,3
                ,HL
                ,txt("+")+PPT+" "+txt("s")
        );
        
        tooltip.setBulletedListMode(null);
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return true;	
    }
}
