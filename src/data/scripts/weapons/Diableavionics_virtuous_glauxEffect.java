package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
//import data.scripts.util.Diableavionics_graphicLibEffects;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_virtuous_glauxEffect implements BeamEffectPlugin {
    
    private boolean hasFired=false;
    private float random=1f;
    private final float WIDTH=25, PARTICLES=5;
    private final IntervalUtil timer = new IntervalUtil(0.1f,0.1f);
    
    private final String id="diableavionics_zephyr_firing";
    private final List <Vector2f> PODS = new ArrayList<>();
    {
        PODS.add(new Vector2f(17.5f,25.5f));
        PODS.add(new Vector2f(27.5f,24.5f));
        PODS.add(new Vector2f(37.5f,23.5f));
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
        
        if(beam.getBrightness()==1) {            
            
            Vector2f start = beam.getFrom();
            Vector2f end = beam.getTo();
            
            //extra damge against fighters
            if(beam.didDamageThisFrame()&& beam.getDamageTarget().getCollisionClass() == CollisionClass.FIGHTER){
                float damage = beam.getDamage().computeDamageDealt(0.1f);
                engine.applyDamage(beam.getDamageTarget(), end, damage, DamageType.ENERGY, damage/2, false, true, beam.getSource());
            }
            
            //visual fluff
            if (MathUtils.getDistanceSquared(start, end)==0){
                return;
            }
            
            timer.advance(amount);
            if (timer.intervalElapsed()){
                hasFired=false;
                if(MagicRender.screenCheck(0.1f, start)){
                    
                    WeaponAPI weapon = beam.getWeapon();
                    ShipAPI ship = beam.getSource();
                    
                    //weapon fluff
                    //pick one pod emplacement
                    Vector2f loc = new Vector2f(PODS.get(MathUtils.getRandomNumberInRange(0,2)));
                    //align on the weapon
                    VectorUtils.rotate(loc, weapon.getCurrAngle());
                    Vector2f.add(loc, weapon.getLocation(), loc);
                    //shuffle the position a smidgen
                    loc = MathUtils.getRandomPointInCircle(loc, 5);

                    Vector2f vel = MathUtils.getPoint(new Vector2f(ship.getVelocity()),MathUtils.getRandomNumberInRange(20, 50),weapon.getCurrAngle()+45);

                    float size=MathUtils.getRandomNumberInRange(8, 16);
                    float glowth=MathUtils.getRandomNumberInRange(32, 64);

                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx","zap_0"+MathUtils.getRandomNumberInRange(0, 7)),
                            new Vector2f(loc),
                            new Vector2f(vel),
                            new Vector2f(size,size),
                            new Vector2f(glowth,glowth), 
                            MathUtils.getRandomNumberInRange(0, 360), 
                            MathUtils.getRandomNumberInRange(-15, 15), 
                            new Color(100,255,255,255), 
                            true,
                            0,0,
                            2f,1f,0,
                            0,
                            MathUtils.getRandomNumberInRange(0.1f, 0.2f), 
                            MathUtils.getRandomNumberInRange(0.15f, 0.25f),
                            CombatEngineLayers.FIGHTERS_LAYER
                    );
                
                    //beam fluff
                    for(int i=0; i<PARTICLES; i++){
                        Vector2f point = MathUtils.getPointOnCircumference(start,(float)Math.random()*300,weapon.getCurrAngle());
                        Vector2f.add(point, MathUtils.getRandomPointInCircle(new Vector2f(), WIDTH/3), point);
//                        Vector2f vel = new Vector2f(beam.getSource().getVelocity());
//                        Vector2f.add(vel, MathUtils.getPointOnCircumference(new Vector2f(), random*WIDTH/2 + (float)Math.random()*25, beam.getSource().getFacing()), vel);
                        vel = MathUtils.getPointOnCircumference(ship.getVelocity(), WIDTH/2 + (float)Math.random()*25, ship.getFacing());

                        engine.addHitParticle(
                                point,
                                vel,
                                3+7*(float)Math.random(),
                                0.5f,
                                0.1f+1f*(float)Math.random(),
                                new Color(255,200,90,255)
                        );
                    }
                    engine.addHitParticle(
                            start,
                            beam.getSource().getVelocity(),
                            50+50*(float)Math.random(),
                            1,
                            0.1f+0.2f*(float)Math.random(),
                            new Color(100,150,255,255)
                    );
                    engine.addHitParticle(
                            start,
                            beam.getSource().getVelocity(),
                            40,
                            1,
                            0.05f,
                            new Color(255,255,255,255)
                    );
                }
            }
            
            float theWidth = WIDTH * (Math.min(1,(float)FastTrig.cos(18*MathUtils.FPI * Math.min(timer.getElapsed(),0.05f)) + 1)) ;     
            
            beam.setWidth(random*theWidth);
                        
            if (!hasFired){   
                hasFired=true;
                //play sound (to avoid limitations with the way weapon sounds are handled)
                Global.getSoundPlayer().playSound(id, 0.75f+0.5f*(float)Math.random(), 1.5f, start, beam.getSource().getVelocity());
            }
        } else {
            hasFired=false;
        }
    }
}