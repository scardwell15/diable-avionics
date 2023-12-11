//by Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicAnim;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;

public class Diableavionics_versantEffect implements EveryFrameWeaponEffectPlugin {
    
    private final Map<Integer,String> LEFT_SELECTOR = new HashMap<>();
    {
        LEFT_SELECTOR.put(0, "diableavionics_versant_harvest_LEFT");
        LEFT_SELECTOR.put(1, "diableavionics_versant_harvestB_LEFT");
        LEFT_SELECTOR.put(2, "diableavionics_versant_harvestC_LEFT");
    }
    
    private final Map<Integer,String> RIGHT_SELECTOR = new HashMap<>();
    {
        RIGHT_SELECTOR.put(0, "diableavionics_versant_harvest_RIGHT");
        RIGHT_SELECTOR.put(1, "diableavionics_versant_harvestB_RIGHT");
        RIGHT_SELECTOR.put(2, "diableavionics_versant_harvestC_RIGHT");
    }
    
    private WeaponAPI rgun;
    private WeaponAPI lgun;
    private WeaponAPI rdoor;
    private WeaponAPI ldoor;
    private WeaponAPI rbarrel;
    private WeaponAPI lbarrel;
    private WeaponAPI rbit;
    private WeaponAPI lbit;
    private WeaponAPI rpanel;
    private WeaponAPI lpanel;
    private WeaponAPI rshield;
    private WeaponAPI lshield;
    private ShipAPI ship;   
    private ShipSystemAPI system;
    private ShipEngineControllerAPI engines;
    
    public final String leftshieldID = "ENGINE_PANEL_LEFT"; 
    public final String rightshieldID = "ENGINE_PANEL_RIGHT"; 
    public final String leftslotID = "GUN_LEFT"; 
    public final String rightslotID = "GUN_RIGHT"; 
    public final String leftbarrelID = "BARREL_LEFT"; 
    public final String rightbarrelID = "BARREL_RIGHT"; 
    public final String leftdoorID = "SHIELD_LEFT"; 
    public final String rightdoorID = "SHIELD_RIGHT"; 
    public final String leftbitID = "COVER_LEFT"; 
    public final String rightbitID = "COVER_RIGHT"; 
    public final String leftpanelID = "TOP_LEFT"; 
    public final String rightpanelID = "TOP_RIGHT"; 
    
    private boolean runOnce=false;
    private boolean soundIN=true;
    private boolean soundOUT=true;
    
    private float doorWidth, doorHeight, barrelWidth, barrelHeight, bitWidth, bitHeight, panelWidth, panelHeight, shieldWidth, shieldHeight;
    
    private float rate=1;
    private boolean travelDrive = false;
    
    private final float rotateOffset=5;
    private final float doorOffsetX=-10f;
    private final float doorOffsetY=-10.5f;
    private final float barrelOffsetX=-5;
    private final float barrelOffsetY=8;
    private final float barrelRecoil=5;
    
    private float lrecoil=0, rrecoil=0;
    
    private final float bitOffsetX=-8f;
    private final float bitOffsetY=-3f;
    private final float panelOffsetX=5;
    private final float panelOffsetY=5;
    
    private float currentRotateL=0;
    private float currentRotateR=0;
    private final float maxRotate=22.5f;    
    private final float shieldOffsetX=-4;
    private final float shieldOffsetY=-2;
	
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        
        //initialise the variables
        if (!runOnce || ship==null || system==null){
            ship=weapon.getShip();
            
            system = ship.getSystem();
            engines = ship.getEngineController();
            List <WeaponAPI> weapons = ship.getAllWeapons();
            for (WeaponAPI w : weapons){
                switch(w.getSlot().getId()){
                    case leftslotID:
                        lgun=w;
                        break;
                    case rightslotID:
                        rgun=w;
                        break;       
                    case leftdoorID:
                        ldoor=w;
                        doorHeight=w.getSprite().getHeight();
                        doorWidth=w.getSprite().getWidth();
                        break;
                    case rightdoorID:
                        rdoor=w;
                        break;       
                    case leftbarrelID:
                        lbarrel=w;
                        barrelHeight=w.getSprite().getHeight();
                        barrelWidth=w.getSprite().getWidth();
                        break;
                    case rightbarrelID:
                        rbarrel=w;
                        break;       
                    case leftbitID:
                        lbit=w;
                        bitHeight=w.getSprite().getHeight();
                        bitWidth=w.getSprite().getWidth();
                        break;
                    case rightbitID:
                        rbit=w;
                        break;       
                    case leftpanelID:
                        lpanel=w;
                        panelHeight=w.getSprite().getHeight();
                        panelWidth=w.getSprite().getWidth();
                        break;
                    case rightpanelID:
                        rpanel=w;
                        break; 
                    case leftshieldID:
                        lshield=w;
                        shieldHeight=w.getSprite().getHeight();
                        shieldWidth=w.getSprite().getWidth();
                        break;
                    case rightshieldID:
                        rshield=w;
                        break; 
                }                
            }      
            
            if(lgun==null || rgun==null){
                return;
            }
            
            runOnce=true;
            //return to avoid a null error on the ship
            return;
        }
        
