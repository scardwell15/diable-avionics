//by Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicAnim;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.List;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_zephyrEffect implements EveryFrameWeaponEffectPlugin {

    private WeaponAPI rail;
    private WeaponAPI lbeam;
    private WeaponAPI larm;
    private WeaponAPI lchest;
    private WeaponAPI lpauldron;
    private WeaponAPI lrodA;
    private WeaponAPI lrodB;
    private WeaponAPI rbeam;
    private WeaponAPI rarm;
    private WeaponAPI rchest;
    private WeaponAPI rpauldron;
    private WeaponAPI rrodA;
    private WeaponAPI rrodB;
    private ShipAPI ship;   
    private ShipSystemAPI system;
    
    private final String railID = "RAIL"; 
    private final String lbeamID = "LEFT_BEAM"; 
    private final String larmID = "LEFT_ARM"; 
    private final String lchestID = "LEFT_CHEST"; 
    private final String lpauldronID = "LEFT_PAULDRON"; 
    private final String lrodAID = "LEFT_ROD_A"; 
    private final String lrodBID = "LEFT_ROD_B"; 
    private final String rbeamID = "RIGHT_BEAM"; 
    private final String rarmID = "RIGHT_ARM"; 
    private final String rchestID = "RIGHT_CHEST"; 
    private final String rpauldronID = "RIGHT_PAULDRON"; 
    private final String rrodAID = "RIGHT_ROD_A"; 
    private final String rrodBID = "RIGHT_ROD_B"; 
    
    private final String ID = "DiableAvionics_megaDeathBeam";  
//    private final String soundId="diableavionics_wanzer_transform";   
    private final float SPEED=-0.5f, TURN_RATE=0.5f, TURN_ACC=1f;    
    private final String zapSprite="zap_0";
    private final int zapFrames=8;    
    
    private AnimationAPI anmrail;
    private AnimationAPI anmLarm;
    private AnimationAPI anmLrodA;
    private AnimationAPI anmLrodB;
    private AnimationAPI anmRarm;
    private AnimationAPI anmRrodA;
    private AnimationAPI anmRrodB;
    
    private boolean runOnce=false, transforming=false;
//    private boolean transformIn=false, transformOut=false;
    
    private float armWidth, armHeight, chestWidth, chestHeight, railWidth, railHeight, rodWidth, rodHeight;
    private int rodFrame;
    private float armWidthOffset=-16, armHeightOffset=-13, chestWidthOffset=7, chestHeightOffset=9, railOffset=-12;
    
    private float charge=0;
    
    private final IntervalUtil sparkle = new IntervalUtil(0.05f, 0.5f);
	
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
                    case railID:
                        rail=w;
                        anmrail=rail.getAnimation();                        
                        anmrail.setFrame(1);                        
                        railWidth=rail.getSprite().getWidth()/2;
                        railHeight=rail.getSprite().getHeight()/2;
                        anmrail.setFrame(0);
                        break;
                    case lbeamID:
                        lbeam=w;
                        break;
                    case larmID:
                        larm=w;
                        anmLarm=larm.getAnimation();
                        armWidth=larm.getSprite().getWidth()/2;
                        armHeight=larm.getSprite().getHeight()/2;
                        break;
                    case lchestID:
                        lchest=w;
                        chestWidth=lchest.getSprite().getWidth()/2;
                        chestHeight=lchest.getSprite().getHeight()/2;
                        break;
                    case lpauldronID:
                        lpauldron=w;
                        break;
                    case lrodAID:
                        lrodA=w;
                        anmLrodA=lrodA.getAnimation();   
                        rodFrame=anmLrodA.getNumFrames()-1;
                        anmLrodA.setFrame(1);                        
                        rodWidth=lrodA.getSprite().getWidth()/2;
                        rodHeight=lrodA.getSprite().getHeight();
                        anmLrodA.setFrame(0);                        
                        break;
                    case lrodBID:
                        lrodB=w;
                        anmLrodB=lrodB.getAnimation();   
                        break;
                    case rbeamID:
                        rbeam=w;
                        break;
                    case rarmID:
                        rarm=w;
                        anmRarm=rarm.getAnimation();
                        break;
                    case rchestID:
                        rchest=w;
                        break;
                    case rpauldronID:
                        rpauldron=w;
                        break;
                    case rrodAID:
                        rrodA=w;
                        anmRrodA=rrodA.getAnimation();   
                        break;
                    case rrodBID:
                        rrodB=w;
                        anmRrodB=rrodB.getAnimation();   
                        break;
                }                
            }            
            runOnce=true;
            //return to avoid a null error on the ship
            return;
        }
        
        
        //Charge level
        charge=Math.max(system.getEffectLevel(), charge-(amount/2));
        float transform = MagicAnim.smoothNormalizeRange(charge,0,0.5f);
