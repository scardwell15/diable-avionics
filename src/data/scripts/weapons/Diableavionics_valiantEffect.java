//by Tartiflette
package data.scripts.weapons;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicAnim;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;

public class Diableavionics_valiantEffect implements EveryFrameWeaponEffectPlugin
{
    private WeaponAPI headgun;
    private WeaponAPI rgun;
    private WeaponAPI lgun;
    private WeaponAPI rpauldron;
    private WeaponAPI lpauldron;
    private WeaponAPI rbarrel;
    private WeaponAPI lbarrel;
    private WeaponAPI rcover;
    private WeaponAPI lcover;
    private WeaponAPI rshoulder;
    private WeaponAPI lshoulder;
    private WeaponAPI chest;
    private WeaponAPI back;
    private WeaponAPI head;
    private ShipAPI ship;   
    private ShipSystemAPI system;
    
    public final String headgunID = "HEAD"; 
    public final String leftgunID = "LEFTGUN"; 
    public final String rightgunID = "RIGHTGUN"; 
    public final String leftshoulderID = "TRANSFORM_00"; 
    public final String rightshoulderID = "TRANSFORM_01"; 
    public final String leftbarrelID = "TRANSFORM_02"; 
    public final String rightbarrelID = "TRANSFORM_03"; 
    public final String backID = "TRANSFORM_04"; 
    public final String chestID = "TRANSFORM_05"; 
    public final String headID = "TRANSFORM_06"; 
    public final String leftpauldronID = "TRANSFORM_07"; 
    public final String rightpauldronID = "TRANSFORM_08"; 
    public final String leftcoverID = "TRANSFORM_09"; 
    public final String rightcoverID = "TRANSFORM_10"; 
    
    private boolean runOnce=false, check=true;
    
    private float shoulderWidth, shoulderHeight;
    private float barrelWidth, barrelHeight;
    private float backWidth, backHeight, backFrames;
    private float chestWidth, chestHeight, chestFrames;
    private float headWidth, headHeight, headFrames;
    private float pauldronWidth, pauldronHeight, pauldronFrames;
    private float coverWidth, coverHeight, coverFrames;
    
    private float rate=1;
    private boolean travelDrive = false;
    
    private float shoulderOffsetX=-6;
    
    private float barrelOffsetX=-9;
    private float barrelOffsetY=-2;
    private float barrelRecoil=2;
    private float lrecoil=0, rrecoil=0;
    
    private float backOffsetY=-8;
    
    private float chestOffsetY=1;
    
    private float headOffsetY=2;
    
    private float pauldronOffsetX=-7;
    private float pauldronOffsetY=8;
    
    private float coverOffsetX=-9;
    private float coverOffsetY=11;    
	
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        
        //initialise the variables
        if (!runOnce || ship==null || system==null){
            ship=weapon.getShip();
            system = ship.getSystem();
            List <WeaponAPI> weapons = ship.getAllWeapons();
            for (WeaponAPI w : weapons){
                switch(w.getSlot().getId()){
                    case headgunID:
                        headgun=w;
                        break;
                    case leftgunID:
                        lgun=w;
                        break;   
                    case rightgunID:
                        rgun=w;
                        break;     
                    case leftshoulderID:
                        lshoulder=w;
                        shoulderHeight=w.getSprite().getHeight();
                        shoulderWidth=w.getSprite().getWidth();
                        break;
                    case rightshoulderID:
                        rshoulder=w;
                        break;       
                    case leftbarrelID:
                        lbarrel=w;
                        barrelHeight=w.getSprite().getHeight();
                        barrelWidth=w.getSprite().getWidth();
                        break;
                    case rightbarrelID:
                        rbarrel=w;
                        break;         
                    case backID:
                        back=w;
                        backHeight=w.getSprite().getHeight();
                        backWidth=w.getSprite().getWidth();
                        backFrames=w.getAnimation().getNumFrames();
                        break;             
                    case chestID:
                        chest=w;
                        chestHeight=w.getSprite().getHeight();
                        chestWidth=w.getSprite().getWidth();
                        chestFrames=w.getAnimation().getNumFrames();
                        break;      
                    case headID:
                        head=w;
                        headHeight=w.getSprite().getHeight();
                        headWidth=w.getSprite().getWidth();
                        headFrames=w.getAnimation().getNumFrames();
                        break; 
                    case leftpauldronID:
                        lpauldron=w;
                        pauldronHeight=w.getSprite().getHeight();
                        pauldronWidth=w.getSprite().getWidth();
                        pauldronFrames=w.getAnimation().getNumFrames();
                        break;
                    case rightpauldronID:
                        rpauldron=w;
                        break;       
                    case leftcoverID:
                        lcover=w;
                        coverHeight=w.getSprite().getHeight();
                        coverWidth=w.getSprite().getWidth();
                        coverFrames=w.getAnimation().getNumFrames();
                        break;
                    case rightcoverID:
                        rcover=w;
                        break; 
                }                
            }            
            runOnce=true;
            //return to avoid a null error on the ship
            return;
        }
        
