package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.ai.Diableavionics_antiMissileAI;
import data.scripts.ai.Diableavionics_banishAI;
import data.scripts.ai.Diableavionics_thrushAI;
import data.scripts.ai.Diableavionics_ScatterMissileAI;
import data.scripts.ai.Diableavionics_SrabAI;
import data.scripts.ai.Diableavionics_ThunderboltMissileAI;
import data.scripts.ai.Diableavionics_cicadaAI;
import data.scripts.ai.Diableavionics_deepStrikeAI;
import data.scripts.ai.Diableavionics_itanoMissileAI;
import data.scripts.ai.Diableavionics_ploverMissileAI;
import data.scripts.util.MagicSettings;
import data.scripts.util.MagicVariables;
import data.scripts.world.DiableavionicsGen;
import exerelin.campaign.SectorManager;
import java.util.ArrayList;
import java.util.List;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

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
    
    public static List<String> DERECHO_RESIST = new ArrayList<>();
    public static List<String> DERECHO_IMMUNE = new ArrayList<>();
    public static List<String> WANZERS = new ArrayList<>();
    public static float GANTRY_TIME_MULT = 1, GANTRY_DEPLETION_PERCENT = 0, GANTRY_OP_MODIFIER=0;
    
    @Override
    public void onApplicationLoad() throws ClassNotFoundException {  
            
        
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
	boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
	if (!haveNexerelin || SectorManager.getManager().getCorvusMode()){
            new DiableavionicsGen().generate(Global.getSector());
        }
    }
    
    private final String SPECIAL_FLEETS="$diableavionicsSpecial";
        
    @Override
    public void onNewGameAfterEconomyLoad() {
        //add the special fleets
        if(!MagicVariables.getIBB()){
            //no IBB system? it would be a shame to miss on the Gulf
            DiableavionicsGen.spawnGulf();
        }
        DiableavionicsGen.spawnVirtuous();
        Global.getSector().getMemoryWithoutUpdate().set(SPECIAL_FLEETS, true);
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