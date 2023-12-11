package data.hullmods;

//import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
//import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import org.apache.log4j.Logger;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;

public class DiableAvionicsWanzer extends BaseHullMod {
	
    private final float EMP_RESIST=33, DISABLE_RESIST=66;
    /*
    //wanzer gantry
    private Map<String, Float> BASE_VALUES = new HashMap<>();    
    private final String PATH= "data/hulls/wanzers_data.csv";
    private Logger log = Global.getLogger(DiableAvionicsUniversalDecksUpgrade.class);
    */
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
//        if(((ShipAPI)stats.getEntity()).getWing()!= null && ((ShipAPI)stats.getEntity()).getWing().getSourceShip().getVariant().getHullMods().contains("diableavionics_universaldecksExtra")){
//            ((ShipAPI)stats.getEntity()).getWing().getSpec().setNumFighters(Global.getSettings().getFighterWingSpec(((ShipAPI)stats.getEntity()).getWing().getWingId()).getNumFighters()+1);
//        }
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) EMP_RESIST + "%";
        }
        if (index == 1) {
            return "" + (int) DISABLE_RESIST + "%";
        }
        return null;
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getMutableStats().getEmpDamageTakenMult().modifyMult(id, (100-EMP_RESIST)/100);
        ship.getMutableStats().getEngineDamageTakenMult().modifyMult(id, (100-DISABLE_RESIST)/100);
        ship.getMutableStats().getWeaponDamageTakenMult().modifyMult(id, (100-DISABLE_RESIST)/100);
        /*
        //wanzer gantry
        log.info("WANZER GANTRY - checking "+ship.getHullSpec().getBaseHullId());
        
        if(BASE_VALUES.isEmpty()){
            try{                
                JSONArray wanzer = Global.getSettings().getMergedSpreadsheetDataForMod("id", PATH, "diableavionics");

                for(int i = 0; i < wanzer.length(); i++) {            
                    JSONObject row = wanzer.getJSONObject(i);
                    BASE_VALUES.put(row.getString("id"), (float)row.getDouble("time"));
                }
            } catch (IOException | JSONException ex) {
                log.error("unable to read wanzers_data.csv");
                return;
            }
        }        
        */
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship)
    {
        // Allows any ship with a diableavionics hull id
        return ( ship.getHullSpec().getHullId().startsWith("diableavionics_"));	
    }
}
