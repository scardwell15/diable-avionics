package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.scripts.world.DiableavionicsGen;

import java.awt.*;

import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_outerTerminus implements SectorGeneratorPlugin {

//    public static SectorEntityToken getSectorAccess() {
//        return Global.getSector().getStarSystem(txt("star_C")).getEntityByName("");
//    }

    @Override
    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem(txt("star_C"));
        system.setOptionalUniqueId("diableavionics_outerTerminus");
        system.setBackgroundTextureFilename("graphics/da/backgrounds/diableavionics_outerTerminus.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar(txt("star_C_star_1"), // unique id for this star
                "star_white", // id in planets.json
                450f,
                250);        // radius (in pixels at default zoom)
        system.setLightColor(new Color(255, 250, 250)); // light color in entire system, affects all entities

        system.getLocation().set(29000, -5000);
        /*
         * addPlanet() parameters:
         * 1. What the planet orbits (orbit is always circular)
         * 2. Name
         * 3. Planet type id in planets.json
         * 4. Starting angle in orbit, i.e. 0 = to the right of the star
         * 5. Planet radius, pixels at default zoom
         * 6. Orbit radius, pixels at default zoom
         * 7. Days it takes to complete an orbit. 1 day = 10 seconds.
         */
        /*
         * addAsteroidBelt() parameters:
         * 1. What the belt orbits
         * 2. Number of asteroids
         * 3. Orbit radius
         * 4. Belt width
         * 6/7. Range of days to complete one orbit. Value picked randomly for each asteroid.
         */
        /*
         * addRingBand() parameters:
         * 1. What it orbits
         * 2. Category under "graphics" in settings.json
         * 3. Key in category
         * 4. Width of band within the texture
         * 5. Index of band
         * 6. Color to apply to band
         * 7. Width of band (in the game)
         * 8. Orbit radius (of the middle of the band)
         * 9. Orbital period, in days
         */
//        private void addMarketplace(
//                    String factionID, 
//                    SectorEntityToken primaryEntity, 
//                    ArrayList<SectorEntityToken> connectedEntities, 
//                    String name, 
//                    int size, 
//                    ArrayList<String> marketConditions, 
//                    ArrayList<String> submarkets, 
//                    float tarrif)


        //2000
        PlanetAPI OT1 = system.addPlanet("OT_a",
                star,
                txt("star_C_planet_0"),
                "rocky_unstable",
                25,
                80,
                2000,
                150
        );

        //JUMP POINT
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("OT_jumpPointA",
                txt("star_C_jp_0")
        );
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 85, 2000, 150);
        jumpPoint1.setOrbit(orbit);
        jumpPoint1.setRelatedPlanet(OT1);
        jumpPoint1.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint1);

        //3000
        PlanetAPI ach2 = system.addPlanet("diableavionics_prison",
                star,
                txt("star_C_planet_1"),
                "terran",
                180,
                150,
                3000,
                250
        );
        ach2.setCustomDescriptionId("diableavionics_prison");

        //3750
        //ASTEROID BELT
        system.addAsteroidBelt(star, 750, 3750, 512, 310, 330);
        SectorEntityToken DA_piratePort = system.addCustomEntity("diableavionics_ressource",
                txt("star_C_station_1"),
                "diableavionics_station_ressource",
                "diableavionics");
        DA_piratePort.setCircularOrbitPointingDown(star, 62, 4000, 335);

        //OLD RELAY
        SectorEntityToken relay = system.addCustomEntity("OT_abandonned_relay", // unique id
                txt("star_C_relay"), // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                "diableavionics"); // faction
        relay.setCircularOrbit(star, 150, 4250, 350);

        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.AVERAGE,
                5, 9, // min/max entities to add
                5500, // radius to start adding at
                2, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names

//        //7000
//        PlanetAPI ach3 = system.addPlanet("OT_c", star, "Vun", "ice_giant", 180, 300, 7000, 700);
//
//        //12000
//        PlanetAPI ach5 = system.addPlanet("OT_e", star, "Lema", "gas_giant", 80, 325, 12000, 1200);
//        //ASTEROIDS
//        system.addAsteroidBelt(ach5, 50, 600, 128, 39, 45); 
//        
//            //JUMP POINT
//            JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("OT_jumpPointB", "Lema Jump-Point");
//            OrbitAPI orbit2 = Global.getFactory().createCircularOrbit(star, 20, 12000, 1200);
//            jumpPoint2.setOrbit(orbit2);
//            jumpPoint2.setStandardWormholeToHyperspaceVisual();
//            system.addEntity(jumpPoint2);
//
//        //15000
//        PlanetAPI ach6 = system.addPlanet("OT_f", star, "Tid", "frozen", 75, 75, 15000, 2000);

        system.autogenerateHyperspaceJumpPoints(true, true, true);
        system.setEnteredByPlayer(true);
        Misc.setAllPlanetsSurveyed(system, true);
        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
            market.setSurveyLevel(MarketAPI.SurveyLevel.FULL); // could also be a station, not a planet
        }

        cleanup(system);
    }

    void cleanup(StarSystemAPI system) {
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }
}
