package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_bladeEffect implements BeamEffectPlugin {    

    private final IntervalUtil timer= new IntervalUtil(0f,0.15f), spark=new IntervalUtil(0f,0.05f);
    private boolean runOnce=false;
    private int select=0;
    private final String SOUND="diableavionics_blade_";

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        
        if(!runOnce){
            select=MathUtils.getRandomNumberInRange(0, 9);
            runOnce=true;
        }
        
        timer.advance(amount);
        spark.advance(amount);
        
        CombatEntityAPI target = beam.getDamageTarget();   
        
        if (target != null && beam.getBrightness() >= 1f){
            if(MagicRender.screenCheck(0.2f, beam.getTo())) { 
                if(timer.intervalElapsed()){
                    engine.spawnExplosion(beam.getTo(), target.getVelocity(), Color.GRAY, (float)Math.random()*15+5, (float)Math.random()+1);
                }
                if(spark.intervalElapsed()){
                    engine.addHitParticle(beam.getTo(), MathUtils.getRandomPointInCircle(new Vector2f(), 100), (float)Math.random()*5+3, 1, (float)Math.random()*0.5f+0.25f, Color.yellow);
                }
            }
            
            //sound
            Global.getSoundPlayer().playLoop(SOUND+select, beam, 1, 1, beam.getTo(), target.getVelocity());
        }
    }
}
