package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.DAModPlugin;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_derechoEffect implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce=false, animIsOn=true;
    private ShipAPI theShip;
    private ShipSystemAPI theSystem;
    private AnimationAPI theAnim;
    private float animation=0, fade=0, rim=0, axis=0;
    private final float ANIM=0.05f, RANGE=2000, RIMTICK=0.5f;
    private int LENGTH, frame=0;
    private final IntervalUtil timer = new IntervalUtil(0.1f,0.1f);
    private final IntervalUtil sparkle = new IntervalUtil(0.05f,0.15f);
    private List<MissileAPI> locked= new ArrayList<>(), vulnerable= new ArrayList<>();
//    private final float rangeMult=0;
        
    private final String zapSprite="zap_0";
    private final int zapFrames=8;    
    
    private final String stripeSprite="areaStripes";
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    
        if(!runOnce){
            runOnce=true;
            theShip=weapon.getShip();
            theSystem=theShip.getSystem();
            LENGTH=weapon.getAnimation().getNumFrames()-1;
            theAnim=weapon.getAnimation();
            locked.clear();   
            vulnerable.clear();
        }
        
        if(engine.isPaused()){
            return;
        }
        
        if (theSystem.isOn()){
            if (!animIsOn){
                engine.addSmoothParticle(weapon.getLocation(), weapon.getShip().getVelocity(), 3000, 1f, 0.5f, new Color(0.1f,0f,0.15f));
                animIsOn=true;
            }
            
            timer.advance(amount);
            if (timer.intervalElapsed()){
                List <MissileAPI> missiles=AIUtils.getNearbyEnemyMissiles(theShip, RANGE*theShip.getMutableStats().getSystemRangeBonus().getBonusMult()*theSystem.getEffectLevel());
                for (MissileAPI m : missiles){
                    //leave missiles imune to flares alone
                    if(m.getWeaponSpec()!=null && m.getWeaponSpec().getAIHints().contains("IGNORES_FLARES"))continue;
                    
                    if(!locked.contains(m) && !DAModPlugin.DERECHO_IMMUNE.contains(m.getProjectileSpecId())){
                        locked.add(m);
                        if(!DAModPlugin.DERECHO_RESIST.contains(m.getProjectileSpecId())){
                            vulnerable.add(m);
                        }
                    }
                }
            }
            
            lockMissiles(engine,amount);
            
            //AURA
            rim+=amount;
            if(rim>RIMTICK){
                rim=0;
                /*
                    objectspaceRender(
                SpriteAPI sprite,
                CombatEntityAPI anchor,
                Vector2f offset,
                Vector2f vel,
                Vector2f size,
                Vector2f growth,
                float angle,
                float spin,
                boolean parent,
                Color color,
                boolean additive,
                float fadein,
                float full,
                float fadeout,
                boolean fadeOnDeath
                )
                */
                MagicRender.objectspace(
                                        Global.getSettings().getSprite("fx",stripeSprite),
                                        theShip,
                                        new Vector2f(),
                                        theShip.getVelocity(),
                                        (Vector2f) new Vector2f(1160,4000).scale(theSystem.getEffectLevel()*theShip.getMutableStats().getSystemRangeBonus().getBonusMult()),
                                        new Vector2f(116,400),
                                        axis,
                                        +10f,
                                        false,
                                        new Color(0.5f,0.5f,0.5f),
                                        true,
                                        0,0,0,0,0,
                                        1f,
                                        0.5f,
                                        1f,
                                        false,
                                        CombatEngineLayers.BELOW_SHIPS_LAYER 
                                );                
                axis+=45;
            }
            
        } else {
            animIsOn=false;
        }       
        
        if (animIsOn || fade>0){            
            animation+=amount;            
            if(animation>ANIM){
                animation-=ANIM;                   
                
                frame++;
                if(frame>LENGTH){
                    frame=1;
                }
                theAnim.setFrame(frame);
                
                if(animIsOn){
                    fade = Math.min(fade+0.02f,1);
                } else {
                    fade = Math.max(fade-0.02f,0);
                }
                theAnim.setAlphaMult(fade);
            }
        }
    } 
    
    private void lockMissiles(CombatEngineAPI engine, float amount){
        
        boolean sparkling = false;
        sparkle.advance(amount);
        if (sparkle.intervalElapsed()){
            sparkling=true;
        }
        
        if(!locked.isEmpty()){
            for (Iterator<MissileAPI> iter = locked.iterator(); iter.hasNext();) {                
                MissileAPI m = iter.next();  
                if(m.isFading() || m.didDamage() || !engine.isEntityInPlay(m)){
                    iter.remove();
                    if(vulnerable.contains(m)){
                        vulnerable.remove(m);
                    }
                } else {
                    m.setAngularVelocity(0);
                    if(sparkling){
                        //flameout
                        if(Math.random()>0.96 && vulnerable.contains(m)){
                            if (m.getEngineController().isFlamedOut()){
                                m.setArmingTime(m.getFlightTime()+0.2f);
                            } else {
                                m.flameOut();
                            }
                        }
                        
                        //zaps
                        if(Math.random()>0.75 && MagicRender.screenCheck(0.1f, m.getLocation())){
                            int chooser = new Random().nextInt(zapFrames - 1) + 1;
                            float rand = 0.5f*(float)Math.random()+0.5f;
                            
                            MagicRender.objectspace(
                                    Global.getSettings().getSprite("fx",zapSprite+chooser),
                                    m,
                                    new Vector2f(),
                                    new Vector2f(),
                                    new Vector2f(48*rand,48*rand),
                                    new Vector2f((float)Math.random()*20,(float)Math.random()*20),
                                    (float)Math.random()*360,
                                    (float)(Math.random()-0.5f)*10,
                                    false,
                                    new Color(255,175,255),
                                    true,
                                    0,
                                    0.1f+(float)Math.random()*0.1f,
                                    0.1f,
                                    false
                            );
                        }
                    }
                }
            }
        }
    }
}