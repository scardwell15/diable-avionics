package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_evasionStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {        
        stats.getMaxSpeed().modifyFlat(id, 1000f*effectLevel);
        stats.getAcceleration().modifyPercent(id, 4000f * effectLevel);
        stats.getDeceleration().modifyPercent(id, 4000f * effectLevel);
        stats.getTurnAcceleration().modifyPercent(id, 1000f * effectLevel);
        stats.getMaxTurnRate().modifyPercent(id, 1000f*effectLevel);
        stats.getHullDamageTakenMult().modifyPercent(id, 100-50*effectLevel);
        stats.getArmorDamageTakenMult().modifyPercent(id, 100-50*effectLevel);
        
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if(ship!=null){
                if(effectLevel>0){
                ship.setCollisionClass(CollisionClass.NONE);
            } else {
                if(ship.isFighter()){
                    ship.setCollisionClass(CollisionClass.FIGHTER);
                } else {
                    ship.setCollisionClass(CollisionClass.SHIP);
                }
            }
        }
        
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id); 
        
        ShipAPI ship = (ShipAPI) stats.getEntity();    
        if(ship!=null){
            if(ship.isFighter()){
                ship.setCollisionClass(CollisionClass.FIGHTER);
            } else {
                ship.setCollisionClass(CollisionClass.SHIP);
            }
        }
    }
    private final String TXT = txt("evasion");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(TXT, false);
        }
        return null;
    }
}