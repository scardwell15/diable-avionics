package data.shipsystems.scripts;

import java.awt.Color;
import java.util.EnumSet;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;
import java.util.ArrayList;
import java.util.List;

public class Diableavionics_assumeControlStats extends BaseShipSystemScript {
    
    private final Color JITTER_UNDER_COLOR = new Color(150,100,50,50);
    private final float MAX_TIME_MULT = 1.25f;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
                ship = (ShipAPI) stats.getEntity();
        } else {
                return;
        }
        
        if (effectLevel > 0) {
//            float jitterLevel = effectLevel;
            
            for (ShipAPI fighter : getFighters(ship)) {
                
                if (fighter.isHulk()) continue;
                
                //accelerate fighters
                float mult = stats.getSystemRangeBonus().getBonusMult();
                float shipTimeMult = 1f + (MAX_TIME_MULT+mult - 2f) * effectLevel;
		fighter.getMutableStats().getTimeMult().modifyMult(id, shipTimeMult);
                //visual effect
                if (effectLevel > 0) {
                    fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 50), EnumSet.allOf(WeaponType.class));
                    fighter.setJitterUnder(fighter, JITTER_UNDER_COLOR, effectLevel, (int)(5*effectLevel), 3, 3+3*effectLevel);
                }               
            }            
        }                
    }

    private List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();
        if(!carrier.getAllWings().isEmpty()){
            for(FighterWingAPI w : carrier.getAllWings()){
                for(ShipAPI f : w.getWingMembers()){
                    result.add(f);
                }
            }
        }
        return result;
    }
    
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
        
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();                
        } else {
            return;
        }

        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) continue;
            fighter.setWeaponGlow(0, Color.BLACK, EnumSet.allOf(WeaponType.class));
            fighter.getMutableStats().getTimeMult().unmodify(id);
        }
    }

    private final String TXT = txt("assumeControl");
    
    @Override
    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData(TXT, false);
        }
        return null;
    }
    
    private final String READY = txt("ready");
    private final String TARGET = txt("target");
    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        ShipAPI target = findTarget(ship);
        if (target != null) {
            return READY;
        } else {return TARGET;}
    }
    
    protected ShipAPI findTarget(ShipAPI ship) {
        if(!ship.getAllWings().isEmpty()){
            for(FighterWingAPI w : ship.getAllWings()){
                ShipAPI leader = w.getLeader();
                if(leader.isAlive() && !leader.isLanding() && !w.getReturning().contains(leader)){
                    return leader;
                } else {
                    for (ShipAPI f : w.getWingMembers()){
                        if(f!=leader && f.isAlive() && !f.isLanding() && !w.getReturning().contains(f)){
                            return f;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        ShipAPI target = findTarget(ship);
        return target != null;
    }
}