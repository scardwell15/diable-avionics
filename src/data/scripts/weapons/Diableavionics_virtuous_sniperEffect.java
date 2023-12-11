//By Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
//import com.fs.starfarer.api.combat.ShipAPI;
//import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_virtuous_sniperEffect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin{
    
//    private boolean runOnce=false;
//    private ShipAPI SHIP;
//    private ShipEngineControllerAPI ENGINES;
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //failed acceleration-based variable accuracy 
//        if(!runOnce){
//            runOnce=true;
//            SHIP = weapon.getShip();
//            ENGINES = SHIP.getEngineController();
//            weapon.ensureClonedSpec();
//        }
//        
//        if(
//                ENGINES.isAccelerating() 
//                || ENGINES.isAcceleratingBackwards() 
//                || ENGINES.isStrafingLeft() 
//                || ENGINES.isStrafingRight() 
//                || (ENGINES.isDecelerating() && SHIP.getVelocity().lengthSquared()>100)
//                ){
//            weapon.getSpec().setMinSpread(5f);
//            weapon.getSpec().setSpreadDecayRate(5f);
//        } else {
//            weapon.getSpec().setMinSpread(0f);
//            weapon.getSpec().setSpreadDecayRate(20f);
//        }

        if(weapon.getCooldownRemaining()>0){
            weapon.getShip().getMutableStats().getTurnAcceleration().modifyMult(weapon.getId(), 1-(weapon.getCooldownRemaining()/6));
            weapon.getShip().getMutableStats().getAcceleration().modifyMult(weapon.getId(), 1-(weapon.getCooldownRemaining()/3));
        } else {
            weapon.getShip().getMutableStats().getTurnAcceleration().unmodify(weapon.getId());
            weapon.getShip().getMutableStats().getAcceleration().unmodify(weapon.getId());
        }
    }
    
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
                
        engine.addSmoothParticle(
                projectile.getLocation(),
                weapon.getShip().getVelocity(), 
                MathUtils.getRandomNumberInRange(120, 150),
                2,
                0.1f,
                Color.CYAN
        );
        
        for(int i=0; i<10; i++){
            
            Vector2f drift = MathUtils.getRandomPointInCone(weapon.getShip().getVelocity(), MathUtils.getRandomNumberInRange(25, 100), weapon.getCurrAngle()-5, weapon.getCurrAngle()+5);
            
            engine.addNebulaParticle(
                    projectile.getLocation(),
                    drift, 
                    MathUtils.getRandomNumberInRange(20, 40),
                    2,
                    0.1f,
                    0.3f,
                    1f,
                    new Color(150,120,120,100)
            );
        }
        
        //recoil
//        Vector2f vel = weapon.getShip().getVelocity();
        Vector2f.add(weapon.getShip().getVelocity(), MathUtils.getPoint(new Vector2f(), 75, weapon.getCurrAngle()+180), weapon.getShip().getVelocity());
        weapon.getShip().setAngularVelocity(weapon.getShip().getAngularVelocity()-50);
    }
}
