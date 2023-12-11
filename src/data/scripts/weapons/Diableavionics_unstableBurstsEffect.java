package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.util.IntervalUtil;
//import data.scripts.util.Diableavionics_graphicLibEffects;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_unstableBurstsEffect implements BeamEffectPlugin {
    
    private boolean hasFired=false;
    private float random=1f;
    private final float WIDTH=35, PARTICLES=5;
    private final IntervalUtil timer = new IntervalUtil(0.2f,0.2f);
    
    private final String id="diableavionics_zephyr_firing";
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
        
        if(beam.getBrightness()==1) {            
            Vector2f start = beam.getFrom();
            Vector2f end = beam.getTo();
            
            if (MathUtils.getDistanceSquared(start, end)==0){
                return;
            }
            
            timer.advance(amount);
            if (timer.intervalElapsed()){
                hasFired=false;
                random = MathUtils.getRandomNumberInRange(0.5f, 1);
                
                //beam fluff
                if(MagicRender.screenCheck(0.1f, start)){
                    for(int i=0; i<PARTICLES; i++){
                        Vector2f point = MathUtils.getPointOnCircumference(start,(float)Math.random()*random*300,beam.getSource().getFacing());
                        Vector2f.add(point, MathUtils.getRandomPointInCircle(new Vector2f(), random*WIDTH/3), point);
                        Vector2f vel = new Vector2f(beam.getSource().getVelocity());
                        Vector2f.add(vel, MathUtils.getPointOnCircumference(new Vector2f(), random*WIDTH/2 + (float)Math.random()*25, beam.getSource().getFacing()), vel);

                        engine.addHitParticle(
                                point,
                                vel,
                                3+7*(float)Math.random(),
                                0.5f,
                                0.1f+0.5f*(float)Math.random(),
                                new Color(95,165,200,255)
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
            
            float theWidth = WIDTH * (Math.min(1,(float)FastTrig.cos(20*MathUtils.FPI * Math.min(timer.getElapsed(),0.05f)) + 1)) ;     
            
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