package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import data.scripts.world.systems.Diableavionics_stagging;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.special.Diableavionics_gulfLoot;
import data.campaign.special.Diableavionics_virtuousLoot;
import static data.scripts.util.Diableavionics_stringsManager.txt;
import data.scripts.util.MagicCampaign;
import data.scripts.world.systems.Diableavionics_fob;
import data.scripts.world.systems.Diableavionics_outerTerminus;

@SuppressWarnings("unchecked")
public class DiableavionicsGen implements SectorGeneratorPlugin {
    
    @Override
    public void generate(SectorAPI sector) {
	
        new Diableavionics_outerTerminus().generate(sector);
        new Diableavionics_stagging().generate(sector);
        new Diableavionics_fob().generate(sector);

        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("diableavionics");                      

        FactionAPI diableavionics = sector.getFaction("diableavionics");
        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
        FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
        FactionAPI pirates = sector.getFaction(Factions.PIRATES);
        FactionAPI independent = sector.getFaction(Factions.INDEPENDENT); 
        FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
        FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);   	
        FactionAPI diktat = sector.getFaction(Factions.DIKTAT); 
        FactionAPI kol = sector.getFaction(Factions.KOL);	
	FactionAPI persean = sector.getFaction(Factions.PERSEAN);
        FactionAPI guard = sector.getFaction(Factions.LIONS_GUARD);
        FactionAPI remnant = sector.getFaction(Factions.REMNANTS);
        FactionAPI derelict = sector.getFaction(Factions.DERELICT);
        
        //vanilla factions
        diableavionics.setRelationship(guard.getId(), RepLevel.FRIENDLY);    
        
        diableavionics.setRelationship(diktat.getId(), RepLevel.FAVORABLE); 
        
        diableavionics.setRelationship(player.getId(), RepLevel.SUSPICIOUS);
        
        diableavionics.setRelationship(independent.getId(), RepLevel.INHOSPITABLE);  
        diableavionics.setRelationship(tritachyon.getId(), RepLevel.INHOSPITABLE);      
        diableavionics.setRelationship(pirates.getId(), RepLevel.INHOSPITABLE);
        diableavionics.setRelationship(persean.getId(), RepLevel.INHOSPITABLE);	
        diableavionics.setRelationship(kol.getId(), RepLevel.INHOSPITABLE); 
        
        diableavionics.setRelationship(hegemony.getId(), RepLevel.HOSTILE);
        diableavionics.setRelationship(path.getId(), RepLevel.HOSTILE);   
        
        diableavionics.setRelationship(church.getId(), RepLevel.VENGEFUL);   
        
        //environment
        diableavionics.setRelationship(remnant.getId(), RepLevel.HOSTILE);	
        diableavionics.setRelationship(derelict.getId(), RepLevel.FRIENDLY);     
        
        //mods
        diableavionics.setRelationship("cabal", RepLevel.FRIENDLY);    
           
        diableavionics.setRelationship("sun_ici", RepLevel.FAVORABLE);       
        
        diableavionics.setRelationship("crystanite", RepLevel.NEUTRAL); 
        diableavionics.setRelationship("mayorate", RepLevel.NEUTRAL);	 
        diableavionics.setRelationship("pirateAnar", RepLevel.NEUTRAL);	 
        diableavionics.setRelationship("exipirated", RepLevel.NEUTRAL);	 
        
        diableavionics.setRelationship("exigency", RepLevel.SUSPICIOUS);
        diableavionics.setRelationship("syndicate_asp", RepLevel.SUSPICIOUS);	   
        diableavionics.setRelationship("tiandong", RepLevel.SUSPICIOUS);        
        diableavionics.setRelationship("SCY", RepLevel.SUSPICIOUS);     
        diableavionics.setRelationship("neutrinocorp", RepLevel.SUSPICIOUS);           
        
        diableavionics.setRelationship("6eme_bureau", RepLevel.INHOSPITABLE);	
        diableavionics.setRelationship("dassault_mikoyan", RepLevel.INHOSPITABLE);
        diableavionics.setRelationship("pack", RepLevel.INHOSPITABLE);     
        diableavionics.setRelationship("blackrock_driveyards", RepLevel.INHOSPITABLE);	   
        diableavionics.setRelationship("citadeldefenders", RepLevel.INHOSPITABLE);
        diableavionics.setRelationship("pn_colony", RepLevel.INHOSPITABLE);    
        diableavionics.setRelationship("junk_pirates", RepLevel.INHOSPITABLE);  
        diableavionics.setRelationship("sun_ice", RepLevel.INHOSPITABLE);          
            
