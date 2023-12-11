package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
//import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.Map;
//import java.util.Set;

public class DiableAvionicsUpgrade extends BaseHullMod {

//    private final float SHIELD_BONUS_UNFOLD = 200f;
    private final float CHECK=1f;
    private float timer=0, previous=0;
    private final String ID="Target Analysis";    
        
//    @Override
//    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
//        //unfold rate
//        stats.getShieldUnfoldRateMult().modifyPercent(id, SHIELD_BONUS_UNFOLD);	
//    }
    
        private class BuffData {
        float increment;
        float autoaimMin;
        float autoaimMax;
        float accelBuff;
        float turnBuff;
        float weaponTurn;

        public BuffData(float increment, float autoaimMin, float autoaimMax, float accelBuff, float turnBuff, float weaponTurn) {
            this.increment = increment;
            //the effect ranges from 0 to 1. ie an increment of 1 goes straight from 0 to 1, while an increment of 0.25 needs 4 steps.
            this.autoaimMin = autoaimMin;
            this.autoaimMax = autoaimMax;
            this.accelBuff = accelBuff;
            this.turnBuff = turnBuff;
            this.weaponTurn = weaponTurn;
        }
    }    
    private final Map<ShipAPI.HullSize, BuffData> WEAPON_BUFF = new HashMap<>();
    {
        WEAPON_BUFF.put(ShipAPI.HullSize.DEFAULT, new BuffData(1f/4f, -15, +50, 50, 50, 30));
        WEAPON_BUFF.put(ShipAPI.HullSize.CAPITAL_SHIP, new BuffData(1f/5f, -15, +100, 100, 100, 50));
        WEAPON_BUFF.put(ShipAPI.HullSize.CRUISER, new BuffData(1f/4f, -15, +50, 50, 50, 30));
        WEAPON_BUFF.put(ShipAPI.HullSize.DESTROYER, new BuffData(1f/3f, -15, +30, 35, 35, 20));
        WEAPON_BUFF.put(ShipAPI.HullSize.FRIGATE, new BuffData(1f/2f, -15, +15, 25, 25, 15));
        WEAPON_BUFF.put(ShipAPI.HullSize.FIGHTER, new BuffData(0f, 0, 0, 0, 0, 0));
    }
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        timer=Global.getCombatEngine().getTotalElapsedTime(false);  
        
        ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
        if(previous>=timer || timer>previous+CHECK || ship==playerShip){
            
            if(ship!=playerShip){
                previous=timer;
            }

            int tick = (int)ship.getTimeDeployedForCRReduction()/60;
            float effectLevel = Math.min(1, (WEAPON_BUFF.get(ship.getHullSize()).increment)*tick);
            float autoaimEffect = (WEAPON_BUFF.get(ship.getHullSize()).autoaimMin) + effectLevel * ((WEAPON_BUFF.get(ship.getHullSize()).autoaimMax)-(WEAPON_BUFF.get(ship.getHullSize()).autoaimMin));

            ship.getMutableStats().getWeaponTurnRateBonus().modifyMult(ID, 1+((WEAPON_BUFF.get(ship.getHullSize()).weaponTurn)*effectLevel/100));            
            ship.getMutableStats().getAutofireAimAccuracy().modifyMult(ID, 1+(autoaimEffect/100));
            ship.getMutableStats().getRecoilDecayMult().modifyMult(ID, 1+(autoaimEffect/100));
            ship.getMutableStats().getRecoilPerShotMult().modifyMult(ID, 1-(autoaimEffect/100));   

            ship.getMutableStats().getAcceleration().modifyMult(ID, 1+((WEAPON_BUFF.get(ship.getHullSize()).accelBuff)*effectLevel/100));
            ship.getMutableStats().getDeceleration().modifyMult(ID, 1+((WEAPON_BUFF.get(ship.getHullSize()).accelBuff)*effectLevel/100));
            ship.getMutableStats().getTurnAcceleration().modifyMult(ID, 1+((WEAPON_BUFF.get(ship.getHullSize()).turnBuff)*effectLevel/100));            

            EnumSet WEAPON_TYPES = EnumSet.of(WeaponType.BALLISTIC,WeaponType.ENERGY);
            ship.setWeaponGlow(
                    effectLevel,
                    new Color(0.25f*(effectLevel+1)+0.25f,0.4f*(effectLevel+1)+0.1f,0.05f*(effectLevel+1)+0.05f),
                    WEAPON_TYPES
            );

            if(ship==playerShip){
                Global.getCombatEngine().maintainStatusForPlayerShip(
                        "AdvancedAvionicsBoost",
                        "graphics/icons/hullsys/high_energy_focus.png",
                        "Patterns Analysis:",                        
                        Math.round(effectLevel*100)+"% complete.",
                        effectLevel < 0.5f);
            }
        }
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "-15%";
        }
        if (index == 1) {
            return "60 seconds";
        }
        if (index == 2) {
            return "120/180/240/300 seconds";
        }
        if (index == 3) {
            return "25/35/50/100 percent";
        }
        if (index == 4) {
            return "15/30/50/100 percent";
        }
        
        return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a diableavionics hull id
        return ( ship.getHullSpec().getHullId().startsWith("diableavionics_"));	
    }
}
