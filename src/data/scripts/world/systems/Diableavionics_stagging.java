package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.scripts.world.DiableavionicsGen;
import org.apache.log4j.Logger;

import java.awt.*;

import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_stagging {
    private Logger log = Global.getLogger(Diableavionics_stagging.class);

    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem(txt("star_B"));
        system.setOptionalUniqueId("diableavionics_stagging");

        system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar(txt("star_B_star_1"), // unique id for this star
                "star_red_dwarf", // id in planets.json
                350f,
                75);        // radius (in pixels at default zoom)
        system.setLightColor(new Color(255, 200, 175)); // light color in entire system, affects all entities

        system.getLocation().set(20000, -8000);

        SectorEntityToken DAstation = system.addCustomEntity(
                "diableavionics_eclipse",
                txt("star_B_station_1"),
                "diableavionics_station_eclipse",
                "diableavionics"
        );
        DAstation.setCustomDescriptionId("diableavionics_station_eclipse");
        float angle = (float) Math.random() * 360;
        DAstation.setCircularOrbitPointingDown(star, angle, 900, 45);

        //JUMP POINT
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("99_jumpPoint",
                txt("star_B_jp_0")
        );
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, angle + 60, 900, 45);
        jumpPoint.setOrbit(orbit);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.OLD,
                5, 7, // min/max entities to add
                750, // radius to start adding at
                0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                false); // whether to use custom or system-name based names

        system.autogenerateHyperspaceJumpPoints(true, true, true);
        system.setEnteredByPlayer(true);
        Misc.setAllPlanetsSurveyed(system, true);
        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
            market.setSurveyLevel(MarketAPI.SurveyLevel.FULL); // could also be a station, not a planet
        }


        cleanup(system);

        //fun stuff    
        boolean surprise = true;
        for (StarSystemAPI s : sector.getStarSystems()) {
            if (!s.isProcgen() && Math.random() < 0.2f) {
                for (PlanetAPI p : s.getPlanets()) {
                    if (p.getMarket() == null && Math.random() < 0.2f) {
                        addDerelict(s, p, "diableavionics_pandemonium_gutsRipper", ShipCondition.AVERAGE, p.getRadius() * 2, (Math.random() < 0.1));
                        surprise = false;
                        log.info("Surprise: " + s.getName() + " " + p.getFullName());
                        break;
                    }
                }
            }
            if (!surprise) {
                break;
            }
        }
    }

    void cleanup(StarSystemAPI system) {
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }

    protected void addDerelict(StarSystemAPI system,
                               SectorEntityToken focus,
                               String variantId,
                               ShipRecoverySpecial.ShipCondition condition,
                               float orbitRadius,
                               boolean recoverable) {
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
        ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);

        if (recoverable) {
            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
    }
}