//by Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;

public class Diableavionics_virtuous_pauldronsEffect implements EveryFrameWeaponEffectPlugin {

    private WeaponAPI larm;
    private WeaponAPI lpauldron;
    private WeaponAPI lpauldronglow;
    private WeaponAPI rarm;
    private WeaponAPI rpauldron;
    private WeaponAPI rpauldronglow;
    private ShipAPI ship;   
    private AnimationAPI anm_lpauldronglow;
    private AnimationAPI anm_rpauldronglow;
    
    private final String larmID = "LEFT"; 
    private final String lpauldronID = "PAULDRON_L"; 
    private final String lpauldronglowID = "PAULDRON_LG"; 
    private final String rarmID = "RIGHT"; 
    private final String rpauldronID = "PAULDRON_R"; 
    private final String rpauldronglowID = "PAULDRON_RG"; 
    
    private ShipEngineControllerAPI ENGINE;
    private float leftAccel=0f, rightAccel=0f, leftSpin=0, rightSpin=0, leftStraf=0, rightStraf=0;
    private final float MAX_ACCEL=6, MAX_STRAF=3, MAX_SPIN=3, SNAPINESS=6;
    
    private boolean runOnce=false, broken=false;
    
    private ShipSystemAPI system;
    private float systemReady=0;
	
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(broken)return;
        
        //initialise the variables
        if (!runOnce || ship==null){
            ship=weapon.getShip();
            if(ship.getHullSpec().getBuiltInMods().contains("diableavionics_virtuousBroken")){
                broken=true;
                return;
            }
            system=ship.getSystem();
            ENGINE=ship.getEngineController();
            List <WeaponAPI> weapons = ship.getAllWeapons();
            for (WeaponAPI w : weapons){
                switch(w.getSlot().getId()){
                    case larmID:
                        larm=w;
                        break;
                    case lpauldronID:
                        lpauldron=w;
                        break;
                    case lpauldronglowID:
                        lpauldronglow=w;
                        anm_lpauldronglow=w.getAnimation();
                        break;
                    case rarmID:
                        rarm=w;
                        break;
                    case rpauldronID:
                        rpauldron=w;
                        break;
                    case rpauldronglowID:
                        rpauldronglow=w;
                        anm_rpauldronglow=w.getAnimation();
                        break;
                }                
            }            
            runOnce=true;
            //return to avoid a null error on the ship
            return;
        }
        
        if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
            return;
        }
        
        float mult=SNAPINESS*amount;
        
        //acceleration sway
        if(ENGINE.isAccelerating()){
            leftAccel = leftAccel + (-MAX_ACCEL-leftAccel)*mult;
            rightAccel = rightAccel + (MAX_ACCEL-rightAccel)*mult;
        } else if (ENGINE.isAcceleratingBackwards()){
            leftAccel = leftAccel + (MAX_ACCEL-leftAccel)*mult;
            rightAccel = rightAccel + (-MAX_ACCEL-rightAccel)*mult;
        } else if (ENGINE.isDecelerating()){
            leftAccel = leftAccel + (MAX_ACCEL/2-leftAccel)*mult;
            rightAccel = rightAccel + (-MAX_ACCEL/2-rightAccel)*mult;
        } else {
            leftAccel = leftAccel - leftAccel*mult/2;
            rightAccel = rightAccel - rightAccel*mult/2;
        }
        
        //straffing sway
        if (ENGINE.isStrafingLeft()){
            leftStraf = leftStraf + (MAX_STRAF-leftStraf)*mult;
            rightStraf = rightStraf + (MAX_STRAF-rightStraf)*mult;
        } else if (ENGINE.isStrafingRight()){
            leftStraf = leftStraf + (-MAX_STRAF-leftStraf)*mult;
            rightStraf = rightStraf + (-MAX_STRAF-rightStraf)*mult;
        } else {
            leftStraf = leftStraf - leftStraf*mult/2;
            rightStraf = rightStraf - rightStraf*mult/2;
        }

        //spin sway
        if(Math.abs(ship.getAngularVelocity())>5){            
            float max = Math.min(20,Math.abs(ship.getAngularVelocity()))*0.1f;            
            if(ship.getAngularVelocity()>0){
                leftSpin = leftSpin + ((-MAX_SPIN*max)-leftSpin)*mult;
                rightSpin = rightSpin + ((-MAX_SPIN*max)-rightSpin)*mult;    
            } else {
                leftSpin = leftSpin + ((MAX_SPIN*max)-leftSpin)*mult;
                rightSpin = rightSpin + ((MAX_SPIN*max)-rightSpin)*mult; 
            }
        } else {
            leftSpin = leftSpin - leftSpin*mult/2;
            rightSpin = rightSpin - rightSpin*mult/2;
        }
        
        //pauldron orientation
        float leftAngle=ship.getFacing();
        leftAngle+=MathUtils.getShortestRotation(leftAngle, larm.getCurrAngle())/2;
        leftAngle+=leftAccel;
        leftAngle+=leftStraf;
        leftAngle+=leftSpin;
        lpauldron.setCurrAngle(leftAngle);
        lpauldronglow.setCurrAngle(leftAngle);
        
        float rightAngle=ship.getFacing();
        rightAngle+=MathUtils.getShortestRotation(rightAngle, rarm.getCurrAngle())/2;
        rightAngle+=rightAccel;
        rightAngle+=rightStraf;
        rightAngle+=rightSpin;
        rpauldron.setCurrAngle(rightAngle);
        rpauldronglow.setCurrAngle(rightAngle);
        
        //visual glow
        if(system.isChargeup() || system.isOn()){
            systemON(system.getEffectLevel());
            systemReady=-1;
        } else if(system.isChargedown()){
            systemDOWN(system.getEffectLevel());
            systemReady=-1;
        } else {
            if(AIUtils.canUseSystemThisFrame(ship)){
                systemReady+=amount;
                systemREADY(systemReady);
            } else {
                systemReady=-1;
                systemOFF();
            }
        }
    }
    
    private void systemON(float level){
        
        //pauldron glow
        anm_lpauldronglow.setFrame(1);
        anm_rpauldronglow.setFrame(1);
        
        float glow = .5f+level/2;
        
        lpauldronglow.getSprite().setColor(new Color(glow,glow,1,1));
        rpauldronglow.getSprite().setColor(new Color(glow,glow,1,1));
    }
    
    private void systemDOWN(float level){
        
        //pauldron glow
        anm_lpauldronglow.setFrame(1);
        anm_rpauldronglow.setFrame(1);
        
        lpauldronglow.getSprite().setColor(new Color(1,level,level,level));
        rpauldronglow.getSprite().setColor(new Color(1,level,level,level));
    }
    
    private void systemOFF(){
        anm_lpauldronglow.setFrame(0);
        anm_rpauldronglow.setFrame(0);
        
        lpauldronglow.getSprite().setColor(Color.BLACK);
        rpauldronglow.getSprite().setColor(Color.BLACK);
    }
    
    private void systemREADY(float time){
        float glow;
        if(time<0){
            glow = 1+time;
        } else {
            glow = 0.8f + 0.2f * (float)FastTrig.cos(time*3);
        }
        lpauldronglow.getSprite().setColor(new Color(1,1,1,glow));
        rpauldronglow.getSprite().setColor(new Color(1,1,1,glow));
    }
}
