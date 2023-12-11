package data.missions.da_itanoCircus;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class MissionDefinition implements MissionDefinitionPlugin {
    @Override
    public void defineMission(MissionDefinitionAPI api) {

        // Set up the fleets so we can add ships and fighter wings to them.
        // In this scenario, the fleets are attacking each other, but
        // in other scenarios, a fleet may be defending or trying to escape
        api.initFleet(FleetSide.PLAYER, "USN", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "ITN", FleetGoal.ATTACK, true);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Wanzer pilot candidates");
        api.setFleetTagline(FleetSide.ENEMY, "VIRTUAL ENTITIES");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Survive");

        // Set up the player's fleet.  Variant names come from the
        // files in data/variants and data/variants/fighters
        api.addToFleet(FleetSide.PLAYER, "diableavionics_valiant_Standard", FleetMemberType.SHIP, true);       


        // Set up the enemy fleet.
        api.addToFleet(FleetSide.ENEMY, "buffalo_d_Standard", FleetMemberType.SHIP, false);		

        // Set up the map.
        float width = 1080f;
        float height = 1080f;
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
        api.addObjective(minX + width * 0.75f, minY + height * 0.5f, "nav_buoy");
        api.addObjective(minX + width * 0.25f, minY + height * 0.5f, "nav_buoy");   

        api.addPlugin(new Plugin());
    }
    

    private float timer = 0;
    private int clock=0;
    ShipAPI player, enemy;
    
    public class Plugin extends BaseEveryFrameCombatPlugin {        
        
        ////////////////////////////////////
        //                                //
        //      BATTLE INITIALISATION     //
        //                                //
        ////////////////////////////////////        

        @Override
        public void init(CombatEngineAPI engine) {
            timer=0;
            clock=0;
        }
        
        
        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            CombatEngineAPI engine = Global.getCombatEngine();   
            
            if(player==null && !engine.getFleetManager(FleetSide.PLAYER).getDeployedCopy().isEmpty()){
                player = engine.getFleetManager(FleetSide.PLAYER).getShipFor(engine.getFleetManager(FleetSide.PLAYER).getDeployedCopy().get(0));
            }
            
            if(enemy==null && !engine.getFleetManager(FleetSide.ENEMY).getDeployedCopy().isEmpty()){
                enemy = engine.getFleetManager(FleetSide.ENEMY).getShipFor(engine.getFleetManager(FleetSide.ENEMY).getDeployedCopy().get(0));
            }
            
            if(player==null || enemy==null )return;
            
            engine.setDoNotEndCombat(true);
            
            //keep enemy ship away
            Vector2f location = enemy.getLocation();
            location.scale(0);
            Vector2f.add(location, new Vector2f(2000,0), location);
            
            engine.getFogOfWar(1).revealAroundPoint(enemy, 0, 0, 2000);
            
            //timer
            if(engine.getPlayerShip()!=null && engine.getPlayerShip().isAlive()){
                timer+=amount;            
                if(timer>clock+1){
                    clock++;
                    engine.addFloatingText(new Vector2f(0,500), ""+clock, 20+clock/10, Color.yellow, null, .1f, .1f);
                    Vector2f point = MathUtils.getPoint(new Vector2f(), 1500, clock*30);
                    CombatEntityAPI missile = engine.spawnProjectile(enemy, null, "diableavionics_thrush", point, VectorUtils.getFacing(point)+180, new Vector2f());
                }
            }
        }
    }
}
