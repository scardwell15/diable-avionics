package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicLensFlare;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_srabEffect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        
        if(MagicRender.screenCheck(0.25f, projectile.getLocation())){

            int offset = MathUtils.getRandomNumberInRange(20, 80);
            boolean closed=false;
            Vector2f pointA = MathUtils.getPoint(point, MathUtils.getRandomNumberInRange(40, 80), projectile.getFacing());
            Vector2f pointB = MathUtils.getPoint(point, MathUtils.getRandomNumberInRange(40, 80), projectile.getFacing()+offset);
            
            while (!closed){
                engine.spawnEmpArc(
                        projectile.getSource(),
                        pointA, 
                        new SimpleEntity(point),
                        new SimpleEntity(pointB),
                        DamageType.KINETIC,
                        0,
                        0, 
                        200, 
                        null,
                        MathUtils.getRandomNumberInRange(2, 4),
                        new Color(150,20,200,32), 
                        new Color(50,10,150,90)
                );
                offset+= MathUtils.getRandomNumberInRange(20, 80);
                if(offset>=360){
                    offset=360;
                    closed=true;
                }
                pointA=pointB;
                pointB=MathUtils.getPoint(point, MathUtils.getRandomNumberInRange(40, 80), projectile.getFacing()+offset);
            }
            
            if(MagicRender.screenCheck(0.25f, point)){
                MagicLensFlare.createSharpFlare(
                        engine,
                        projectile.getSource(),
                        point, 
                        6, 
                        300, 
                        0,
                        new Color(150,20,200,100), 
                        new Color(50,10,150,100)
                );
            }
        }
        Global.getSoundPlayer().playSound(
                "tachyon_lance_emp_impact",
                1.25f,
                1,
                point,
                target.getLocation()
        );
    }
}