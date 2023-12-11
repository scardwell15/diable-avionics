package data.shipsystems.scripts;

//import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
//import com.fs.starfarer.api.combat.ShipAPI;
//import com.fs.starfarer.api.combat.WeaponAPI;
//import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import org.apache.log4j.Logger;
//import org.lazywizard.lazylib.FastTrig;

public class Diableavionics_fluxRedirection extends BaseShipSystemScript {

    private final float 
//            ACCURACY_LOSS = 1f,
            ROF_BONUS_PERCENT = 50,
            BEAM_BONUS_PERCENT = 50,
            FLUX_REDUCTION = 0.5f;
//    private Map<WeaponAPI,List<Float>> affected = new HashMap<>();
//    private float timer=0;
    
//    private Logger log = Global.getLogger(Diableavionics_fluxRedirection.class);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        float beamPercent = BEAM_BONUS_PERCENT * effectLevel;
        stats.getBeamWeaponDamageMult().modifyPercent(id, beamPercent);                

        float rofPercent = ROF_BONUS_PERCENT * effectLevel;
        stats.getBallisticRoFMult().modifyPercent(id, rofPercent);
        stats.getEnergyRoFMult().modifyPercent(id, rofPercent);

        float fluxMult = 1-(FLUX_REDUCTION* effectLevel);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id,fluxMult);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id,fluxMult);
        stats.getBeamWeaponFluxCostMult().modifyMult(id,fluxMult);

//        if(affected.isEmpty()){
//            for(WeaponAPI w : ((ShipAPI)stats.getEntity()).getUsableWeapons()){
//                if(!w.getSlot().isHardpoint()|| w.getSlot().getWeaponType()==WeaponType.MISSILE){
//                    continue;
//                }
//                w.ensureClonedSpec();
//                affected.put(w, new ArrayList<>(w.getSpec().getHardpointAngleOffsets()));
//            }
//            
//            for (WeaponAPI w : affected.keySet()){
//                for (int j=0; j<affected.get(w).size(); j++){
//                    log.info(w.getId() + "hardpoint angle: " + affected.get(w).get(j));
//                }
//            }
//            
//        } else {
//            if(!Global.getCombatEngine().isPaused()){
//                
//                float elapsed=Global.getCombatEngine().getElapsedInLastFrame();  
//                timer+=elapsed*5;
//                
//                for (WeaponAPI w : affected.keySet()){
//                    for (int i=0; i<affected.get(w).size(); i++){
//                        float base = affected.get(w).get(i);
//                        float offset = (float)FastTrig.sin(timer)*effectLevel*ACCURACY_LOSS;                        
//                        w.getSpec().getHardpointAngleOffsets().set(i, base + offset);
//                    }
//                }
//            }
//        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getBeamWeaponDamageMult().unmodify(id);
//        stats.getMaxRecoilMult().unmodify(id);
                
//        for (WeaponAPI w : affected.keySet()){
//            for (int i=0; i<affected.get(w).size(); i++){                     
//                w.getSpec().getHardpointAngleOffsets().set(i, affected.get(w).get(i));
//                log.info(w.getId() + "hardpoint reset to : " + affected.get(w).get(i));
//            }
//        }       
//        
//        affected.clear();        
//        timer=0;
    }

    
    private final String TXT1 = txt("redirection1");
    private final String TXT2 = txt("%");
    private final String TXT3 = txt("redirection2");
//    private final String TXT4 = txt("redirection3");
//    private final String TXT5 = txt("redirection4");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        int mult = Math.round(ROF_BONUS_PERCENT * effectLevel);
//        int acc = Math.round(ACCURACY_LOSS * effectLevel * 2);
        int flux = Math.round(FLUX_REDUCTION*100 * effectLevel);
        if (index == 0) {
                return new StatusData(TXT1 + mult + TXT2, false);
        }
        if (index == 1) {
                return new StatusData(TXT3 + flux + TXT2, false);
        }
//        if (index == 2) {
//                return new StatusData(TXT4 + acc+ TXT5, false);
//        }
        return null;
    }
}
