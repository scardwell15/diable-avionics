package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_kataStats extends BaseShipSystemScript {

    private final float MAX_MULT=4;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        WeaponAPI left=null,right=null;
        for(WeaponAPI w : ((ShipAPI)stats.getEntity()).getAllWeapons()){
            if(w.getSlot().getId().endsWith("M_L")){
                left=w;
            } 
            if(w.getSlot().getId().endsWith("M_R")){
                right=w;
            }
        }
        //null pointer protection
        if(left==null||right==null)return;
        
        //warp is active if no weapon is firing, that is charge level >0.75
        float warp=effectLevel;
        warp-=Math.max(0, Math.min(1,-4+left.getChargeLevel()*8));
        warp-=Math.max(0, Math.min(1,-4+right.getChargeLevel()*8));
        //clamp
        warp=1+Math.min(1, Math.max(0, warp));
        
        stats.getTimeMult().modifyMult(id, warp*MAX_MULT);
        
        //the fighter is held mostly in place while the warp is active
        stats.getMaxSpeed().modifyMult(id, 1 - effectLevel);
        stats.getEntity().getVelocity().scale(0.9f);
        
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getTimeMult().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
    }

    private final String TXT = txt("gunKata");
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(TXT, false);
        }
        return null;
    }
}
