package data.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static data.scripts.util.Diableavionics_stringsManager.txt;

public class WanzerGantry extends BaseHullMod {
    private static final String GANTRY_ID = "diableavionics_universaldecksExtra";
    private final Color HL = Global.getSettings().getColor("hColor");
    private final String ID = "wanzer_gantry", TAG = "wanzer";

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return txt("hm_gantry_101");
        }
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return Math.round(REPLACEMENT_RATE_THRESHOLD * 100f) + "%";
        } else if (index == 1) {
            return Math.round(REPLACEMENT_RATE_RESET * 100f) + "%";
        }
        return super.getSModDescriptionParam(index, hullSize);
    }


    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        //title
        tooltip.addSectionHeading(txt("hm_gantry_102"), Alignment.MID, 15);

        if (ship != null && ship.getVariant() != null) {
            if (ship.getVariant().getFittedWings().isEmpty()) {
                //no wing fitted
                tooltip.addPara(
                        txt("hm_gantry_103")
                        , 10
                        , HL
                );
            } else if (noWanzer(ship.getVariant())) {
                //no wanzer wings installed
                tooltip.addPara(
                        txt("hm_gantry_104")
                        , 10
                        , HL
                );
            } else {
                //effect applied
                List<String> wanzers = allWanzers(ship.getVariant());

                if (!wanzers.isEmpty()) {
                    tooltip.addPara(
                            txt("hm_gantry_105")
                            , 10
                            , HL
                            , txt("hm_gantry_105hl")
                    );

                    tooltip.setBulletedListMode("    - ");

                    for (String w : wanzers) {
                        tooltip.addPara(
                                w
                                , 3
                        );
                    }
                    tooltip.setBulletedListMode(null);
                }
            }
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        if (ship.getOriginalOwner() == -1) {
            return; //suppress in refit
        }

        boolean allDeployed = true, ranOnce = false;

        for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
            if (bay.getWing() != null) {
                ranOnce = true;
                if (bay.getWing().getSpec().hasTag(TAG)) {

                    FighterWingSpecAPI wingSpec = bay.getWing().getSpec();
                    int deployed = bay.getWing().getWingMembers().size();
                    int maxTotal = wingSpec.getNumFighters() + 1;
                    int actualAdd = maxTotal - deployed;

                    if (actualAdd > 0) {
                        bay.setExtraDeployments(actualAdd);
                        bay.setExtraDeploymentLimit(maxTotal);
                        bay.setExtraDuration(9999999);
                        allDeployed = false;
                    } else {
                        bay.setExtraDeployments(0);
                        bay.setExtraDeploymentLimit(0);
                        bay.setFastReplacements(0);
                    }

                    if (ship.getMutableStats().getFighterRefitTimeMult().getPercentStatMod(ID) == null && actualAdd != 0) {
                        //instantly add all the required fighters upon deployment
                        bay.setFastReplacements(actualAdd);
                    }

                    //debug
//                    Global.getCombatEngine().addFloatingText(
//                            bay.getWeaponSlot().computePosition(ship),
//                            "add= "+bay.getExtraDeployments()+" max= "+bay.getExtraDeploymentLimit()+" fast= "+bay.getFastReplacements(),
//                            10, Color.ORANGE, ship, 1, 1);
                }
            }
        }

        if (ship.getMutableStats().getFighterRefitTimeMult().getPercentStatMod(ID) == null && allDeployed && ranOnce) {
            //used as a check to add all the extra fighters upon deployment
            ship.getMutableStats().getFighterRefitTimeMult().modifyPercent(ID, 1);
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //reset the "check" mutable stat so that it is applied next deployment
        stats.getFighterRefitTimeMult().unmodify(ID);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        //the extra crafts do not deplete the fighter replacement gauge when destroyed, this makes the rate deplete faster from those that do to compensate.
        Integer crafts = 0, extraCrafts = 0;
        for (String w : ship.getVariant().getFittedWings()) {
            if (Global.getSettings().getFighterWingSpec(w).hasTag(TAG)) {
                crafts += Global.getSettings().getFighterWingSpec(w).getNumFighters();
                extraCrafts++;
            }
        }
        if (extraCrafts > 0) {
            ship.getMutableStats().getDynamic().getMod(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, (crafts + extraCrafts) / crafts);
        }

        boolean sMod = isSMod(ship);
        if (sMod) {
            ship.addListener(new BDeckListener(ship));
        }
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return txt("hm_noBays");
    }

    private boolean noWanzer(ShipVariantAPI variant) {
        boolean noWanzer = true;
        for (String w : variant.getFittedWings()) {
            if (Global.getSettings().getFighterWingSpec(w).hasTag(TAG)) {
                noWanzer = false;
                break;
            }
        }
        return noWanzer;
    }

    private List<String> allWanzers(ShipVariantAPI variant) {
        List<String> allWanzers = new ArrayList<>();
        for (String w : variant.getFittedWings()) {
            FighterWingSpecAPI f = Global.getSettings().getFighterWingSpec(w);
            if (f.hasTag(TAG)) {
                allWanzers.add(f.getWingName() + " " + f.getRoleDesc());
            }
        }
        return allWanzers;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null) return false;
        return ship.getVariant().hasHullMod(GANTRY_ID);
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return ship.getVariant().hasHullMod(GANTRY_ID);
    }

    public static float REPLACEMENT_RATE_THRESHOLD = 0.4f;
    public static float REPLACEMENT_RATE_RESET = 0.75f;

    public static class BDeckListener implements AdvanceableListener {
        protected ShipAPI ship;
        protected boolean fired = false;

        public BDeckListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            float cr = ship.getCurrentCR();

            if (!fired && cr >= 0) {
                if (ship.getSharedFighterReplacementRate() <= REPLACEMENT_RATE_THRESHOLD) {
                    fired = true;

                    for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
                        if (bay.getWing() == null) continue;

                        float rate = REPLACEMENT_RATE_RESET;
                        bay.setCurrRate(rate);

                        bay.makeCurrentIntervalFast();
                        FighterWingSpecAPI spec = bay.getWing().getSpec();

                        int maxTotal = spec.getNumFighters();
                        int actualAdd = maxTotal - bay.getWing().getWingMembers().size();
                        if (actualAdd > 0) {
                            bay.setFastReplacements(bay.getFastReplacements() + actualAdd);
                        }
                    }
                }
            }

            if (Global.getCurrentState() == GameState.COMBAT &&
                    Global.getCombatEngine() != null && Global.getCombatEngine().getPlayerShip() == ship) {

                String status = txt("hm_gantry_s02_standby");
                boolean penalty = false;
                if (fired) status = txt("hm_gantry_s02_active");
                Global.getCombatEngine().maintainStatusForPlayerShip("da_bdeck",
                        Global.getSettings().getSpriteName("ui", "icon_tactical_bdeck"),
                        txt("hm_gantry_s02"), status, penalty);
            }
        }
    }
}
