package data.scripts.weapons;

import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class Diableavionics_virtuous_minigunHit implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        if(Math.random()<0.25f){
            engine.addSmoothParticle(point, new Vector2f(), 100, 2, 0.05f, Color.WHITE);
            engine.addHitParticle(point, new Vector2f(), 75, 1, 0.2f, Color.PINK);
            engine.addHitParticle(point, new Vector2f(), 50, 0.1f, 0.5f, Color.RED);
        }
        
        if(!shieldHit){
            for(int i=0; i<MathUtils.getRandomNumberInRange(2, 6); i++){
                float angle = VectorUtils.getAngle(target.getLocation(),point);
                angle = angle + Math.min(90, Math.max(-90 , MathUtils.getShortestRotation(projectile.getFacing()+180, angle)));
                Vector2f velocity = MathUtils.getPoint(new Vector2f(), MathUtils.getRandomNumberInRange(50, 500), angle+MathUtils.getRandomNumberInRange(-15, 15));
                engine.addHitParticle(
                        point, 
                        velocity,
                        MathUtils.getRandomNumberInRange(2, 8),
                        2f,
                        MathUtils.getRandomNumberInRange(0.15f, 0.5f),
                        new Color(255, MathUtils.getRandomNumberInRange(150, 255), 150)
                );
            }
        }
    }
}