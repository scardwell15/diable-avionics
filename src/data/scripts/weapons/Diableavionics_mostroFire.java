package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_mostroFire implements EveryFrameWeaponEffectPlugin {
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1){return;}
        
        if(MagicRender.screenCheck(0.25f, weapon.getLocation())){
            if(weapon.getChargeLevel()==1){
                for(DamagingProjectileAPI p : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 150)){
                    
                    if(p.getWeapon()!=weapon)continue;

                    engine.addHitParticle(p.getLocation(), weapon.getShip().getVelocity(), 200, 0.5f, 0.33f, new Color(200,96,0,32));
                    engine.addHitParticle(p.getLocation(), weapon.getShip().getVelocity(), 75, 2, 0.1f, Color.WHITE);
                    
                    for(int i=0; i<10; i++){
                        
                        Vector2f vel=MathUtils.getRandomPointInCone(new Vector2f(), i*2, p.getFacing()-5, p.getFacing()+5);
                        Vector2f pos=new Vector2f(p.getLocation());
                        Vector2f.add(pos, vel, pos);
                        Vector2f.add(vel, p.getSource().getVelocity(), vel);
                        int color = MathUtils.getRandomNumberInRange(64,255);
                        
                        engine.addSmokeParticle(
                                pos,
                                vel,
                                MathUtils.getRandomNumberInRange(10, 25),
                                1,
                                MathUtils.getRandomNumberInRange(1f, 3f),
                                new Color(color,color,color,32)
                        );
                    }
                    
                    for(int i=0; i<15; i++){
                        engine.addHitParticle(
                                MathUtils.getRandomPointInCone(p.getLocation(), i*2, p.getFacing()-5, p.getFacing()+5),
                                p.getSource().getVelocity(),
                                MathUtils.getRandomNumberInRange(10, 50-2*i),
                                1,
                                0.1f+0.02f*i,
                                new Color(200, (int)(i*5)+20,20,128)
                        );
                    }                
                }
            }
        }
    }
}