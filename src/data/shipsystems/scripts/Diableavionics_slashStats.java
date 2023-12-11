package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_slashStats extends BaseShipSystemScript {

    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        
        stats.getMaxSpeed().modifyMult(id, 1-(0.75f*effectLevel));
        stats.getMaxTurnRate().modifyMult(id, 1+effectLevel*2);
        
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if(ship!=null){
            if(effectLevel==1){                
                stats.getMaxTurnRate().modifyMult(id, 1-(.75f*effectLevel));
                ship.setCollisionClass(CollisionClass.NONE);
            } else {
                ship.setCollisionClass(CollisionClass.FIGHTER);
            }
        }        
    }
    
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        ShipAPI ship = (ShipAPI) stats.getEntity();    
        ship.setCollisionClass(CollisionClass.FIGHTER);   
    }
    
    private final String TXT = txt("slash");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(TXT, false);
        }
        return null;
    }
}