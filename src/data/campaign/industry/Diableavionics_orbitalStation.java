package data.campaign.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class Diableavionics_orbitalStation extends OrbitalStation {
    
    @Override
    public boolean isAvailableToBuild() {
        boolean canBuild = super.isAvailableToBuild();

        SectorAPI sector = Global.getSector();

        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI diableavionics = sector.getFaction("diableavionics");

        /* If we have a battlestation already, that means we're checking if we can downgrade -- which we should be able
         * to do, regardless of reputation.
         */
        if (!market.hasIndustry("diableavionics_battlestation")) {
            if ((market.getPlanetEntity() != null) && !(player.getRelationshipLevel(diableavionics).isAtWorst(RepLevel.WELCOMING) || Global.getSector().getPlayerFaction().knowsIndustry(getId()))) {
                canBuild = false;
            }
        }

        return canBuild;
    }

    @Override
    public String getUnavailableReason() {
        return "Reputation too low";
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }
}