//        float loading = MagicAnim.SR(charge,0.5f,1f);
        float transformA = MagicAnim.smoothNormalizeRange(charge,0,0.3f);
        float transformB = MagicAnim.smoothNormalizeRange(charge,0.1f,0.4f);
        float transformC = MagicAnim.smoothNormalizeRange(charge,0.2f,0.5f);
        
//        float transformAB = MagicAnim.SR(charge,0f,0.3f);
//        float transformBC = MagicAnim.SR(charge,0.1f,0.4f);
        
        float transformD = MagicAnim.smoothNormalizeRange(charge,0.5f,0.7f);
        float transformE = MagicAnim.smoothNormalizeRange(charge,0.6f,0.8f);
        float transformF = MagicAnim.smoothNormalizeRange(charge,0.7f,0.9f);
        
        
        //TRANSFORM
        
        float facing = ship.getFacing();
        float langle = lbeam.getCurrAngle();
        float rangle = rbeam.getCurrAngle();
        
        //facing
        larm.setCurrAngle(facing+(1-transformA)*MathUtils.getShortestRotation(facing, langle));        
        rarm.setCurrAngle(facing+(1-transformA)*MathUtils.getShortestRotation(facing, rangle));
        
        lpauldron.setCurrAngle(facing-(30*transformB)+(1-transformA)*(0.66f*MathUtils.getShortestRotation(facing, langle)));
        rpauldron.setCurrAngle(facing+(30*transformB)+(1-transformA)*(0.66f*MathUtils.getShortestRotation(facing, rangle)));
        
        //position
        if(transform>0){
            
            transforming=true;
            
//            if(!transformIn){
//                transformIn=true;
//                Global.getSoundPlayer().playSound(soundId, 1, 1, ship.getLocation(), ship.getVelocity());            
//            }

            if(ship.getShipTarget()!=null){
                ship.setAngularVelocity(
                        2.5f*MathUtils.getShortestRotation(
                        ship.getFacing(),
                        VectorUtils.getAngle(
                                ship.getLocation(),
                                ship.getShipTarget().getLocation()
                        )
                ));
            }
            
            //frame
            if(transform==1){
                
                anmLarm.setFrame(1);
                anmRarm.setFrame(1);                
                anmrail.setFrame(1);

                //rods            
                rail.getSprite().setCenter(railWidth, railHeight+(transformD*railOffset));
                
                anmLrodA.setFrame(1+(int)((rodFrame-1)*transformE));
                anmLrodB.setFrame(1+(int)((rodFrame-1)*transformF));
                anmRrodA.setFrame(1+(int)((rodFrame-1)*transformE));
                anmRrodB.setFrame(1+(int)((rodFrame-1)*transformF));
                
                lrodA.getSprite().setCenter(rodWidth, (1-transformE)*rodHeight);
                lrodB.getSprite().setCenter(rodWidth, (1-transformF)*rodHeight);
                rrodA.getSprite().setCenter(rodWidth, (1-transformE)*rodHeight);
                rrodB.getSprite().setCenter(rodWidth, (1-transformF)*rodHeight);
                
//                transformOut=true;
                
            } else {
                anmLarm.setFrame(0);
                anmRarm.setFrame(0);
                anmrail.setFrame(0);
                anmLrodA.setFrame(0);
                anmLrodB.setFrame(0);  
                anmRrodA.setFrame(0);
                anmRrodB.setFrame(0);
                
//                if(transformOut){
//                    transformOut=false;
//                    Global.getSoundPlayer().playSound(soundId, 1.25f, 0.75f, ship.getLocation(), ship.getVelocity()); 
//                }
            }
            
            larm.getSprite().setCenter((armWidth)+(transformC*armWidthOffset), armHeight+(transformB*armHeightOffset));
            rarm.getSprite().setCenter((armWidth)-(transformC*armWidthOffset), armHeight+(transformB*armHeightOffset));            
            
            //chest
            lchest.setCurrAngle(facing+(15*transformA));
            rchest.setCurrAngle(facing-(15*transformA));
            
            lchest.getSprite().setCenter((chestWidth)+(transformB*chestWidthOffset), chestHeight+(transformC*chestHeightOffset));
            rchest.getSprite().setCenter((chestWidth)-(transformB*chestWidthOffset), chestHeight+(transformC*chestHeightOffset));       
            
        }
        
        //RESET
         if(transform==0 && transforming){
            transforming=false;
             
            anmLarm.setFrame(0);
            anmRarm.setFrame(0);
            anmrail.setFrame(0);
            anmLrodA.setFrame(0);
            anmLrodB.setFrame(0);
            anmRrodA.setFrame(0);
            anmRrodB.setFrame(0);
            
            larm.getSprite().setCenter(armWidth, armHeight);
            rarm.getSprite().setCenter(armWidth, armHeight);
            
            lchest.setCurrAngle(facing);
            rchest.setCurrAngle(facing);            
            lchest.getSprite().setCenter(chestWidth, chestHeight);
            rchest.getSprite().setCenter(chestWidth, chestHeight);     
            
//            transformIn=false;
//            transformOut=false;
         }
        
        //STATS
        
        if(transform>0){
            ship.getMutableStats().getMaxSpeed().modifyMult(ID, 1+transform*SPEED);
            ship.getMutableStats().getMaxTurnRate().modifyMult(ID, 1+transform*TURN_RATE);
            ship.getMutableStats().getTurnAcceleration().modifyMult(ID, 1+transform*TURN_ACC);
            
            lbeam.setAmmo(0);
            rbeam.setAmmo(0);
        } else {            
            ship.getMutableStats().getMaxSpeed().unmodify(ID);
            ship.getMutableStats().getMaxTurnRate().unmodify(ID);
            ship.getMutableStats().getTurnAcceleration().unmodify(ID);
        }
        
        //FLUFF
        
        if(transform==1) {
            sparkle.advance(amount);
            if(sparkle.intervalElapsed()){
                int chooser = new Random().nextInt(zapFrames - 1) + 1;
                float rand = 0.25f*(float)Math.random()+0.75f;

                Vector2f point = MathUtils.getRandomPointInCone(new Vector2f(), 30, ship.getFacing()+115, ship.getFacing()+245);
                Vector2f vel = new Vector2f((Vector2f)ship.getVelocity());
                vel.scale(0.8f);
                Vector2f.add(vel, point, vel);
                
                Vector2f loc = new Vector2f(ship.getLocation());
                Vector2f.add(loc, point, loc);

                MagicRender.battlespace(
                        Global.getSettings().getSprite("fx",zapSprite+chooser),
                        loc,
                        vel,
                        new Vector2f(36*rand,36*rand),
                        new Vector2f((float)Math.random()*20,(float)Math.random()*20),
                        (float)Math.random()*360,
                        (float)(Math.random()-0.5f)*10,
                        new Color(255,175,255),
                        true,
                        0,
                        0.2f+(float)Math.random()*0.5f,
                        0.2f
                );

                Vector2f.add(point, ship.getLocation(), point);
                
                engine.addHitParticle(
                        point,
                        vel,
                        30*rand,
                        1,
                        0.1f,
                        new Color(100,150,255,255)
                );
            }
        }
    }
}