        diableavionics.setRelationship("shadow_industry", RepLevel.HOSTILE);	
        diableavionics.setRelationship("ORA", RepLevel.HOSTILE);     
        diableavionics.setRelationship("interstellarimperium", RepLevel.HOSTILE);     
        diableavionics.setRelationship("blade_breakers", RepLevel.HOSTILE);   
        	
        diableavionics.setRelationship("new_galactic_order", RepLevel.VENGEFUL);	
        diableavionics.setRelationship("explorer_society", RepLevel.VENGEFUL);
        
        diableavionics.setRelationship("Coalition", -0.2f);      
        diableavionics.setRelationship("metelson", -0.2f);     
        diableavionics.setRelationship("the_deserter", 0.35f);    
        diableavionics.setRelationship("noir", 0.0f);     
        diableavionics.setRelationship("Lte", 0.0f);  
        diableavionics.setRelationship("GKSec", 0.1f); 
        diableavionics.setRelationship("gmda", -0.1f);   
        diableavionics.setRelationship("oculus", -0.25f);     
        diableavionics.setRelationship("nomads", -0.25f); 
        diableavionics.setRelationship("thulelegacy", -0.25f); 
        diableavionics.setRelationship("infected", -0.99f);     
        
    }
    
    private static final WeightedRandomPicker<String> VIRTUOUS = new WeightedRandomPicker<>();
    static{
        VIRTUOUS.add("diableavionics_virtuous_breacher");
        VIRTUOUS.add("diableavionics_virtuous_carbine");
        VIRTUOUS.add("diableavionics_virtuous_grenadier");
        VIRTUOUS.add("diableavionics_virtuous_sniper");
    }
    private static final String ID = "diableavionics_unique";
    public static String virtuousVariant="diableavionics_virtuous_breacher";
    
    public static void spawnVirtuous(){
        
        SectorEntityToken target=null;
        if(Global.getSector().getEntityById("diableavionics_prison")!=null && Global.getSector().getEntityById("diableavionics_prison").getFaction() == Global.getSector().getFaction("diableavionics")){
            target = Global.getSector().getEntityById("diableavionics_prison");
        } else {
            for(MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()){
                if(m.getFaction().getId().equals("diableavionics")){
                    if(
                            target==null 
                            || (
                                m.hasSubmarket(Submarkets.GENERIC_MILITARY) 
                                && (
                                    !target.getMarket().hasSubmarket(Submarkets.GENERIC_MILITARY)
                                    ||
                                    m.getSize()>target.getMarket().getSize()))){
                        target=m.getPrimaryEntity();
                    }
                }
            }
        }
        if(target!=null){

            PersonAPI virtuousCaptain = MagicCampaign.createCaptain(
                    false,
                    null,
                    txt("virtuousFN"),
                    txt("virtuousLN"),
                    "da_subject71",
                    FullName.Gender.ANY,
                    "diableavionics",
                    Ranks.SPECIAL_AGENT,
                    Ranks.POST_UNKNOWN,
                    Personalities.AGGRESSIVE,
                    10,
                    10,
                    OfficerManagerEvent.SkillPickPreference.GENERIC,
                    null
            );

            /**
            * Creates a fleet with a defined flagship and optional escort
            * 
            * @param fleetName
            * @param fleetFaction
            * @param fleetType
            * campaign.ids.FleetTypes, default to FleetTypes.PERSON_BOUNTY_FLEET
            * @param flagshipName
            * Optional flagship name
            * @param flagshipVariant
            * @param captain
            * PersonAPI, can be NULL for random captain, otherwise use createCaptain() 
            * @param supportFleet
            * Optional escort ship VARIANTS and their NUMBERS
            * @param minFP
            * Minimal fleet size, can be used to adjust to the player's power, set to 0 to ignore
            * @param reinforcementFaction
            * Reinforcement faction, if the fleet faction is a "neutral" faction without ships
            * @param qualityOverride
            * Optional ship quality override, default to 2 (no D-mods) if null or <0
            * @param spawnLocation
            * Where the fleet will spawn, default to assignmentTarget if NULL
            * @param assignment
            * campaign.FleetAssignment, default to orbit aggressive
            * @param assignementTarget
            * @param isImportant
            * @param transponderOn
            * @return 
            */
            String variant = VIRTUOUS.pick();
            virtuousVariant=variant;
            CampaignFleetAPI virtuous = (CampaignFleetAPI)MagicCampaign.createFleet(
                    txt("virtuousFleet"),
                    "diableavionics",
                    FleetTypes.TASK_FORCE,
                    txt("virtuousShip"),
                    variant,
                    virtuousCaptain, //officer
                    null, //escort fleet
                    300, //support fleet
                    "diableavionics", //support faction
                    2f, //quallity override
                    null,
                    FleetAssignment.PATROL_SYSTEM,
                    target,
                    false,
                    true
            );
            virtuous.setDiscoverable(false);
            virtuous.addTag(Tags.NEUTRINO);
            virtuous.getFlagship().getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(ID, -2000);
            virtuous.addEventListener(new Diableavionics_virtuousLoot());
            
            virtuous.getMemoryWithoutUpdate().set("$virtuous", true);
//            virtuous.getCustomEntitySpec().setIconColor(Color.WHITE);
        }
    }
    
    public static void spawnGulf(){
        
//        List<String> marketDemeter = new ArrayList<>();
//        marketDemeter.add("eochu_bres");
//                
//        List<String> factionDemeter = new ArrayList<>();
//        factionDemeter.add("tritachyon");
//        
//        SectorEntityToken targetDemeter = MagicCampaign.findSuitableTarget(
//                marketDemeter,
//                factionDemeter,
//                "CORE",
//                null,
//                null,
//                null,
//                false,
//                false,
//                false
//        );

        //settle for the largest military market
        SectorEntityToken target=null;
        
        for(MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()){
            if(m.getFaction().getId().equals(Factions.TRITACHYON)){
                if(
                        target==null 
                        || (
                            m.hasSubmarket(Submarkets.GENERIC_MILITARY) 
                            && (
                                !target.getMarket().hasSubmarket(Submarkets.GENERIC_MILITARY)
                                ||
                                m.getSize()>target.getMarket().getSize()))){
                    target=m.getPrimaryEntity();
                }
            }
        }
        
        if(target!=null){

            PersonAPI gulfCaptain = MagicCampaign.createCaptain(
                    false,
                    null,
                    txt("gulfFN"),
                    txt("gulfLN"),
                    "da_gulf",
                    FullName.Gender.FEMALE,
                    "tritachyon",
                    Ranks.SPECIAL_AGENT,
                    Ranks.POST_FLEET_COMMANDER,
                    Personalities.STEADY,
                    6,
                    0,
                    OfficerManagerEvent.SkillPickPreference.GENERIC,
                    null
            );

            /**
            * Creates a fleet with a defined flagship and optional escort
            * 
            * @param fleetName
            * @param fleetFaction
            * @param fleetType
            * campaign.ids.FleetTypes, default to FleetTypes.PERSON_BOUNTY_FLEET
            * @param flagshipName
            * Optional flagship name
            * @param flagshipVariant
            * @param captain
            * PersonAPI, can be NULL for random captain, otherwise use createCaptain() 
            * @param supportFleet
            * Optional escort ship VARIANTS and their NUMBERS
            * @param minFP
            * Minimal fleet size, can be used to adjust to the player's power, set to 0 to ignore
            * @param reinforcementFaction
            * Reinforcement faction, if the fleet faction is a "neutral" faction without ships
            * @param qualityOverride
            * Optional ship quality override, default to 2 (no D-mods) if null or <0
            * @param spawnLocation
            * Where the fleet will spawn, default to assignmentTarget if NULL
            * @param assignment
            * campaign.FleetAssignment, default to orbit aggressive
            * @param assignementTarget
            * @param isImportant
            * @param transponderOn
            * @return 
            */
            CampaignFleetAPI gulf = (CampaignFleetAPI)MagicCampaign.createFleet(
                    txt("gulfFleet"),
                    Factions.TRITACHYON,
                    FleetTypes.TASK_FORCE,
                    txt("gulfShip"),
                    "diableavionics_IBBgulf_boss",
                    gulfCaptain, //officer
                    null, //escort fleet
                    200, //support fleet
                    "tritachyon", //support faction
                    2f, //quallity override
                    null,
                    FleetAssignment.PATROL_SYSTEM,
                    target,
                    true,
                    false
            );
            gulf.setDiscoverable(false);
            gulf.addTag(Tags.NEUTRINO);
            gulf.getFlagship().getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(ID, -2000f);                
            gulf.addEventListener(new Diableavionics_gulfLoot());       
            
            gulf.getMemoryWithoutUpdate().set("$gulf", true);
//            gulf.getCustomEntitySpec().setIconColor(Color.WHITE);
        }
    }
}