        //ENGINES SHIELDS MOVEMENT ROTATIONS     
        
        float ltarget=0;
        float rtarget=0;
        
        if(engines.isAccelerating()){
            ltarget-=maxRotate/2;
            rtarget+=maxRotate/2;
        } else if (engines.isDecelerating()|| engines.isAcceleratingBackwards()){            
            ltarget+=maxRotate;
            rtarget-=maxRotate;
        }
        if(engines.isStrafingLeft()){            
            ltarget+=maxRotate/3;
            rtarget+=maxRotate/1.5f;
        } else if (engines.isStrafingRight()){            
            ltarget-=maxRotate/1.5f;
            rtarget-=maxRotate/3;
        }
        if(engines.isTurningLeft()){          
            ltarget-=maxRotate/2;
            rtarget-=maxRotate/2;            
        } else if (engines.isTurningRight()){                      
            ltarget+=maxRotate/2;
            rtarget+=maxRotate/2;
        }
        
        float rtl = MathUtils.getShortestRotation(currentRotateL, ltarget);
        if (Math.abs(rtl)<0.5f){
            currentRotateL=ltarget;
        } else if (rtl>0) {
            currentRotateL+=0.5f;
        } else {
            currentRotateL-=0.5f;
        }
        
        float rtr = MathUtils.getShortestRotation(currentRotateR, rtarget);
        if (Math.abs(rtr)<0.5f){
            currentRotateR=rtarget;
        } else if (rtr>0) {
            currentRotateR+=0.5f;
        } else {
            currentRotateR-=0.5f;
        }
        
        float FACING=ship.getFacing();
        float LGUN=lgun.getCurrAngle();
        float RGUN=rgun.getCurrAngle();        
        
        //CUSTOM RECOIL
        if(lgun.getChargeLevel()==1){
            lrecoil=Math.min(1, lrecoil+0.33f);
        } else {
            lrecoil=Math.max(0, lrecoil-(0.5f*amount));
        }
        
        if(rgun.getChargeLevel()==1){
            rrecoil=Math.min(1, rrecoil+0.33f);
        } else {
            rrecoil=Math.max(0, rrecoil-(0.5f*amount));
        }
        
        //ALL THE STUFF
        
        if(ship.getTravelDrive().isActive() || ship.getFluxTracker().isVenting()){
            rate = Math.min(1,rate+amount);
            travelDrive=true;
        } else if (travelDrive){
            rate = Math.max(0,rate-amount);
            if(rate==0){
                travelDrive=false;
            }
        } else {
            rate = system.getEffectLevel();
        }
        
        if (rate==0){
            soundIN=false;
        } else if (rate==1){
            soundOUT=false;
        }
        
