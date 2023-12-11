package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_dakkaStats extends BaseShipSystemScript {


    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        stats.getMaxSpeed().modifyMult(id, 1-(effectLevel*0.75f));
        stats.getMaxTurnRate().modifyMult(id, 1-(effectLevel*0.5f));

        stats.getArmorDamageTakenMult().modifyMult(id, 1-(effectLevel*0.5f));

        stats.getWeaponTurnRateBonus().modifyMult(id, 1+effectLevel);
        stats.getAutofireAimAccuracy().modifyMult(id, 1+effectLevel);
        stats.getBallisticWeaponRangeBonus().modifyMult(id, 1+(effectLevel/2));
        stats.getNonBeamPDWeaponRangeBonus().modifyMult(id, 1-(effectLevel/2));
        stats.getMaxRecoilMult().modifyMult(id, 1-effectLevel+0.5f);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);

        stats.getArmorDamageTakenMult().unmodify(id);

        stats.getWeaponTurnRateBonus().unmodify(id);
        stats.getAutofireAimAccuracy().unmodify(id);
        stats.getBallisticWeaponRangeBonus().unmodify(id);
        stats.getNonBeamPDWeaponRangeBonus().unmodify(id);
        stats.getMaxRecoilMult().unmodify(id);
    }

    private final String TXT = txt("dakka");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(TXT, false);
        }
        return null;
    }
}
