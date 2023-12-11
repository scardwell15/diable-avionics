package data.missions.da_escalation;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {
    @Override
    public void defineMission(MissionDefinitionAPI api) {


        // Set up the fleets so we can add ships and fighter wings to them.
        // In this scenario, the fleets are attacking each other, but
        // in other scenarios, a fleet may be defending or trying to escape
        api.initFleet(FleetSide.PLAYER, "DSF", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "SDS", FleetGoal.ATTACK, true);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Advanced Tactics Group");
        api.setFleetTagline(FleetSide.ENEMY, "Baited Patrol");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Your fleet has an unmatched mobility on the battlefield.");
        api.addBriefingItem("Divide and Conquer.");
        api.addBriefingItem("Capture points to give more orders.");

        // Set up the player's fleet.  Variant names come from the
        // files in data/variants and data/variants/fighters
        api.setHyperspaceMode(true);


        api.addToFleet(FleetSide.PLAYER, "diableavionics_versant_standard", FleetMemberType.SHIP,"DSF Coton Candy", true);
        api.addToFleet(FleetSide.PLAYER, "diableavionics_versant_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "diableavionics_versant_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "diableavionics_versant_standard", FleetMemberType.SHIP, false);  
        api.addToFleet(FleetSide.PLAYER, "diableavionics_storm_support", FleetMemberType.SHIP, false);
        //api.addToFleet(FleetSide.PLAYER, "diableavionics_fractus_standard", FleetMemberType.SHIP, false);
        //api.addToFleet(FleetSide.PLAYER, "diableavionics_fractus_standard", FleetMemberType.SHIP, false);

	//api.addToFleet(FleetSide.PLAYER, "diableavionics_valiant_wing", FleetMemberType.FIGHTER_WING, false)
        //api.addToFleet(FleetSide.PLAYER, "diableavionics_valiant_wing", FleetMemberType.FIGHTER_WING, false)   
        //api.addToFleet(FleetSide.PLAYER, "diableavionics_valiant_wing", FleetMemberType.FIGHTER_WING, false)
        //api.addToFleet(FleetSide.PLAYER, "diableavionics_valiant_wing", FleetMemberType.FIGHTER_WING, false)          

        // Mark both ships as essential - losing either one results
        // in mission failure. Could also be set on an enemy ship,
        // in which case destroying it would result in a win.

        // Set up the enemy fleet.                
        api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false);
        //api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.ENEMY, "hammerhead_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.ENEMY, "brawler_Elite", FleetMemberType.SHIP, false); 
        //api.addToFleet(FleetSide.ENEMY, "brawler_Elite", FleetMemberType.SHIP, false); 
        api.addToFleet(FleetSide.ENEMY, "brawler_Assault", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.ENEMY, "vigilance_AP", FleetMemberType.SHIP, false); 
        //api.addToFleet(FleetSide.ENEMY, "vigilance_AP", FleetMemberType.SHIP, false); 
        api.addToFleet(FleetSide.ENEMY, "vigilance_Strike", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.ENEMY, "heron_Attack", FleetMemberType.SHIP, false); 
        //api.addToFleet(FleetSide.ENEMY, "drover_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "drover_Strike", FleetMemberType.SHIP, false);

        //api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false); 	
        //api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false); 
        //api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false);	
        //api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);		
        //api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false); 
        //api.addToFleet(FleetSide.ENEMY, "piranha_wing", FleetMemberType.FIGHTER_WING, false); 		
        //api.addToFleet(FleetSide.ENEMY, "piranha_wing", FleetMemberType.FIGHTER_WING, false);

        // Set up the map.
        float width = 16000f;
        float height = 24000f;
        api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

        float minX = -width/2;
        float minY = -height/2;

        // All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.

        // And a few random ones to spice up the playing field.
        // A similar approach can be used to randomize everything
        // else, including fleet composition.
        for (int i = 0; i < 15; i++) {
            float x = (float) Math.random() * width - width/2;
            float y = (float) Math.random() * height - height/2;
            float radius = 50f + (float) Math.random() * 200f; 
            api.addNebula(x, y, radius);
        }

        // Add objectives. These can be captured by each side
        // and provide stat bonuses and extra command points to
        // bring in reinforcements.
        // Reinforcements only matter for large fleets - in this
        // case, assuming a 100 command point battle size,
        // both fleets will be able to deploy fully right away.

        api.addObjective(minX + width * (0.5f -0.125f + 0.25f*(float)Math.random()) , minY + height * (0.5f -0.125f + 0.25f*(float)Math.random()), "sensor_array");
        api.addObjective(minX + width * (0.75f -0.125f + 0.25f*(float)Math.random()), minY + height * (0.5f -0.125f + 0.25f*(float)Math.random()), "comm_relay");
        api.addObjective(minX + width * (0.25f -0.125f + 0.25f*(float)Math.random()), minY + height * (0.5f -0.125f + 0.25f*(float)Math.random()), "comm_relay");
    }
}
