package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_srabFire implements EveryFrameWeaponEffectPlugin {
        
    private boolean runOnce=false, hidden=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1){return;}
        
        if(!runOnce){
            runOnce=true;
            if(weapon.getSlot().isHidden()){
                hidden=true;
            }
        }
        
        if(!hidden && MagicRender.screenCheck(0.25f, weapon.getLocation()) && weapon.getChargeLevel()==1){

            Vector2f loc = new Vector2f(weapon.getSpec().getTurretFireOffsets().get(0));
            VectorUtils.rotate(loc, weapon.getCurrAngle(), loc);
            Vector2f.add(loc, weapon.getLocation(), loc);

            Vector2f vel = new Vector2f(weapon.getShip().getVelocity());

            //flash
            engine.addHitParticle(
                    loc,
                    vel,
                    50,
                    1,
                    0.3f,
                    new Color(220,75,230,255)
            );
            engine.addHitParticle(
                    loc,
                    vel,
                    75,
                    1,
                    0.1f,
                    Color.white
            );

            //sparkes
            for (int i=0; i<15; i++){
                engine.addHitParticle(
                        loc,
                        Vector2f.add(
                                vel,
                                MathUtils.getRandomPointInCone(new Vector2f(), 150, weapon.getCurrAngle()-5, weapon.getCurrAngle()+5),
                                new Vector2f()),
                        2+5*(float)Math.random(),
                        1,
                        0.2f+0.5f*(float)Math.random(),
                        new Color(220,75,230,255)
                );
            }
        }
    }
}