package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_berserkStats extends BaseShipSystemScript {


    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        stats.getMaxTurnRate().modifyMult(id, 1+effectLevel);
        stats.getArmorDamageTakenMult().modifyMult(id, 1-(effectLevel*0.33f));

        stats.getWeaponTurnRateBonus().modifyMult(id, 1+effectLevel);
        stats.getAutofireAimAccuracy().modifyMult(id, 1+effectLevel);
        stats.getMaxRecoilMult().modifyMult(id, 1-(effectLevel*0.33f));

        stats.getEnergyWeaponRangeBonus().modifyMult(id, 1+(effectLevel/2));        
        stats.getEnergyRoFMult().modifyMult(id, 1+(effectLevel*0.5f));
        stats.getBallisticRoFMult().modifyMult(id,1+(effectLevel*0.5f));
        stats.getProjectileSpeedMult().modifyMult(id, 1+effectLevel);
        
        
        if(effectLevel<1){
            stats.getEnergyWeaponFluxCostMod().modifyFlat(id, 1000);
        } else {
            stats.getEnergyWeaponFluxCostMod().unmodify(id);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        stats.getMaxTurnRate().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);

        stats.getWeaponTurnRateBonus().unmodify(id);
        stats.getAutofireAimAccuracy().unmodify(id);
        stats.getMaxRecoilMult().unmodify(id);
        
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getProjectileSpeedMult().unmodify(id);
        
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
    }
    

    private final String TXT1 = txt("berserker");
    private final String TXT2 = txt("%");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float bonusPercent = (int) (effectLevel * 100f);
        if (index == 0) {
            return new StatusData(TXT1 + (int) bonusPercent + TXT2, false);
        }
        return null;
    }
}
