package data.missions.da_tminuszero;

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
		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Sprear-head formation");
		api.setFleetTagline(FleetSide.ENEMY, "Temporary delay");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Launch missiles for maximum PD saturation.");
		api.addBriefingItem("Use your shield sparingly at high flux, your shielding is merely average.");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
	
                api.addToFleet(FleetSide.PLAYER, "diableavionics_maelstrom_standard", FleetMemberType.SHIP, true);		   
                api.addToFleet(FleetSide.PLAYER, "diableavionics_storm_standard", FleetMemberType.SHIP, false);	               
                api.addToFleet(FleetSide.PLAYER, "diableavionics_gust_standard", FleetMemberType.SHIP, false);	
                api.addToFleet(FleetSide.PLAYER, "diableavionics_haze_standard", FleetMemberType.SHIP, false);	             
                api.addToFleet(FleetSide.PLAYER, "diableavionics_daze_combat", FleetMemberType.SHIP, false);	            
                api.addToFleet(FleetSide.PLAYER, "diableavionics_calm_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_hayle_standard", FleetMemberType.SHIP, false);	                  
                api.addToFleet(FleetSide.PLAYER, "diableavionics_draft_standard", FleetMemberType.SHIP, false);                 
                api.addToFleet(FleetSide.PLAYER, "diableavionics_draft_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_vapor_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_vapor_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_sleet_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_sleet_standard", FleetMemberType.SHIP, false);
		
		// Set up the enemy fleet.
		//api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);		
		//api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, false);	
		api.addToFleet(FleetSide.ENEMY, "onslaught_xiv_Elite", FleetMemberType.SHIP, false);	
		api.addToFleet(FleetSide.ENEMY, "onslaught_Elite", FleetMemberType.SHIP, false);	
		api.addToFleet(FleetSide.ENEMY, "dominator_XIV_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "dominator_AntiCV", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "mora_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "mora_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
                
                
		// Set up the map.
		float width = 15000f;
		float height = 11000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 7; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 800f; 
			api.addNebula(x, y, radius);
		}
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.

		api.addObjective(minX + width * 0.7f, minY + height * 0.25f, "sensor_array");
		api.addObjective(minX + width * 0.8f, minY + height * 0.75f, "nav_buoy");
		api.addObjective(minX + width * 0.2f, minY + height * 0.25f, "nav_buoy");
		
		
	}

}
