package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
//import com.fs.starfarer.api.combat.ShipAPI;
//import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import static data.scripts.util.Diableavionics_stringsManager.txt;
//import data.scripts.weapons.Diableavionics_derechoEffect;

public class Diableavionics_quantumimpulseStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
    }

    private final String TXT = txt("quantum");
    @Override
    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData(TXT, false);
        }
        return null;
    }
}