        if (system.isActive() || rate > 0){
            
            if(rate>0 && !soundIN){   
                soundIN=true;
                Global.getSoundPlayer().playSound("diableavionics_transform_in", 1, 1, ship.getLocation(), ship.getVelocity());                        
            } else if (rate<1 && !soundOUT){                
                soundOUT=true;
                Global.getSoundPlayer().playSound("diableavionics_transform_out", 1, 1, ship.getLocation(), ship.getVelocity());                     
            }
            
            lgun.setRemainingCooldownTo(1);
            rgun.setRemainingCooldownTo(1);
                        
            float rotateDoors = MagicAnim.smoothNormalizeRange(rate,0.25f,0.75f);
            float slideDoors = MagicAnim.smoothNormalizeRange(rate,0f,0.5f);
            float recessDoors = MagicAnim.smoothNormalizeRange(rate,0.5f,1f);
            
            float clipDoors = MagicAnim.smoothReturnNormalizeRange(rate,0.5f,1f);
            
            //BARRELS
            lbarrel.setCurrAngle(lgun.getCurrAngle()+slideDoors*MathUtils.getShortestRotation(LGUN,FACING));
            rbarrel.setCurrAngle(rgun.getCurrAngle()+slideDoors*MathUtils.getShortestRotation(RGUN,FACING));
            
            float lbX = barrelWidth/2 + barrelOffsetX*rotateDoors;
            float rbX = barrelWidth/2 - barrelOffsetX*rotateDoors;
            
            float lbY = barrelHeight/2 + barrelOffsetY*slideDoors + barrelRecoil*lrecoil;
            float rbY = barrelHeight/2 + barrelOffsetY*slideDoors + barrelRecoil*rrecoil;
                    
            lbarrel.getSprite().setCenter(lbX, lbY);
            rbarrel.getSprite().setCenter(rbX, rbY);
            
            //DOORS
                     
            ldoor.setCurrAngle(lgun.getCurrAngle()+rotateDoors*(MathUtils.getShortestRotation(LGUN,FACING))+(clipDoors*rotateOffset));
            rdoor.setCurrAngle(rgun.getCurrAngle()+rotateDoors*(MathUtils.getShortestRotation(RGUN,FACING))-(clipDoors*rotateOffset));   
            
            float ldX = doorWidth/2 + doorOffsetX*recessDoors;
            float rdX = doorWidth/2 - doorOffsetX*recessDoors;
            
            float dY = doorHeight/2 + doorOffsetY*slideDoors;
                    
            ldoor.getSprite().setCenter(ldX, dY);
            rdoor.getSprite().setCenter(rdX, dY);         
            
            //DOORS REAR BITS
            
            lbit.setCurrAngle(lgun.getCurrAngle()+rotateDoors*(MathUtils.getShortestRotation(LGUN,FACING))+(clipDoors*rotateOffset));
            rbit.setCurrAngle(rgun.getCurrAngle()+rotateDoors*(MathUtils.getShortestRotation(RGUN,FACING))-(clipDoors*rotateOffset));            
            
            float lbtX = bitWidth/2 + bitOffsetX*recessDoors;
            float rbtX = bitWidth/2 - bitOffsetX*recessDoors;
            
            float btY = bitHeight/2 + bitOffsetY*slideDoors;
                    
            lbit.getSprite().setCenter(lbtX, btY);
            rbit.getSprite().setCenter(rbtX, btY);            
            
            //CENTER PANELS
            
            float lpX = panelWidth/2 + panelOffsetX*recessDoors;
            float rpX = panelWidth/2 - panelOffsetX*recessDoors;
            
            float pY = panelHeight/2 + panelOffsetY*rotateDoors;
                    
            lpanel.getSprite().setCenter(lpX, pY);
            rpanel.getSprite().setCenter(rpX, pY);            
            
            //ENGINE SHIELDS
            
            lshield.setCurrAngle(FACING+currentRotateL*(1-slideDoors));
            rshield.setCurrAngle(FACING+currentRotateR*(1-slideDoors));
            
            float lsX = shieldWidth/2 + shieldOffsetX*slideDoors;
            float rsX = shieldWidth/2 - shieldOffsetX*slideDoors;
            
            float sY = shieldHeight/2 + shieldOffsetY*recessDoors;
                    
            lshield.getSprite().setCenter(lsX, sY);
            rshield.getSprite().setCenter(rsX, sY);
         
        } else {
            
            lbarrel.getSprite().setCenter(barrelWidth/2, barrelHeight/2 + barrelRecoil*lrecoil);
            rbarrel.getSprite().setCenter(barrelWidth/2, barrelHeight/2 + barrelRecoil*rrecoil);
            
            lbarrel.setCurrAngle(LGUN);
            ldoor.setCurrAngle(LGUN);
            lbit.setCurrAngle(LGUN);
            rbarrel.setCurrAngle(RGUN);
            rdoor.setCurrAngle(RGUN);
            rbit.setCurrAngle(RGUN);
            lshield.setCurrAngle(FACING+currentRotateL);            
            rshield.setCurrAngle(FACING+currentRotateR);
        }
    }
}
