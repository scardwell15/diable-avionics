package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

public class Diableavionics_banishEffect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        if(MagicRender.screenCheck(0.25f, projectile.getLocation())){
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","banishSharp"),
                    new Vector2f(point),
                    new Vector2f(),
                    new Vector2f(128,128),
                    new Vector2f(-384,-384), 
                    MathUtils.getRandomNumberInRange(0, 360), 
                    MathUtils.getRandomNumberInRange(-1, 1), 
                    Color.PINK, 
                    false,
                    0,0,0,0,0,
                    0,
                    0.1f,
                    0.2f,
                    CombatEngineLayers.JUST_BELOW_WIDGETS
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","banishDiffuse"),
                    new Vector2f(point),
                    new Vector2f(),
                    new Vector2f(194,194),
                    new Vector2f(-196,-196), 
                    MathUtils.getRandomNumberInRange(0, 360), 
                    MathUtils.getRandomNumberInRange(-1, 1),
                    Color.BLUE, 
                    false,
                    0,0,0,0,0,
                    0f,
                    0.15f,
                    0.45f,
                    CombatEngineLayers.JUST_BELOW_WIDGETS
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","banishDiffuse"),
                    new Vector2f(point),
                    new Vector2f(),
                    new Vector2f(156,156),
                    new Vector2f(-128,-128), 
                    MathUtils.getRandomNumberInRange(0, 360), 
                    MathUtils.getRandomNumberInRange(-1, 1),
                    Color.WHITE, 
                    false,
                    0,0,0,0,0,
                    0.2f,
                    0.05f,
                    0.35f,
                    CombatEngineLayers.JUST_BELOW_WIDGETS
            );
            
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","banishFlash"),
                    new Vector2f(point),
                    new Vector2f(),
                    new Vector2f(128,128),
                    new Vector2f(), 
                    MathUtils.getRandomNumberInRange(0, 360), 
                    MathUtils.getRandomNumberInRange(-1, 1), 
                    Color.WHITE, 
                    true,
                    0,0,0,0,0,
                    0,
                    0.05f,
                    0.05f,
                    CombatEngineLayers.JUST_BELOW_WIDGETS
            );
            
            for(int i=0; i<MathUtils.getRandomNumberInRange(4, 8); i++){
                
                int size = MathUtils.getRandomNumberInRange(16, 54);
                float fade = MathUtils.getRandomNumberInRange(0.15f, 0.5f);
                CombatEngineLayers layer = CombatEngineLayers.JUST_BELOW_WIDGETS;
                if(Math.random()<fade){
                    layer = CombatEngineLayers.BELOW_INDICATORS_LAYER;
                }
                
                MagicRender.battlespace(
                        Global.getSettings().getSprite("fx","banishSharp"),
                        MathUtils.getRandomPointOnCircumference(
                                point,
                                MathUtils.getRandomNumberInRange(32, 128-size)
                        ),
                        new Vector2f(),
                        new Vector2f(size,size),
                        new Vector2f(-size/fade,-size/fade), 
                        MathUtils.getRandomNumberInRange(0, 360), 
                        MathUtils.getRandomNumberInRange(-1, 1), 
                        new Color(128,24,200,255), 
                        false,
                        0,0,0,0,0,
                        0,
                        3*fade/4,
                        fade/4,
                        layer
                );
            }
        }
        
        Global.getSoundPlayer().playSound(
                "diableavionics_banish_blast",
                1f,
                1,
                point,
                target.getLocation()
        );
    }
}