        float FACING=ship.getFacing();
        float LGUN=lgun.getCurrAngle();
        float RGUN=rgun.getCurrAngle();   
        float HEAD=headgun.getCurrAngle();       
        
        //CUSTOM RECOIL
        
        if(lgun.getChargeLevel()==1){
            lrecoil=Math.min(1, lrecoil+0.33f);
        } else {
            lrecoil=Math.max(0, lrecoil-(0.75f*amount));
        }
        
        if(rgun.getChargeLevel()==1){
            rrecoil=Math.min(1, rrecoil+0.33f);
        } else {
            rrecoil=Math.max(0, rrecoil-(0.75f*amount));
        }
        
        //ALL THE STUFF
        
        if(ship.getTravelDrive().isActive()){
            rate = Math.min(1,rate+1.25f*amount);
            travelDrive=true;
        } else if (travelDrive){
            rate = Math.max(0,rate-1.25f*amount);
            if(rate==0){
                travelDrive=false;
            }
        } else {
            rate = system.getEffectLevel();
        }
        
        if (system.isActive() || rate > 0){
            check=true;
            lgun.setRemainingCooldownTo(0.75f);
            rgun.setRemainingCooldownTo(0.75f);
                        
            float smooth1 = MagicAnim.smoothNormalizeRange(rate,0f,0.5f);
            float smooth2 = MagicAnim.smoothNormalizeRange(rate,0.25f,0.75f);
            float smooth3 = MagicAnim.smoothNormalizeRange(rate,0.5f,1f);
            float straight1 = MagicAnim.normalizeRange(rate,0f,0.5f);
            float straight2 = MagicAnim.normalizeRange(rate,0.25f,0.75f);
            float straight3 = MagicAnim.normalizeRange(rate,0.5f,1f);
//            float bump = RSO(rate,0.5f,1f);
            
            //SHOULDERS                      
            
            float lsX = shoulderWidth/2 + shoulderOffsetX*smooth3;
            float rsX = shoulderWidth/2 - shoulderOffsetX*smooth3;
            
            float sY = shoulderHeight/2;            
                    
            lshoulder.getSprite().setCenter(lsX, sY);
            rshoulder.getSprite().setCenter(rsX, sY);   
            
            //BARRELS
            
            lbarrel.setCurrAngle(lgun.getCurrAngle()+smooth1*MathUtils.getShortestRotation(LGUN,FACING));
            rbarrel.setCurrAngle(rgun.getCurrAngle()+smooth1*MathUtils.getShortestRotation(RGUN,FACING));
            
            float lbX = barrelWidth/2 + barrelOffsetX*smooth3;
            float rbX = barrelWidth/2 - barrelOffsetX*smooth3;
            
            float lbY = barrelHeight/2 + barrelOffsetY*smooth2 + barrelRecoil*lrecoil;
            float rbY = barrelHeight/2 + barrelOffsetY*smooth2 + barrelRecoil*rrecoil;
                    
            lbarrel.getSprite().setCenter(lbX, lbY);
            rbarrel.getSprite().setCenter(rbX, rbY);
            
            //BACK
            
            int baF = Math.round(Math.max(0, Math.min(backFrames-1, (straight2*backFrames)-0.5f)));
            
            back.getAnimation().setFrame(baF);
            
            float baX = backWidth/2 ;            
            float baY = backHeight/2 + backOffsetY*smooth3;
                    
            back.getSprite().setCenter(baX, baY);            
            
            //CHEST
            
            int cF = Math.round(Math.max(0, Math.min(chestFrames-1, (straight2*chestFrames)-0.5f)));
            
            chest.getAnimation().setFrame(cF);
            
            float cX = chestWidth/2 ;            
            float cY = chestHeight/2 + chestOffsetY*smooth3;
                    
            chest.getSprite().setCenter(cX, cY);
            
            //HEAD
            
            int hF = Math.round(Math.max(0, Math.min(headFrames-1, (straight1*headFrames)-0.5f)));
            
            head.getAnimation().setFrame(hF);
            
            head.setCurrAngle(headgun.getCurrAngle()+smooth1*MathUtils.getShortestRotation(HEAD,FACING));
            
            float hX = headWidth/2 ;            
            float hY = headHeight/2 + headOffsetY*smooth1;
                    
            head.getSprite().setCenter(hX, hY);
            
            //PAULDRONS       
            
            int pF = Math.round(Math.max(0, Math.min(pauldronFrames-1, (straight3*pauldronFrames)-0.5f)));
            
            lpauldron.getAnimation().setFrame(pF);
            rpauldron.getAnimation().setFrame(pF);
            
            lpauldron.setCurrAngle(lgun.getCurrAngle()+smooth1*MathUtils.getShortestRotation(LGUN,FACING));
            rpauldron.setCurrAngle(rgun.getCurrAngle()+smooth1*MathUtils.getShortestRotation(RGUN,FACING));
            
            float lpX = pauldronWidth/2 + pauldronOffsetX*smooth3;
            float rpX = pauldronWidth/2 - pauldronOffsetX*smooth3;
            
            float pY = pauldronHeight/2 + pauldronOffsetY*smooth2;
                    
            lpauldron.getSprite().setCenter(lpX, pY);
            rpauldron.getSprite().setCenter(rpX, pY);     
            
            //COVERS        
            
            int coF = Math.round(Math.max(0, Math.min(coverFrames-1, (straight3*coverFrames)-0.5f)));
            
            lcover.getAnimation().setFrame(coF);
            rcover.getAnimation().setFrame(coF);
            
            lcover.setCurrAngle(lgun.getCurrAngle()+smooth1*MathUtils.getShortestRotation(LGUN,FACING));
            rcover.setCurrAngle(rgun.getCurrAngle()+smooth1*MathUtils.getShortestRotation(RGUN,FACING));
            
            float lcoX = coverWidth/2 + coverOffsetX*smooth3 + barrelRecoil*lrecoil;
            float rcoX = coverWidth/2 - coverOffsetX*smooth3 + barrelRecoil*rrecoil;
            
            float coY = coverHeight/2 + coverOffsetY*smooth2;
                    
            lcover.getSprite().setCenter(lcoX, coY);
            rcover.getSprite().setCenter(rcoX, coY);    
            
        } else {
            if(check){
                check=false;
                lpauldron.getSprite().setCenter(pauldronWidth/2, pauldronHeight/2);
                rpauldron.getSprite().setCenter(pauldronWidth/2, pauldronHeight/2);                     
                head.getSprite().setCenter(headWidth/2, headHeight/2);                   
                back.getSprite().setCenter(backWidth/2, backHeight/2);                   
                chest.getSprite().setCenter(chestWidth/2, chestHeight/2);                   
                lshoulder.getSprite().setCenter(shoulderWidth/2, shoulderHeight/2);         
                rshoulder.getSprite().setCenter(shoulderWidth/2, shoulderHeight/2);
                
            }
            lbarrel.getSprite().setCenter(barrelWidth/2, barrelHeight/2 + barrelRecoil*lrecoil);
            rbarrel.getSprite().setCenter(barrelWidth/2, barrelHeight/2 + barrelRecoil*rrecoil);
            
            lcover.getSprite().setCenter(coverWidth/2, coverHeight/2 + barrelRecoil*lrecoil);
            rcover.getSprite().setCenter(coverWidth/2, coverHeight/2 + barrelRecoil*rrecoil);                                   
            
            lpauldron.setCurrAngle(LGUN);
            lcover.setCurrAngle(LGUN);
            lbarrel.setCurrAngle(LGUN);
            rpauldron.setCurrAngle(RGUN);
            rcover.setCurrAngle(RGUN);   
            rbarrel.setCurrAngle(RGUN);  
            head.setCurrAngle(HEAD);
            
            lcover.getAnimation().setFrame(0);
            rcover.getAnimation().setFrame(0);
            lpauldron.getAnimation().setFrame(0);
            rpauldron.getAnimation().setFrame(0);
            head.getAnimation().setFrame(0);
            chest.getAnimation().setFrame(0);
            back.getAnimation().setFrame(0);         
        }
    }
}
