package data.scripts.weapons;

import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

public class Diableavionics_virtuous_shotgunHit implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        engine.addSmoothParticle(point, new Vector2f(), 100, 2, 0.05f, Color.WHITE);
        engine.addHitParticle(point, new Vector2f(), 50, 0.25f, 0.2f, Color.MAGENTA);
        
//        for(int i=0; i<3; i++){
            float color = MathUtils.getRandomNumberInRange(0.25f, 0.75f);
            engine.addNebulaParticle(
                    point,
                    (Vector2f)(new Vector2f(target.getVelocity())).scale(0.75f),
                    MathUtils.getRandomNumberInRange(25,50),
                    MathUtils.getRandomNumberInRange(1.5f, 2),
                    0, 0.25f, 
                    MathUtils.getRandomNumberInRange(0.5f, 1.5f), 
                    new Color(color,color,color, 0.33f),
                    true
            );
//        }
    }
}