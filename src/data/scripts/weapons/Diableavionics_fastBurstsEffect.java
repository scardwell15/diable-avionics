package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.Diableavionics_graphicLibEffects;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_fastBurstsEffect implements BeamEffectPlugin
{
    private boolean hasFired=false;
    private float width, count=0;
    private final IntervalUtil timer = new IntervalUtil(0.1f,0.1f);
    private String id;
    
    private final String IDa="diableavionics_glaux_firing";
    private final float WIDTHa = 22;
    private final String IDb="diableavionics_burchel_firing";
    private final float WIDTHb = 16;
    
    private boolean runOnce=false;
    private Color fringe;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
        
        if(!runOnce){
            runOnce=true;
            if (beam.getWeapon().getId().equals("diableavionics_glaux")){
                width=WIDTHa;
                id=IDa;
            } else {
                width=WIDTHb;
                id=IDb;
            }
            fringe = beam.getFringeColor();
        }
        
        if(beam.getBrightness()==1 && count<6) {            
            Vector2f start = beam.getFrom();
            Vector2f end = beam.getTo();
            
            if (MathUtils.getDistanceSquared(start, end)==0){
                return;
            }
            
            timer.advance(amount);
            if (timer.intervalElapsed()){
                hasFired=false;
            }
            
            float theWidth = width * ( 0.5f * (float) FastTrig.cos( 20*MathUtils.FPI * Math.min(timer.getElapsed(),0.05f) ) + 0.5f ) ;
            beam.setWidth(theWidth);
                        
            if (!hasFired){   
                hasFired=true; 
                count++;
                //play sound (to avoid limitations with the way weapon sounds are handled)
                Global.getSoundPlayer().playSound(id, 1f, 1f, start, beam.getSource().getVelocity());
                
                //particle effects                
                if(MagicRender.screenCheck(0.25f, start)){
                    engine.addHitParticle(start, beam.getSource().getVelocity(), MathUtils.getRandomNumberInRange(width*2,width*3), 0.5f, 0.15f, fringe);
                    for(int i=0; i<MathUtils.getRandomNumberInRange(3, 5); i++){
                        Vector2f point = MathUtils.getRandomPointInCone(
                                new Vector2f(),
                                width*1.5f,
                                beam.getWeapon().getCurrAngle()-5,
                                beam.getWeapon().getCurrAngle()+5
                        );
                        engine.addHitParticle(
                                new Vector2f(
                                        start.x+point.x,
                                        start.y+point.y
                                ), 
                                new Vector2f(
                                        beam.getSource().getVelocity().x+point.x,
                                        beam.getSource().getVelocity().y+point.y
                                ),
                                MathUtils.getRandomNumberInRange(width/6,width/4),
                                0.25f,
                                0.1f,
                                fringe
                        );
                    }
                    engine.addHitParticle(start, beam.getSource().getVelocity(), MathUtils.getRandomNumberInRange(width,width*2), 0.5f, 0.05f, Color.WHITE);
                    
                    //light
                    if(Global.getSettings().getModManager().isModEnabled("shaderLib")){
                        Diableavionics_graphicLibEffects.customLight(
                                start,
                                beam.getSource(),
                                width*5f,
                                width/25,
                                beam.getFringeColor(),
                                0,
                                0,
                                0.05f
                        );
                    }
                }
            }
        }
    }
}