package data.scripts.weapons;

import com.fs.starfarer.api.combat.CollisionClass;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

public class Diableavionics_virtuous_missileHit implements OnHitEffectPlugin {

    private final Color EXPLOSION_COLOR = new Color(255, 0, 0, 255);
    private final Color PARTICLE_COLOR = new Color(240, 200, 50, 255);
    private final int NUM_PARTICLES = 20;
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        
        DamagingExplosionSpec boom = new DamagingExplosionSpec(
                0.1f,
                100,
                50,
                projectile.getDamageAmount(),
                50,
                CollisionClass.PROJECTILE_NO_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                2,
                5,
                5,
                25,
                new Color(225,100,0),
                new Color(200,100,25)
        );
        boom.setDamageType(DamageType.FRAGMENTATION);
        boom.setShowGraphic(false);
        boom.setSoundSetId("explosion_flak");
        engine.spawnDamagingExplosion(boom, projectile.getSource(), projectile.getLocation());
        
        if(MagicRender.screenCheck(0.1f, projectile.getLocation())){
            engine.addHitParticle(
                projectile.getLocation(),
                new Vector2f(),
                100,
                1,
                0.25f,
                EXPLOSION_COLOR
            );
            for (int i=0; i<NUM_PARTICLES; i++){
                float axis = (float)Math.random()*360;
                float range = (float)Math.random()*100;
                engine.addHitParticle(
                    MathUtils.getPointOnCircumference(projectile.getLocation(), range/5, axis),
                    MathUtils.getPointOnCircumference(new Vector2f(), range, axis),
                    2+(float)Math.random()*2,
                    1,
                    1+(float)Math.random(),
                    PARTICLE_COLOR
                );
            }
            engine.applyDamage(
                    projectile,
                    projectile.getLocation(),
                    projectile.getHitpoints() * 2f,
                    DamageType.FRAGMENTATION,
                    0f,
                    false,
                    false,
                    projectile
            );
        }
    }
}