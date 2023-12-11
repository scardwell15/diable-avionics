package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

public class Diableavionics_avalancheHit implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        float size = MathUtils.getRandomNumberInRange(32, 64);
        
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","flakPuff"),
                point,
                new Vector2f(),
                new Vector2f(size,size),
                new Vector2f(16,16), 
                360*(float)Math.random(),
                0, 
                new Color(255,240,230,64), 
                false,
                0,
                0.1f,
                1.9f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","flakFlash"),
                point,
                new Vector2f(),
                new Vector2f(size,size),
                new Vector2f(8,8), 
                360*(float)Math.random(),
                0, 
                new Color(255,100,25,128), 
                true,
                0f,
                0.1f,
                1.4f
        );
        
//        engine.addHitParticle(
//                point,
//                new Vector2f(),
//                250,
//                0.1f, 
//                1f,
//                Color.red);
//        engine.addSmoothParticle(
//                point,
//                new Vector2f(),
//                350,
//                2f, 
//                0.25f,
//                Color.white);
//        engine.addSmoothParticle(
//                point,
//                new Vector2f(),
//                300,
//                2f, 
//                0.1f,
//                Color.white);
    }
}