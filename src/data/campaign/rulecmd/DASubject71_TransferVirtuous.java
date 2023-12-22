package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.DANexVirtuousFleetInteractionDialogPluginImpl;
import data.campaign.DAVirtuousFleetInteractionDialogPluginImpl;

import java.util.List;
import java.util.Map;

public class DASubject71_TransferVirtuous extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        CampaignFleetAPI targetFleet = (CampaignFleetAPI) dialog.getInteractionTarget();
        FleetMemberAPI virtuousMember = targetFleet.getFleetData().getMemberWithCaptain(targetFleet.getCommander());
        targetFleet.getFleetData().removeOfficer(targetFleet.getCommander());
        virtuousMember.setCaptain(null);
        virtuousMember.setFlagship(false);
        targetFleet.getFleetData().removeFleetMember(virtuousMember);
        targetFleet.getMemoryWithoutUpdate().unset("$virtuous");
        targetFleet.getFleetData().ensureHasFlagship();

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        playerFleet.getFleetData().addFleetMember(virtuousMember);

        //reset battle so that the visual fleet also updates, showing no damage to the fleet.
        FleetEncounterContext context = (FleetEncounterContext) dialog.getPlugin().getContext();
        BattleAPI battle = Global.getFactory().createBattle(playerFleet, targetFleet);
        context.setBattle(battle);
        if (dialog.getPlugin() instanceof DANexVirtuousFleetInteractionDialogPluginImpl) {
            DANexVirtuousFleetInteractionDialogPluginImpl plugin = (DANexVirtuousFleetInteractionDialogPluginImpl) dialog.getPlugin();
            plugin.pullFleets();
            plugin.refreshFleetInfo();
        } else if (dialog.getPlugin() instanceof DAVirtuousFleetInteractionDialogPluginImpl) {
            DAVirtuousFleetInteractionDialogPluginImpl plugin = (DAVirtuousFleetInteractionDialogPluginImpl) dialog.getPlugin();
            plugin.pullFleets();
            plugin.refreshFleetInfo();
        }
        return true;
    }
}
