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

public class DiableAvionicsVirtuous_teleport extends BaseHullMod {
    
    private final int DISSIPATION = 150, CAPACITY=1000;
    
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
        
        stats.getFluxDissipation().modifyFlat(id, DISSIPATION);
        stats.getFluxCapacity().modifyFlat(id, CAPACITY);
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
                txt("hm_virtuousEffect_dissipation")
                + txt("+")
                + DISSIPATION
                + " "
                + txt("fps")
                ,10
                ,HL
                ,txt("+")+DISSIPATION+" "+txt("fps")
        );
        
        tooltip.addPara(
                txt("hm_virtuousEffect_capacity")
                + txt("+")
                + CAPACITY
                + " "
                + txt("f")
                ,3
                ,HL
                ,txt("+")+CAPACITY+" "+txt("f")
        );
        
        tooltip.setBulletedListMode(null);
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return true;	
    }
}
