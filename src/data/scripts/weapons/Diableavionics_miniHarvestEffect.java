package data.scripts.weapons;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class Diableavionics_miniHarvestEffect implements OnHitEffectPlugin {
    
    private final Color PARTICLE_COLOR = new Color(150, 200, 250);
    private final float PARTICLE_SIZE = 2f;
    private final float PARTICLE_BRIGHTNESS = 1;
    private final float PARTICLE_DURATION = 0.5f;

    private final float EXPLOSION_SIZE = 10f;      
    private final Color EXPLOSION_COLOR = new Color(50, 100, 200);
    
    private final float FLASH_SIZE = 15f;
    private final Color FLASH_COLOR = new Color(150, 230, 250);
    
    private final float GLOW_SIZE = 30;
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        
        if(MagicRender.screenCheck(0.1f, point)){
            for (float i = 0; i <= 5; i++) {
                float particleSize = MathUtils.getRandomNumberInRange(PARTICLE_SIZE-1, PARTICLE_SIZE+1);
                Vector2f randSpawnPoint = MathUtils.getRandomPointOnCircumference(point, EXPLOSION_SIZE);
                Vector2f randExitVector = VectorUtils.getDirectionalVector(point, randSpawnPoint);
                randExitVector.scale(EXPLOSION_SIZE*2);
                engine.addHitParticle(randSpawnPoint, randExitVector, particleSize, PARTICLE_BRIGHTNESS, PARTICLE_DURATION, PARTICLE_COLOR);  
            }

            //void spawnExplosion(Vector2f loc, Vector2f vel, Color color, float size, float maxDuration);
            engine.spawnExplosion(point, new Vector2f(), EXPLOSION_COLOR, EXPLOSION_SIZE + (float)Math.random()*5, 0.5f);
            engine.spawnExplosion(point, new Vector2f(), FLASH_COLOR, FLASH_SIZE + (float)Math.random()*5, 0.25f);
            engine.addHitParticle(point, new Vector2f(), GLOW_SIZE + (float)Math.random()*5, 1, 0.1f, Color.WHITE);    
        }        
    }   
}