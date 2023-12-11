package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;
import java.awt.Color;

public class Diableavionics_temporalShellStats extends BaseShipSystemScript {

    private final Integer TURN_ACC_BUFF = 200;
    private final Integer TURN_RATE_BUFF = 200;
    private final Integer ACCEL_BUFF = 200;
    private final Integer DECCEL_BUFF = 200;
    private final Integer SPEED_BUFF = 200;
    private final Integer TIME_BUFF = 400;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        
        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }
        
        //ship can reorient
        stats.getTurnAcceleration().modifyPercent(id, TURN_ACC_BUFF * effectLevel);
        stats.getMaxTurnRate().modifyPercent(id, TURN_RATE_BUFF * effectLevel);
        
        //ship can slightly jump forward
        stats.getMaxSpeed().modifyPercent(id, SPEED_BUFF * effectLevel);
        stats.getAcceleration().modifyPercent(id, ACCEL_BUFF);
        stats.getDeceleration().modifyPercent(id, DECCEL_BUFF);
        
        //time drift
        stats.getTimeMult().modifyPercent(id, TIME_BUFF * effectLevel);
        
        if (player) {
            //player go half speed
            float playerTimeMult = 1-(0.5f*effectLevel);
            Global.getCombatEngine().getTimeMult().modifyMult(id, playerTimeMult);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }

        ship.getEngineController().fadeToOtherColor(this, Color.CYAN, new Color(0,0,0,0), effectLevel, 0.5f);
        ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
        
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        
        stats.getTimeMult().unmodify(id);
        Global.getCombatEngine().getTimeMult().unmodify(id);
    }
    
    private final String TXT = txt("drift");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(TXT, false);
        }
        return null;
    }
}