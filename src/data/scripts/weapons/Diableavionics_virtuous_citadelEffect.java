//by Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;

public class Diableavionics_virtuous_citadelEffect implements EveryFrameWeaponEffectPlugin {

    private ShipAPI ship;   
    private ShieldAPI shield;
    private float shieldArc=0, shieldRadius=0, shieldRotate=0, speedBoost=0, afterimage=0;
    private Color shieldInColor,shieldOutColor;
    private final Color 
            activeInColor = new Color(
                    255-30,
                    255-60,
                    255-100
            ),
            activeOutColor = new Color(
                    255-180,
                    255-230,
                    0
            );
    private ShipSystemAPI system;
    
    private final String ID = "diableavionics_citadel_deactivation";
    
    private boolean runOnce=false, activated=false, deactivated = false;
	
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        
        //initialise the variables
        if (!runOnce || ship==null || system==null){
            ship=weapon.getShip();
            system = ship.getSystem();
            shield = ship.getShield();
            if(shield!=null){
                shieldArc=shield.getArc();
                shieldRadius=shield.getRadius();
                shieldRotate=shield.getInnerRotationRate();
                shieldInColor = shield.getInnerColor();
                shieldOutColor = shield.getRingColor();
            }  
            runOnce=true;
            //return to avoid a null error on the ship
            return;
        }
        
        if(system.isActive() && !ship.getFluxTracker().isOverloadedOrVenting()){
            activated=true;
            speedBoost+=amount;
            citadelEffect(amount);
        } else {
            if(activated)citadelOff();
            if(speedBoost>0){
                deactivated=true;
                if(ship.getFluxTracker().isOverloadedOrVenting()){
                    speedBoost=0;
                } else {
                    afterimage+=amount;
                    citadelDeactivationBoost(speedBoost);
                    speedBoost-=amount*3;
                }
            } else if (deactivated){
                deactivated = false;
                speedBoost=0;
                citadelDeactivationUnboost();
            }
        }
    }
    
    private void citadelDeactivationBoost(float boost){
        ship.getMutableStats().getMaxSpeed().modifyFlat(ID, Math.min(50, boost*5));
        if(afterimage>0.1f){
            afterimage=0;
            //mild visual trail
            ship.addAfterimage(
                    new Color(128,200,255,128),
                    0, 0,
                    -ship.getVelocity().x, -ship.getVelocity().y,
                    0,
                    0, 0, Math.min(1, boost/10), 
                    false, true, false
            );
        }
    }
    private void citadelDeactivationUnboost(){
        ship.getMutableStats().getMaxSpeed().unmodify(ID);
    }
    
    private void citadelEffect(float amount){
        float level=system.getEffectLevel();

        if(shield!=null && shield.isOn()){
            float targetArc = shieldArc + level * (360-shieldArc);
            shield.setActiveArc(shield.getActiveArc() + (targetArc-shield.getActiveArc())*amount*10 );
            shield.setRadius(shieldRadius, "graphics/da/fx/da_shields256.png", "graphics/da/fx/da_shields128ring.png");
            shield.setInnerRotationRate((1-0.75f*level)*shieldRotate);
            
            shield.setInnerColor(
                    new Color(
                            Math.min(255, Math.round(255 - (activeInColor.getRed()*level))),
                            Math.min(255, Math.round(255 - (activeInColor.getGreen()*level))),
                            Math.min(255, Math.round(255 - (activeInColor.getBlue()*level)))
                    )
            );
            shield.setRingColor(
                    new Color(
                            Math.min(255, Math.round(255 - (activeOutColor.getRed()*level))),
                            Math.min(255, Math.round(255 - (activeOutColor.getGreen()*level))),
                            Math.min(255, Math.round(255 - (activeOutColor.getBlue()*level)))
                    )
            );
            ship.setJitter(ship, new Color(25,75,100), level*0.33f, (int)(5*level), level*10f);
        }

    }
    
    private void citadelOff(){
        if(shield!=null){
            shield.setRadius(shieldRadius, "graphics/da/fx/da_shields128base.png", "graphics/da/fx/da_shields128ring.png");
            shield.setInnerRotationRate(shieldRotate);

            shield.setInnerColor(shieldInColor);
            shield.setRingColor(shieldOutColor);
            
            shield.setActiveArc(Math.min(shield.getActiveArc(),shieldArc));
        }
    }
}
