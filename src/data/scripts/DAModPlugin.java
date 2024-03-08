package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.campaign.DACampaignPlugin;
import data.scripts.ai.*;
import data.scripts.world.DiableavionicsGen;
import data.scripts.world.MarketHelpers;
import exerelin.campaign.SectorManager;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import org.magiclib.util.MagicSettings;
import org.magiclib.util.MagicVariables;

import java.util.ArrayList;
import java.util.List;

public class DAModPlugin extends BaseModPlugin {
    public static final String SCATTER_MISSILE_ID = "diableavionics_micromissile";
    public static final String PD_MISSILE_ID = "diableavionics_magicmissile";
    public static final String THUNDERBOLT_ID = "diableavionics_thunderbolt";
    public static final String PLOVER_ID = "diableavionics_plover";
    public static final String BANISH_ID = "diableavionics_banishmirv";
    public static final String THRUSH_ID = "diableavionics_thrushmirv";
    public static final String SRAB_ID = "diableavionics_srab_shot";
    public static final String CICADA_ID = "diableavionics_cicada_shot";
    public static final String VIRTUOUS_GRENADE_ID = "diableavionics_virtuousGrenade_shot";
    public static final String VIRTUOUS_MISSILE_ID = "diableavionics_virtuousmissile";
    public static final String DEEPSTRIKE_ID = "diableavionics_deepStrikeM";

    public static final String MEMKEY_VERSION = "$diableavionics_version";
    public static final String MEMKEY_INTIALIZED = "$diableavionics_initialized";
    public static final String MEMKEY_SPECIAL_FLEETS_INITIALIZED = "$diableavionicsSpecial";

    public static List<String> DERECHO_RESIST = new ArrayList<>();
    public static List<String> DERECHO_IMMUNE = new ArrayList<>();
    public static List<String> WANZERS = new ArrayList<>();
    public static float GANTRY_TIME_MULT = 1, GANTRY_DEPLETION_PERCENT = 0, GANTRY_OP_MODIFIER = 0;
    public static final boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");


    @Override
    public void onApplicationLoad() throws ClassNotFoundException {
        ModSpecAPI ml = Global.getSettings().getModManager().getModSpec("MagicLib");
        int minor = Integer.parseInt(ml.getVersionInfo().getMinor());
        int major = Integer.parseInt(ml.getVersionInfo().getMajor());
        if (major < 1 || (major == 1 && minor < 4))
            throw new RuntimeException("Diable Avionics 2.8.0 requires MagicLib version 1.4.0 or newer.");

        try {
            Global.getSettings().getScriptClassLoader().loadClass("org.dark.shaders.util.ShaderLib");
            ShaderLib.init();
            LightData.readLightDataCSV("data/config/modFiles/diableavionics_lights.csv");
            TextureData.readTextureDataCSV("data/config/modFiles/diableavionics_maps.csv");
        } catch (ClassNotFoundException ex) {
        }

        //modSettings loading:
        DERECHO_RESIST = MagicSettings.getList("diableavionics", "missile_resist_derecho");
        DERECHO_IMMUNE = MagicSettings.getList("diableavionics", "missile_immune_derecho");
        WANZERS = MagicSettings.getList("diableavionics", "wanzers");
        GANTRY_TIME_MULT = MagicSettings.getFloat("diableavionics", "gantry_refitMult");
        GANTRY_DEPLETION_PERCENT = MagicSettings.getFloat("diableavionics", "gantry_depletionPercent");

    }

    @Override
    public void onNewGame() {
        if (!haveNexerelin || SectorManager.getManager().isCorvusMode()) {
            new DiableavionicsGen().generate(Global.getSector());
        }

        Global.getSector().getMemoryWithoutUpdate().set(MEMKEY_INTIALIZED, true);
    }


