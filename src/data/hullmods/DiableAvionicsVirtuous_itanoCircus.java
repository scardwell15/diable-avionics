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

public class DiableAvionicsVirtuous_itanoCircus extends BaseHullMod {
    
    private final int SPEED = 25, ACCELERATION=50, TURN_RATE=10, DP=5, BURN=1, CARGO=100, FUEL=150;
    
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
        
        stats.getMaxSpeed().modifyFlat(id, SPEED);
        stats.getAcceleration().modifyPercent(id, ACCELERATION);
        stats.getDeceleration().modifyPercent(id, ACCELERATION);
        stats.getMaxTurnRate().modifyFlat(id, TURN_RATE);
        stats.getTurnAcceleration().modifyPercent(id, ACCELERATION);
        
        stats.getSuppliesToRecover().modifyFlat(id, -DP);
        stats.getSuppliesPerMonth().modifyFlat(id, -DP);
        stats.getMaxBurnLevel().modifyFlat(id, BURN);
        stats.getCargoMod().modifyFlat(id, CARGO);
        stats.getFuelMod().modifyFlat(id, FUEL);
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
                txt("hm_virtuousEffect_speed")
                + txt("+")
                + SPEED
                + " "
                + txt("sups")
                ,10
                ,HL
                ,txt("+")+SPEED+" "+txt("sups")
        );
        
        tooltip.addPara(
                txt("hm_virtuousEffect_maneuv")
                + txt("+")
                + ACCELERATION
                + " "
                + txt("percent")
                ,3
                ,HL
                ,txt("+")+ACCELERATION+" "+txt("percent")
        );
        
        tooltip.addPara(
                txt("hm_virtuousEffect_supplies")
                + txt("-")
                + DP
                + " "
                + txt("supplies")
                ,3
                ,HL
                ,txt("-")+DP+" "+txt("supplies")
        );
        
        tooltip.addPara(
                txt("hm_virtuousEffect_burn")
                + txt("+")
                + BURN
                ,3
                ,HL
                ,txt("+")+BURN
        );
        
        tooltip.addPara(
                txt("hm_virtuousEffect_cargo")
                + txt("+")
                + CARGO
                ,3
                ,HL
                ,txt("+")+CARGO
        );
        
        tooltip.addPara(
                txt("hm_virtuousEffect_fuel")
                + txt("+")
                + FUEL
                ,3
                ,HL
                ,txt("+")+FUEL
        );
        
        tooltip.setBulletedListMode(null);
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return true;	
    }
}
