package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.DAModPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarketHelpers {
    // Copied from Nexerelin / Histidine
    // Stolen from Dal

    public static PersonAPI getPerson(MarketAPI market, String postId) {

        for (CommDirectoryEntryAPI dir : market.getCommDirectory().getEntriesCopy()) {
            if (dir.getType() == CommDirectoryEntryAPI.EntryType.PERSON) {
                PersonAPI person = (PersonAPI) dir.getEntryData();
                if (person.getPostId().equals(postId)) {
                    return person;
                }
            }
        }

        return null;
    }

    public static boolean hasPersonSpecific(MarketAPI market, PersonAPI person) {

        for (CommDirectoryEntryAPI dir : market.getCommDirectory().getEntriesCopy()) {
            if (dir.getType() == CommDirectoryEntryAPI.EntryType.PERSON) {
                PersonAPI dirP = (PersonAPI) dir.getEntryData();
                if (dirP.equals(person)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean hasPerson(MarketAPI market, String postId)
    {
        return getPerson(market, postId) != null;
    }

    public static boolean removePerson(MarketAPI market, String postId)
    {
        PersonAPI person = getPerson(market, postId);
        if (person == null) return false;

        market.getCommDirectory().removePerson(person);
        market.removePerson(person);
        Global.getSector().getImportantPeople().removePerson(person);
        return true;
    }

    public static PersonAPI addPerson(ImportantPeopleAPI ip, MarketAPI market,
                                      String rankId, String postId, boolean noDuplicate)
    {
        if (noDuplicate && hasPerson(market, postId))
            return null;

        PersonAPI person = market.getFaction().createRandomPerson();
        if (rankId != null) person.setRankId(rankId);
        person.setPostId(postId);

        market.getCommDirectory().addPerson(person);
        market.addPerson(person);
        ip.addPerson(person);
        ip.getData(person).getLocation().setMarket(market);
        ip.checkOutPerson(person, "permanent_staff");

        if (postId.equals(Ranks.POST_BASE_COMMANDER) || postId.equals(Ranks.POST_STATION_COMMANDER)
                || postId.equals(Ranks.POST_ADMINISTRATOR))
        {
            if (market.getSize() >= 8) {
                person.setImportanceAndVoice(PersonImportance.VERY_HIGH, StarSystemGenerator.random);
            } else if (market.getSize() >= 6) {
                person.setImportanceAndVoice(PersonImportance.HIGH, StarSystemGenerator.random);
            } else {
                person.setImportanceAndVoice(PersonImportance.MEDIUM, StarSystemGenerator.random);
            }
        } else if (postId.equals(Ranks.POST_PORTMASTER)) {
            if (market.getSize() >= 8) {
                person.setImportanceAndVoice(PersonImportance.HIGH, StarSystemGenerator.random);
            } else if (market.getSize() >= 6) {
                person.setImportanceAndVoice(PersonImportance.MEDIUM, StarSystemGenerator.random);
            } else if (market.getSize() >= 4) {
                person.setImportanceAndVoice(PersonImportance.LOW, StarSystemGenerator.random);
            } else {
                person.setImportanceAndVoice(PersonImportance.VERY_LOW, StarSystemGenerator.random);
            }
        } else if (postId.equals(Ranks.POST_SUPPLY_OFFICER)) {
            if (market.getSize() >= 6) {
                person.setImportanceAndVoice(PersonImportance.MEDIUM, StarSystemGenerator.random);
            } else if (market.getSize() >= 4) {
                person.setImportanceAndVoice(PersonImportance.LOW, StarSystemGenerator.random);
            } else {
                person.setImportanceAndVoice(PersonImportance.VERY_LOW, StarSystemGenerator.random);
            }
        }

        return person;
    }

    public static boolean addPersonSpecific(ImportantPeopleAPI ip, PersonAPI person, MarketAPI market,
                                      String rankId, String postId, boolean noDuplicate)
    {
        if (noDuplicate && hasPersonSpecific(market, person))
            return false;

        if (rankId != null) person.setRankId(rankId);
        person.setPostId(postId);

        market.getCommDirectory().addPerson(person);
        market.addPerson(person);
        ip.addPerson(person);
        ip.getData(person).getLocation().setMarket(market);
        ip.checkOutPerson(person, "permanent_staff");

        if (postId.equals(Ranks.POST_BASE_COMMANDER) || postId.equals(Ranks.POST_STATION_COMMANDER)
                || postId.equals(Ranks.POST_ADMINISTRATOR))
        {
            if (market.getSize() >= 8) {
                person.setImportanceAndVoice(PersonImportance.VERY_HIGH, StarSystemGenerator.random);
            } else if (market.getSize() >= 6) {
                person.setImportanceAndVoice(PersonImportance.HIGH, StarSystemGenerator.random);
            } else {
                person.setImportanceAndVoice(PersonImportance.MEDIUM, StarSystemGenerator.random);
            }
        } else if (postId.equals(Ranks.POST_PORTMASTER)) {
            if (market.getSize() >= 8) {
                person.setImportanceAndVoice(PersonImportance.HIGH, StarSystemGenerator.random);
            } else if (market.getSize() >= 6) {
                person.setImportanceAndVoice(PersonImportance.MEDIUM, StarSystemGenerator.random);
            } else if (market.getSize() >= 4) {
                person.setImportanceAndVoice(PersonImportance.LOW, StarSystemGenerator.random);
            } else {
                person.setImportanceAndVoice(PersonImportance.VERY_LOW, StarSystemGenerator.random);
            }
        } else if (postId.equals(Ranks.POST_SUPPLY_OFFICER)) {
            if (market.getSize() >= 6) {
                person.setImportanceAndVoice(PersonImportance.MEDIUM, StarSystemGenerator.random);
            } else if (market.getSize() >= 4) {
                person.setImportanceAndVoice(PersonImportance.LOW, StarSystemGenerator.random);
            } else {
                person.setImportanceAndVoice(PersonImportance.VERY_LOW, StarSystemGenerator.random);
            }
        }

        return true;
    }


    public static void addMarketPeople(MarketAPI market)
    {
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();

        if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_DO_NOT_INIT_COMM_LISTINGS)) return;
        if (DAModPlugin.haveNexerelin) return;

        boolean addedPerson = false;
        if (market.hasIndustry(Industries.MILITARYBASE) || market.hasIndustry(Industries.HIGHCOMMAND)) {
            String rankId = Ranks.GROUND_MAJOR;
            if (market.getSize() >= 6) {
                rankId = Ranks.GROUND_GENERAL;
            } else if (market.getSize() >= 4) {
                rankId = Ranks.GROUND_COLONEL;
            }

            addPerson(ip, market, rankId, Ranks.POST_BASE_COMMANDER, true);
            addedPerson = true;
        }

        boolean hasStation = false;
        for (Industry curr : market.getIndustries()) {
            if (curr.getSpec().hasTag(Industries.TAG_STATION)) {
                hasStation = true;
                continue;
            }
        }
        if (hasStation) {
            String rankId = Ranks.SPACE_COMMANDER;
            if (market.getSize() >= 6) {
                rankId = Ranks.SPACE_ADMIRAL;
            } else if (market.getSize() >= 4) {
                rankId = Ranks.SPACE_CAPTAIN;
            }

            addPerson(ip, market, rankId, Ranks.POST_STATION_COMMANDER, true);
            addedPerson = true;
        }

//			if (market.hasIndustry(Industries.WAYSTATION)) {
//				// kept here as a reminder to check core plugin again when needed
//			}

        if (market.hasSpaceport()) {
            //person.setRankId(Ranks.SPACE_CAPTAIN);

            addPerson(ip, market, null, Ranks.POST_PORTMASTER, true);
            addedPerson = true;
        }

        if (addedPerson) {
            addPerson(ip, market, Ranks.SPACE_COMMANDER, Ranks.POST_SUPPLY_OFFICER, true);
            addedPerson = true;
        }

        if (!addedPerson) {
            addPerson(ip, market, Ranks.CITIZEN, Ranks.POST_ADMINISTRATOR, true);
        }
    }

    public static Industry getStationIndustry(MarketAPI market) {
        for (Industry curr : market.getIndustries()) {
            if (curr.getSpec().hasTag(Industries.TAG_STATION)) {
                return curr;
            }
        }
        return null;
    }

    public static void updateStationIfNeeded(MarketAPI market, Industry curr, String goalIndID) {
        if (curr == null) return;

        String currIndId = (getStationIndustry(market).getId());

        if (currIndId.equals(goalIndID)) return;

        market.removeIndustry(curr.getId(), null, false);

        market.addIndustry(goalIndID);
        curr = getStationIndustry(market);
        if (curr == null) return;

        curr.finishBuildingOrUpgrading();


        CampaignFleetAPI fleet = Misc.getStationFleet(market.getPrimaryEntity());
        if (fleet == null) return;

        List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        if (members.size() < 1) return;

        fleet.inflateIfNeeded();

        FleetMemberAPI station = members.get(0);

        WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<Integer>();
        int index = 1; // index 0 is station body
        for (String slotId : station.getVariant().getModuleSlots()) {
            ShipVariantAPI mv = station.getVariant().getModuleVariant(slotId);
            if (Misc.isActiveModule(mv)) {
                picker.add(index, 1f);
            }
            index++;
        }
    }


    public static void generateMarketsFromEconJson(String fileName) {
        generateMarketsFromEconJson(Global.getSector().getStarSystem(fileName), fileName);
    }

    /**
     * Generates markets using the econ json in data/campaign/econ.
     *
     * @param system star system to generate in
     * @param fileName file name without the .json.
     */
    public static void generateMarketsFromEconJson(StarSystemAPI system, String fileName) {
        try {
            JSONObject economyJson = Global.getSettings().loadJSON("data/campaign/econ/" + fileName + ".json");
            JSONArray marketsJson = economyJson.optJSONArray("markets");
            if (marketsJson != null) {
                for (int i = 0; i < marketsJson.length(); i++) {
                    JSONObject marketJson = marketsJson.getJSONObject(i);


                    JSONArray entities = marketJson.getJSONArray("entities");
                    SectorEntityToken primaryEntity = system.getEntityById(entities.getString(0));
                    List<SectorEntityToken> secondaryEntities = new ArrayList<>();
                    for (int j = 1; j < entities.length(); j++) {
                        secondaryEntities.add(system.getEntityById(entities.getString(j)));
                    }

                    String name = primaryEntity.getName();
                    String factionId = marketJson.getString("faction");

                    int size = marketJson.optInt("size", 0);
                    JSONArray conditionsJson = marketJson.getJSONArray("startingConditions");
                    List<String> conditions = new ArrayList<>();
                    for (int j = 0; j < conditionsJson.length(); j++) {
                        String condition = conditionsJson.getString(j);
                        if (condition.matches("population_\\d+")) {
                            size = Integer.parseInt(condition.substring("population_".length()));
                        } else {
                            conditions.add(condition);
                        }
                    }

                    boolean freePort = marketJson.optBoolean("freePort");
                    List<IndustryData> industries = new ArrayList<>();
                    JSONArray industriesJson = marketJson.getJSONArray("industries");
                    for (int j = 0; j < industriesJson.length(); j++) {
                        JSONArray industryJson = industriesJson.getJSONArray(j);
                        industries.add(new IndustryData(industryJson));
                    }

                    MarketAPI market = addMarketplace(factionId, primaryEntity, secondaryEntities, name, size, conditions, industries, null, 0.3f, false);
                    market.setFreePort(freePort);
                    addMarketPeople(market);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity, List<SectorEntityToken> connectedEntities, String name,
                                           int size, List<String> marketConditions, List<IndustryData> industries, List<String> submarkets, float tariff, boolean hidden) {
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String marketID = primaryEntity.getId();

        MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);
        newMarket.setFactionId(factionID);
        newMarket.setPrimaryEntity(primaryEntity);
        newMarket.getTariff().modifyFlat("generator", tariff);

        submarkets = submarkets != null ? submarkets : new ArrayList<>(Arrays.asList(
                Submarkets.SUBMARKET_STORAGE,
                Submarkets.GENERIC_MILITARY,
                Submarkets.SUBMARKET_BLACK,
                Submarkets.SUBMARKET_OPEN));

        for (String market : submarkets) {
            newMarket.addSubmarket(market);
        }

        for (String condition : marketConditions) {
            newMarket.addCondition(condition);
        }

        for (IndustryData industryData : industries) {
            String industry = industryData.industryId;
            String specialItemId = industryData.specialItemId;
            String aiCoreId = industryData.aiCoreId;
            newMarket.addIndustry(industry);

            if (specialItemId != null) {
                newMarket.getIndustry(industry).setSpecialItem(new SpecialItemData(specialItemId, null));
            }
            if (aiCoreId != null) {
                newMarket.getIndustry(industry).setAICoreId(aiCoreId);
            }
        }

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        globalEconomy.addMarket(newMarket, true);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }

        if (!hidden) newMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

        return newMarket;
    }

    public static class IndustryData {
        private final String industryId;
        private String specialItemId = null;
        private String aiCoreId = null;

        public IndustryData(JSONArray industryJson) throws JSONException {
            this.industryId = industryJson.getString(0);
            String secondId = industryJson.optString(1, null);
            String thirdId = industryJson.optString(2, null);
            if (secondId != null && thirdId == null) {
                if (Global.getSettings().getSpecialItemSpec(secondId) != null) {
                    this.specialItemId = secondId;
                } else {
                    this.aiCoreId = secondId;
                }
            } else {
                if (Global.getSettings().getSpecialItemSpec(secondId) != null) {
                    this.specialItemId = secondId;
                    this.aiCoreId = thirdId;
                } else {
                    this.aiCoreId = secondId;
                    this.specialItemId = thirdId;
                }
            }
        }

        public IndustryData(String industryId, String specialItemId, String aiCoreId) {
            this.industryId = industryId;
            this.specialItemId = specialItemId;
            this.aiCoreId = aiCoreId;
        }
    }
}
