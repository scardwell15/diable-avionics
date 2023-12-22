package data.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public class DACampaignPlugin extends BaseCampaignPlugin {
    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        if (interactionTarget.getMemoryWithoutUpdate().contains("$virtuous")) {
            if (Global.getSettings().getModManager().isModEnabled("nexerelin")) {
                return new PluginPick<InteractionDialogPlugin>(new DANexVirtuousFleetInteractionDialogPluginImpl(), PickPriority.MOD_SPECIFIC);
            } else {
                return new PluginPick<InteractionDialogPlugin>(new DAVirtuousFleetInteractionDialogPluginImpl(), PickPriority.MOD_SPECIFIC);

            }
        }
        return super.pickInteractionDialogPlugin(interactionTarget);
    }

    public static boolean hasMemoryInFleet(CampaignFleetAPI fleet, String key) {
        if (fleet.getActivePerson() != null) {
            if (fleet.getActivePerson().getMemoryWithoutUpdate().contains(key)) {
                return true;
            }
        }
        if (fleet.getCommander() != null) {
            if (fleet.getCommander().getMemoryWithoutUpdate().contains(key)) {
                return true;
            }
        }
        if (fleet.getFlagship() != null && fleet.getFlagship().getCaptain() != null) {
            if (fleet.getFlagship().getCaptain().getMemoryWithoutUpdate().contains(key)) {
                return true;
            }
        }
        if (fleet.getMemoryWithoutUpdate().contains(key)) {
            return true;
        }
        return false;
    }
}
