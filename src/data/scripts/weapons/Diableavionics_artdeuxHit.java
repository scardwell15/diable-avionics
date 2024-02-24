package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

public class Diableavionics_artdeuxHit implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        if(Math.random()-0.2f<(projectile.getSource().getFluxLevel()*0.8f)){
        
            float size = MathUtils.getRandomNumberInRange(48, 64);
            
            engine.applyDamage(target, point, projectile.getDamageAmount()*2, DamageType.FRAGMENTATION, 0, false, false, projectile.getSource());
            
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","flakPuff"),
                    point,
                    new Vector2f(),
                    new Vector2f(size,size),
                    new Vector2f(32,32), 
                    360*(float)Math.random(),
                    0, 
                    new Color(255,255,255,64), 
                    false,
                    0,
                    0.1f,
                    0.4f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","flakFlash"),
                    point,
                    new Vector2f(),
                    new Vector2f(size*1.25f,size*1.25f),
                    new Vector2f(64,64), 
                    360*(float)Math.random(),
                    0, 
                    new Color(255,255,255,225), 
                    true,
                    0f,
                    0.05f,
                    0.1f
            );
        }
    }
}