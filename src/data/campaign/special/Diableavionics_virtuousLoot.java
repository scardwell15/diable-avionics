package data.campaign.special;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicCampaign;

import java.util.List;

import static data.scripts.util.Diableavionics_stringsManager.txt;

/**
 * @author Tartiflette
 */

public class Diableavionics_virtuousLoot implements FleetEventListener {

    private final String VIRTUOUS_DROP_ALREADY = "$virtuous_drop";

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

        // ignore that whole ordeal if the Virtuous already dropped
        if (Global.getSector().getMemoryWithoutUpdate().contains(VIRTUOUS_DROP_ALREADY)
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(VIRTUOUS_DROP_ALREADY)) {
            return;
        }

        if (fleet.getFlagship() == null || !fleet.getFlagship().getHullSpec().getBaseHullId().startsWith("diableavionics_virtuous")) {

            //remove the fleet if flag is dead
            if (!fleet.getMembersWithFightersCopy().isEmpty()) {
                SectorEntityToken source = fleet.getCurrentAssignment().getTarget();
                fleet.clearAssignments();
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source, 9999);
            }

            //boss is dead, 
            boolean salvaged = false;
            for (FleetMemberAPI f : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
                if (f.getHullId().startsWith("diableavionics_virtuous")) salvaged = true;

                //set memkey that the wreck must never spawn
                Global.getSector().getMemoryWithoutUpdate().set(VIRTUOUS_DROP_ALREADY, true);
            }

            //spawn a derelict if it wasn't salvaged
            if (!salvaged) {

                //check around if there is an existing wreck to remove just in case
                List<SectorEntityToken> wrecks = fleet.getStarSystem().getEntitiesWithTag(Tags.WRECK);
                if (!wrecks.isEmpty()) {
                    for (SectorEntityToken t : wrecks) {
                        if (t.getCustomEntitySpec().getSpriteName().startsWith("diableavionics_virtuous")) {
                            fleet.getStarSystem().removeEntity(t);
                            break;
                        }
                    }
                }

                //make sure there is a valid location to avoid spawning in the sun
                Vector2f location = fleet.getLocation();
                if (location == new Vector2f()) {
                    location = primaryWinner.getLocation();
                }

                //spawn the derelict object
                SectorEntityToken wreck = MagicCampaign.createDerelict(
                        "diableavionics_virtuous_destroyed_Hull",
                        ShipRecoverySpecial.ShipCondition.WRECKED,
                        false,
                        -1,
                        true,
                        //orbitCenter,angle,radius,period);
                        fleet.getStarSystem().getCenter(),
                        VectorUtils.getAngle(fleet.getStarSystem().getCenter().getLocation(), location),
                        MathUtils.getDistance(fleet.getStarSystem().getCenter().getLocation(), location),
                        360
                );
                MagicCampaign.placeOnStableOrbit(wreck, true);
                wreck.setName(txt("virtuousShip"));
                wreck.setFacing((float) Math.random() * 360f);
                wreck.getMemoryWithoutUpdate().set(MemFlags.ENTITY_MISSION_IMPORTANT, true);

                //set memkey that the wreck exist
                Global.getSector().getMemoryWithoutUpdate().set(VIRTUOUS_DROP_ALREADY, true);
            }
        }
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        fleet.removeEventListener(this);
    }
}
