package data.campaign.special;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import static data.scripts.util.Diableavionics_stringsManager.txt;
import org.magiclib.util.MagicCampaign;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 * @author Tartiflette
 */

public class Diableavionics_gulfLoot implements FleetEventListener{
    
    private final String GULF_DROP_ALREADY = "$gulf_drop";
    
    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        
        // ignore that whole ordeal if the Virtuous already dropped
        if(Global.getSector().getMemoryWithoutUpdate().contains(GULF_DROP_ALREADY) 
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(GULF_DROP_ALREADY)){
            return;            
        }
        
        if(fleet.getFlagship()==null || !fleet.getFlagship().getHullSpec().getBaseHullId().equals("diableavionics_IBBgulf")){
            
            //remove the fleet if flag is dead
            if(!fleet.getMembersWithFightersCopy().isEmpty()){
                SectorEntityToken source = fleet.getCurrentAssignment().getTarget();
                fleet.clearAssignments();
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source , 9999);
            }
            
            //boss is dead, 
            boolean salvaged=false;
            for (FleetMemberAPI f : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()){
                if(f.getHullId().equals("diableavionics_IBBgulf")) salvaged=true;
                
                //set memkey that the wreck must never spawn
                Global.getSector().getMemoryWithoutUpdate().set(GULF_DROP_ALREADY,true); 
            }
            
            //spawn a derelict if it wasn't salvaged
            if(!salvaged){
             
                //check around if there is an existing wreck to remove just in case
                List<SectorEntityToken>wrecks = fleet.getStarSystem().getEntitiesWithTag(Tags.WRECK);
                if(!wrecks.isEmpty()){
                    for (SectorEntityToken t : wrecks){
                        if(t.getCustomEntitySpec().getSpriteName().startsWith("diableavionics_IBBgulf")){
                            fleet.getStarSystem().removeEntity(t);
                            break;
                        }
                    }
                }
                
                //make sure there is a valid location to avoid spawning in the sun
                Vector2f location = fleet.getLocation();
                if(location==new Vector2f()){
                    location = primaryWinner.getLocation();
                }
                
                //spawn the derelict object
                SectorEntityToken wreck = MagicCampaign.createDerelict(
                        "diableavionics_IBBgulf_Hull",
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
                wreck.setName(txt("gulfShip"));
                wreck.setFacing((float)Math.random()*360f);
                wreck.getMemoryWithoutUpdate().set(MemFlags.ENTITY_MISSION_IMPORTANT, true);
                //set memkey that the wreck must never spawn
                Global.getSector().getMemoryWithoutUpdate().set(GULF_DROP_ALREADY,true); 
            }
        }
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        fleet.removeEventListener(this);
    }
}
