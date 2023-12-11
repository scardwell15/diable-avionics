package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;

public class Diableavionics_uhlanEffect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

//        float angle = 360*(float)Math.random();
        
        if(target instanceof MissileAPI || target instanceof AsteroidAPI)return;
//        if(!projectile.isFading())return;
        
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","muzzleUhlan"),
                point,
                new Vector2f(),
                new Vector2f(96,96),
                new Vector2f(400,400), 
                //angle,
                360*(float)Math.random(),
                0, 
                new Color(255,200,200,255), 
                true,
                0,
                0.1f,
                0.15f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","muzzleUhlan"),
                point,
                new Vector2f(),
                new Vector2f(128,128),
                new Vector2f(200,200), 
                //angle,
                360*(float)Math.random(),
                0, 
                new Color(255,225,225,225), 
                true,
                0.2f,
                0.0f,
                0.3f
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite("fx","muzzleUhlan"),
                point,
                new Vector2f(),
                new Vector2f(196,196),
                new Vector2f(100,100), 
                //angle,
                360*(float)Math.random(),
                0, 
                new Color(255,255,255,200), 
                true,
                0.4f,
                0.0f,
                0.6f
        );
        
        engine.addHitParticle(
                point,
                new Vector2f(),
                250,
                0.1f, 
                1f,
                Color.red);
        engine.addSmoothParticle(
                point,
                new Vector2f(),
                350,
                2f, 
                0.25f,
                Color.white);
        engine.addSmoothParticle(
                point,
                new Vector2f(),
                300,
                2f, 
                0.1f,
                Color.white);
        
    }
}