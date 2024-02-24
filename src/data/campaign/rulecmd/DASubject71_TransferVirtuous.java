package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.DANexVirtuousFleetInteractionDialogPluginImpl;
import data.campaign.DAVirtuousFleetInteractionDialogPluginImpl;
import data.campaign.ids.Diableavionics_ids;
import data.scripts.DAModPlugin;

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
        virtuousMember.getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).unmodify(Diableavionics_ids.UNIQUE);
        targetFleet.getFleetData().removeFleetMember(virtuousMember);
        targetFleet.getMemoryWithoutUpdate().unset("$virtuous");
        targetFleet.getFleetData().ensureHasFlagship();

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        playerFleet.getFleetData().addFleetMember(virtuousMember);

        //reset battle so that the visual fleet also updates, showing no damage to the fleet.
        FleetEncounterContext context = (FleetEncounterContext) dialog.getPlugin().getContext();
        context.getBattle().leave(playerFleet, false);

        BattleAPI battle = Global.getFactory().createBattle(playerFleet, targetFleet);
        context.setBattle(battle);
        if (DAModPlugin.haveNexerelin) {
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
