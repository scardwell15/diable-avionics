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

public class Diableavionics_fob {

    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem(txt("star_A"));
        system.setOptionalUniqueId("diableavionics_fob");
        system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar(txt("star_A_star_1"), // unique id for this star
                "star_browndwarf", // id in planets.json
                300f,
                50);        // radius (in pixels at default zoom)
        system.setLightColor(new Color(200, 125, 75)); // light color in entire system, affects all entities

        system.getLocation().set(10000, -13000);

        SectorEntityToken DAfob = system.addCustomEntity("diableavionics_shadow",
                txt("star_A_station_1"),
                "diableavionics_station_shadow",
                "diableavionics"
        );
        DAfob.setCustomDescriptionId("diableavionics_station_shadow");
        float angle = (float) Math.random() * 360;
        DAfob.setCircularOrbitPointingDown(star, angle, 800, 40);

        //JUMP POINT
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("86_jumpPoint",
                txt("star_A_jp_0")
        );
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, angle - 60, 800, 40);
        jumpPoint.setOrbit(orbit);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.OLD,
                5, 7, // min/max entities to add
                850, // radius to start adding at
                0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                false); // whether to use custom or system-name based names

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