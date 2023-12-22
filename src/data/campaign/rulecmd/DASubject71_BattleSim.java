package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class DASubject71_BattleSim extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        CampaignFleetAPI targetFleet = (CampaignFleetAPI) dialog.getInteractionTarget();


        BattleCreationContext bcc = new BattleCreationContext(Global.getSector().getPlayerFleet(), FleetGoal.ATTACK, targetFleet, FleetGoal.ATTACK);
        bcc.setPlayerCommandPoints((int) Global.getSector().getPlayerFleet().getCommanderStats().getCommandPoints().getModifiedValue());

        dialog.getVisualPanel().fadeVisualOut();
        dialog.startBattle(bcc);
        return true;
    }
}