    @Override
    public void onNewGameAfterEconomyLoad() {
        //add the special fleets
        if (!MagicVariables.getIBB()) {
            //no IBB system? it would be a shame to miss on the Gulf
            DiableavionicsGen.spawnGulf();
        }
        DiableavionicsGen.spawnVirtuous();
        Global.getSector().getMemoryWithoutUpdate().set(MEMKEY_SPECIAL_FLEETS_INITIALIZED, true);
        Global.getSector().getMemoryWithoutUpdate().set(MEMKEY_VERSION, 2.84);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().registerPlugin(new DACampaignPlugin());

        if (!haveNexerelin || SectorManager.getManager().isCorvusMode()) {
            if (!Global.getSector().getMemoryWithoutUpdate().contains(MEMKEY_INTIALIZED)) {
                addToOngoingGame();
                Global.getSector().getMemoryWithoutUpdate().set(MEMKEY_INTIALIZED, true);
            }
        }

        if (!Global.getSector().getMemoryWithoutUpdate().contains(MEMKEY_SPECIAL_FLEETS_INITIALIZED)) {
            addFleetsToOngoingGame();
            Global.getSector().getMemoryWithoutUpdate().set(MEMKEY_SPECIAL_FLEETS_INITIALIZED, true);
        }

        if (!Global.getSector().getMemoryWithoutUpdate().contains(MEMKEY_VERSION)
                || ((Double) Global.getSector().getMemoryWithoutUpdate().get(MEMKEY_VERSION)) < 2.80) {

            for (StarSystemAPI system : Global.getSector().getStarSystems()) {
                for (CampaignFleetAPI fleet : system.getFleets()) {
                    for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
                        if (member.getVariant().hasHullMod("diableavionics_avionics")) {
                            member.getVariant().removeMod("diableavionics_avionics");
                        }
                    }
                }
            }

            Global.getSector().getMemoryWithoutUpdate().set(MEMKEY_VERSION, 2.80);
        }

        if (!Global.getSector().getMemoryWithoutUpdate().contains(MEMKEY_VERSION)
                || ((Double) Global.getSector().getMemoryWithoutUpdate().get(MEMKEY_VERSION)) < 2.82) {

            for (StarSystemAPI system : Global.getSector().getStarSystems()) {
                for (CampaignFleetAPI fleet : system.getFleets()) {
                    for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
                        if (member.getVariant().getSMods().contains("diableavionics_avionics")
                                || member.getVariant().getPermaMods().contains("diableavionics_avionics")) {
                            member.getVariant().removeMod("diableavionics_avionics");
                            member.getVariant().removePermaMod("diableavionics_avionics");
                        }
                    }
                }
            }

            Global.getSector().getMemoryWithoutUpdate().set(MEMKEY_VERSION, 2.82);
        }
    }

    protected void addFleetsToOngoingGame() {
        //add the special fleets
        if (!MagicVariables.getIBB()) {
            //no IBB system? it would be a shame to miss on the Gulf
            DiableavionicsGen.spawnGulf();
        }
        DiableavionicsGen.spawnVirtuous();
    }

    protected void addToOngoingGame() {
        if (!haveNexerelin || SectorManager.getManager().isCorvusMode()) {
            new DiableavionicsGen().generate(Global.getSector());

            MarketHelpers.generateMarketsFromEconJson("diableavionics_outerTerminus");
            MarketHelpers.generateMarketsFromEconJson("diableavionics_stagging");
            MarketHelpers.generateMarketsFromEconJson("diableavionics_fob");
        }
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case SCATTER_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new Diableavionics_ScatterMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case PD_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new Diableavionics_antiMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case THUNDERBOLT_ID:
                return new PluginPick<MissileAIPlugin>(new Diableavionics_ThunderboltMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case PLOVER_ID:
                return new PluginPick<MissileAIPlugin>(new Diableavionics_ploverMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case BANISH_ID:
                return new PluginPick<MissileAIPlugin>(new Diableavionics_banishAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case THRUSH_ID:
                return new PluginPick<MissileAIPlugin>(new Diableavionics_thrushAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case SRAB_ID:
                return new PluginPick<MissileAIPlugin>(new Diableavionics_SrabAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case CICADA_ID:
                return new PluginPick<MissileAIPlugin>(new Diableavionics_cicadaAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case VIRTUOUS_GRENADE_ID:
                return new PluginPick<MissileAIPlugin>(new Diableavionics_cicadaAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case VIRTUOUS_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new Diableavionics_itanoMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case DEEPSTRIKE_ID:
                return new PluginPick<MissileAIPlugin>(new Diableavionics_deepStrikeAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
        }
        return null;
    }
}