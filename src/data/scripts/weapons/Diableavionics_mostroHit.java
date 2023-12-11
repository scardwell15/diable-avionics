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

public class Diableavionics_mostroHit implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        float size = MathUtils.getRandomNumberInRange(64, 96);
        
        
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","flakPuff"),
                point,
                new Vector2f(),
                new Vector2f(size/2,size/2),
                new Vector2f(size,size), 
                360*(float)Math.random(),
                0, 
                new Color(255,240,230,128), 
                false,
                0.1f,
                0.1f,
                0.3f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","flakPuff"),
                point,
                new Vector2f(),
                new Vector2f(size,size),
                new Vector2f(16,16), 
                360*(float)Math.random(),
                0, 
                new Color(255,240,230,128), 
                false,
                0.3f,
                0.2f,
                1.5f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","flakFlash"),
                point,
                new Vector2f(),
                new Vector2f(size/4,size/4),
                new Vector2f(size*2,size*2), 
                360*(float)Math.random(),
                0, 
                new Color(255,200,150,255), 
                true,
                0f,
                0.1f,
                0.1f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","flakFlash"),
                point,
                new Vector2f(),
                new Vector2f(size/2,size/2),
                new Vector2f(size,size), 
                360*(float)Math.random(),
                0, 
                new Color(255,200,150,255), 
                true,
                0.1f,
                0.1f,
                0.2f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","flakFlash"),
                point,
                new Vector2f(),
                new Vector2f(size,size),
                new Vector2f(8,8), 
                360*(float)Math.random(),
                0, 
                new Color(255,100,25,225), 
                true,
                0.2f,
                0.1f,
                0.5f
        );
        
        engine.addHitParticle(
                point,
                new Vector2f(),
                150,
                0.1f, 
                1f,
                Color.red);
//        engine.addSmoothParticle(
//                point,
//                new Vector2f(),
//                250,
//                0.5f, 
//                0.15f,
//                Color.white);
        engine.addSmoothParticle(
                point,
                new Vector2f(),
                200,
                0.5f, 
                0.05f,
                Color.white);
    }
}