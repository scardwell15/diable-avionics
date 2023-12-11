/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DroneLauncherShipSystemAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import static data.scripts.util.Diableavionics_stringsManager.txt;

/**
 *
 * @author Tartiflette
 */
public class Diableavionics_calmEffect implements EveryFrameWeaponEffectPlugin{    
    
    private boolean runOnce=false, refit=false;
    private DroneLauncherShipSystemAPI system;
    private ShipAPI ship;
    private final String ID="diableavionics_calmEffect";
    private int mode=0;
    private final float SPEED=20,SHIELD=-0.2f,WEAPONS=-0.2f;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system = (DroneLauncherShipSystemAPI)ship.getSystem();
            if(ship.getOriginalOwner()==-1){
                refit=true;
            }
        }
        
        if (engine.isPaused() || refit) {
            return;
        } 
        
        
        switch (system.getDroneOrders()){
            case RECALL:
            {
                //drones recalled, speed boost
                if(mode!=1){
                    mode=1;                
                    ship.getMutableStats().getShieldAbsorptionMult().unmodify(ID);
                    ship.getMutableStats().getBeamWeaponFluxCostMult().unmodify(ID);
                    ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(ID);
                    ship.getMutableStats().getBallisticWeaponFluxCostMod().unmodify(ID);
                    ship.getMutableStats().getMissileWeaponFluxCostMod().unmodify(ID);
                    
                    ship.getMutableStats().getMaxSpeed().modifyFlat(ID, SPEED);
                }
                
                if(ship==engine.getPlayerShip()){
                    engine.maintainStatusForPlayerShip(ID+"engines",
                        "graphics/icons/hullsys/burn_drive.png",
                        txt("stm_calm_0"),                       
                        "+"+SPEED+txt("stm_calm_1"),
                        false
                    );
                }
                break;
            }
            case DEPLOY:
            {
                //drones holding position, shield boost
                if(mode!=2){
                    mode=2;
                    ship.getMutableStats().getMaxSpeed().unmodify(ID);
                    ship.getMutableStats().getBeamWeaponFluxCostMult().unmodify(ID);
                    ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(ID);
                    ship.getMutableStats().getBallisticWeaponFluxCostMod().unmodify(ID);
                    ship.getMutableStats().getMissileWeaponFluxCostMod().unmodify(ID);

                    ship.getMutableStats().getShieldAbsorptionMult().modifyMult(ID, 1+SHIELD);
                }
                
                if(ship==engine.getPlayerShip()){
                    engine.maintainStatusForPlayerShip(ID+"shield",
                        "graphics/icons/hullsys/damper_field.png",
                        txt("stm_calm_0"),  
                        (int)(SHIELD*100)+txt("stm_calm_2"),
                        false
                    );
                }
                break;
            }
            case ATTACK:
            {
                //drones attacking, weapon boost
                if(mode!=3){
                    mode=3;
                    ship.getMutableStats().getMaxSpeed().unmodify(ID);
                    ship.getMutableStats().getShieldAbsorptionMult().unmodify(ID);
                
                    ship.getMutableStats().getBeamWeaponFluxCostMult().modifyMult(ID, 1+WEAPONS);
                    ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyMult(ID, 1+WEAPONS);
                    ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyMult(ID, 1+WEAPONS);
                    ship.getMutableStats().getMissileWeaponFluxCostMod().modifyMult(ID, 1+WEAPONS);
                }
                
                if(ship==engine.getPlayerShip()){
                    engine.maintainStatusForPlayerShip(ID+"weapons",
                        "graphics/icons/hullsys/ammo_feeder.png",
                        txt("stm_calm_0"),     
                        (int)(WEAPONS*100)+txt("stm_calm_3"),
                        false
                    );
                }
                break;
            }
            default:
            {
                //drones recalled, speed boost
                if(mode!=1){
                    mode=1;                
                    ship.getMutableStats().getShieldAbsorptionMult().unmodify(ID);
                    ship.getMutableStats().getBeamWeaponFluxCostMult().unmodify(ID);
                    ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(ID);
                    ship.getMutableStats().getBallisticWeaponFluxCostMod().unmodify(ID);
                    ship.getMutableStats().getMissileWeaponFluxCostMod().unmodify(ID);
                    
                    ship.getMutableStats().getMaxSpeed().modifyFlat(ID, SPEED);
                }
                
                if(ship==engine.getPlayerShip()){
                    engine.maintainStatusForPlayerShip(ID+"engines",
                        "graphics/icons/hullsys/burn_drive.png",
                        txt("stm_calm_0"),                       
                        "+"+SPEED+txt("stm_calm_1"),
                        false
                    );
                }
                break;
            }
        }
    }
}
