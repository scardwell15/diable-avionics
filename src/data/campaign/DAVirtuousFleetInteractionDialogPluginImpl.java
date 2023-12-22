package data.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CombatDamageData;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;

public class DAVirtuousFleetInteractionDialogPluginImpl extends FleetInteractionDialogPluginImpl {
    @Override
    public void backFromEngagement(EngagementResultAPI result) {
        if (DACampaignPlugin.hasMemoryInFleet(otherFleet, "$virtuous") && DACampaignPlugin.hasMemoryInFleet(otherFleet, "$simulationRunning")) {
            restoreOrigCaptains();
            if (origFlagship != null) {
                if (selectedFlagship != null) {
                    PersonAPI captain = origFlagship.getCaptain();
                    if (captain != null && !captain.isPlayer()) {
                        selectedFlagship.setCaptain(captain);
                    }
                }
                Global.getSector().getPlayerFleet().getFleetData().setFlagship(origFlagship);
            }

            otherFleet.getMemoryWithoutUpdate().set("$simulationSuccessful", result.didPlayerWin());
            otherFleet.getCommander().getMemoryWithoutUpdate().set("$simulationSuccessful", result.didPlayerWin());

            result.setLastCombatDamageData(new CombatDamageData());

            result.getWinnerResult().getReserves().clear();
            result.getWinnerResult().getDeployed().clear();
            result.getWinnerResult().getDisabled().clear();
            result.getWinnerResult().getRetreated().clear();
            result.getWinnerResult().getDestroyed().clear();

            result.getLoserResult().getReserves().clear();
            result.getLoserResult().getDeployed().clear();
            result.getLoserResult().getDisabled().clear();
            result.getLoserResult().getRetreated().clear();
            result.getLoserResult().getDestroyed().clear();

            context.getDataFor(playerFleet).getCrewLossesDuringLastEngagement().removeAllCrew();
            context.getDataFor(otherFleet).getCrewLossesDuringLastEngagement().removeAllCrew();

            context.getDataFor(playerFleet).getDestroyedInLastEngagement().clear();
            context.getDataFor(playerFleet).getDisabledInLastEngagement().clear();
            context.getDataFor(playerFleet).getRetreatedFromLastEngagement().clear();
            context.getDataFor(playerFleet).getMemberToDeployedMap().clear();

            context.getDataFor(otherFleet).getDestroyedInLastEngagement().clear();
            context.getDataFor(otherFleet).getDisabledInLastEngagement().clear();
            context.getDataFor(otherFleet).getRetreatedFromLastEngagement().clear();

            for (FleetMemberAPI member : playerFleet.getMembersWithFightersCopy()) {
                context.getDataFor(playerFleet).removeOwnCasualty(member);
                context.getDataFor(otherFleet).removeEnemyCasualty(member);
                context.getDataFor(playerFleet).changeOwn(member, FleetEncounterContextPlugin.Status.NORMAL);
                context.getDataFor(otherFleet).changeEnemy(member, FleetEncounterContextPlugin.Status.NORMAL);
                member.getStatus().setHullFraction(1);
                member.getStatus().repairFully();
            }

            for (FleetMemberAPI member : otherFleet.getMembersWithFightersCopy()) {
                context.getDataFor(otherFleet).removeOwnCasualty(member);
                context.getDataFor(playerFleet).removeEnemyCasualty(member);
                context.getDataFor(otherFleet).changeOwn(member, FleetEncounterContextPlugin.Status.NORMAL);
                context.getDataFor(playerFleet).changeEnemy(member, FleetEncounterContextPlugin.Status.NORMAL);
                member.getStatus().setHullFraction(1);
                member.getStatus().repairFully();
            }

            context.setEngagedInActualBattle(false);
            context.setEngagedInHostilities(false);

            showFleetInfo();
            optionSelected("", OptionId.OPEN_COMM);
            return;
        }
        super.backFromEngagement(result);
    }

    public void refreshFleetInfo() {
        showFleetInfo();
    }


    public void pullFleets() {
        pullInNearbyFleets();
    }
}
