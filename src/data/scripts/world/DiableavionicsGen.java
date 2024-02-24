package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.ids.Diableavionics_ids;
import data.campaign.special.Diableavionics_gulfLoot;
import data.campaign.special.Diableavionics_virtuousLoot;
import data.scripts.world.systems.Diableavionics_fob;
import data.scripts.world.systems.Diableavionics_outerTerminus;
import data.scripts.world.systems.Diableavionics_stagging;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.magiclib.util.MagicCampaign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static data.scripts.util.Diableavionics_stringsManager.txt;

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

    static {
        VIRTUOUS.add("diableavionics_virtuous_breacher");
        VIRTUOUS.add("diableavionics_virtuous_carbine");
        VIRTUOUS.add("diableavionics_virtuous_grenadier");
        VIRTUOUS.add("diableavionics_virtuous_sniper");
    }

    public static String virtuousVariant = "diableavionics_virtuous_breacher";

    public static void spawnVirtuous() {
        SectorEntityToken target = null;
        if (Global.getSector().getEntityById("diableavionics_prison") != null && Global.getSector().getEntityById("diableavionics_prison").getFaction() == Global.getSector().getFaction("diableavionics")) {
            target = Global.getSector().getEntityById("diableavionics_prison");
        } else {
            for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
                if (m.getFaction().getId().equals("diableavionics")) {
                    if (target == null || (m.hasSubmarket(Submarkets.GENERIC_MILITARY)
                            && (!target.getMarket().hasSubmarket(Submarkets.GENERIC_MILITARY) || m.getSize() > target.getMarket().getSize()))) {
                        target = m.getPrimaryEntity();
                    }
                }
            }
        }

        if (target != null) {
            PersonAPI virtuousCaptain = MagicCampaign.createCaptainBuilder("diableavionics")
                    .setFirstName(txt("virtuousFN"))
                    .setLastName(txt("virtuousLN"))
                    .setPortraitId("da_subject71")
                    .setGender(FullName.Gender.ANY)
                    .setRankId(Ranks.SPECIAL_AGENT)
                    .setPostId(Ranks.POST_UNKNOWN)
                    .setPersonality(Personalities.AGGRESSIVE)
                    .setLevel(10)
                    .setEliteSkillsOverride(10)
                    .setSkillPreference(OfficerManagerEvent.SkillPickPreference.YES_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE)
                    .create();

            String variant = VIRTUOUS.pick();
            virtuousVariant = variant;
            CampaignFleetAPI virtuous = MagicCampaign.createFleetBuilder()
                    .setFleetFaction("diableavionics")
                    .setFleetName(txt("virtuousFleet"))
                    .setFleetType(FleetTypes.TASK_FORCE)
                    .setFlagshipName(txt("virtuousShip"))
                    .setFlagshipAlwaysRecoverable(true)
                    .setFlagshipVariant(variant)
                    .setFlagshipAutofit(false)
                    .setCaptain(virtuousCaptain)
                    .setMinFP(300)
                    .setReinforcementFaction("diableavionics")
                    .setQualityOverride(2f)
                    .setAssignment(FleetAssignment.PATROL_SYSTEM)
                    .setAssignmentTarget(target)
                    .setSpawnLocation(target)
                    .setIsImportant(false)
                    .setTransponderOn(true)
                    .create();

            virtuous.setDiscoverable(false);
            virtuous.addTag(Tags.NEUTRINO);
            virtuous.getFlagship().getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(Diableavionics_ids.UNIQUE, -2000);
            virtuous.addEventListener(new Diableavionics_virtuousLoot());

            virtuousCaptain.getMemoryWithoutUpdate().set("$virtuous", true);
            virtuous.getMemoryWithoutUpdate().set("$virtuous", true);
        }
    }

    public static void spawnGulf() {
        //settle for the largest military market
        SectorEntityToken target = null;

        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (m.getFaction().getId().equals(Factions.TRITACHYON)) {
                if (target == null
                        || (m.hasSubmarket(Submarkets.GENERIC_MILITARY) && (!target.getMarket().hasSubmarket(Submarkets.GENERIC_MILITARY)
                        || m.getSize() > target.getMarket().getSize()))) {
                    target = m.getPrimaryEntity();
                }
            }
        }

        if (target != null) {
            PersonAPI gulfCaptain = MagicCampaign.createCaptainBuilder(Factions.TRITACHYON)
                    .setFirstName(txt("gulfFN"))
                    .setLastName(txt("gulfLN"))
                    .setPortraitId("da_gulf")
                    .setGender(FullName.Gender.FEMALE)
                    .setRankId(Ranks.SPECIAL_AGENT)
                    .setPostId(Ranks.POST_FLEET_COMMANDER)
                    .setPersonality(Personalities.STEADY)
                    .setLevel(6)
                    .setEliteSkillsOverride(0)
                    .setSkillPreference(OfficerManagerEvent.SkillPickPreference.YES_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE)
                    .create();

            CampaignFleetAPI gulf = MagicCampaign.createFleetBuilder()
                    .setFleetFaction(Factions.TRITACHYON)
                    .setFleetName(txt("gulfFleet"))
                    .setFleetType(FleetTypes.TASK_FORCE)
                    .setFlagshipName(txt("gulfShip"))
                    .setFlagshipAlwaysRecoverable(true)
                    .setFlagshipVariant("diableavionics_IBBgulf_boss")
                    .setFlagshipAutofit(false)
                    .setCaptain(gulfCaptain)
                    .setMinFP(200)
                    .setReinforcementFaction(Factions.TRITACHYON)
                    .setQualityOverride(2f)
                    .setAssignment(FleetAssignment.PATROL_SYSTEM)
                    .setAssignmentTarget(target)
                    .setSpawnLocation(target)
                    .setIsImportant(true)
                    .setTransponderOn(false)
                    .create();

            gulf.setDiscoverable(false);
            gulf.addTag(Tags.NEUTRINO);
            gulf.getFlagship().getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(Diableavionics_ids.UNIQUE, -2000f);
            gulf.addEventListener(new Diableavionics_gulfLoot());

            gulf.getMemoryWithoutUpdate().set("$gulf", true);
        }
    }
}