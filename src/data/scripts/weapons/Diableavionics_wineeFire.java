package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_wineeFire implements EveryFrameWeaponEffectPlugin {
        
    private boolean runOnce=false;
    private Vector2f muzzle;
    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1){return;}
        
        if(!runOnce){
            runOnce=true;
            if(weapon.getSlot().isHidden()){
                muzzle = new Vector2f();
            } else {
                if(weapon.getSlot().isTurret()){
                    muzzle = weapon.getSpec().getTurretFireOffsets().get(3);
                } else {                    
                    muzzle = weapon.getSpec().getHardpointFireOffsets().get(3);
                }
            }
            return;
        }
        
        //muzzle
        if(MagicRender.screenCheck(0.25f, weapon.getLocation())){
            if(Math.random()<weapon.getChargeLevel()/3 && weapon.getCooldownRemaining()==0){
                
                Vector2f loc = new Vector2f(muzzle);
                VectorUtils.rotate(loc, weapon.getCurrAngle());
                Vector2f.add(loc, weapon.getLocation(), loc);
                loc = MathUtils.getRandomPointInCircle(loc, 10);
                
                Vector2f vel = new Vector2f(weapon.getShip().getVelocity());
                Vector2f.add(vel, MathUtils.getRandomPointInCircle(new Vector2f(), 100), vel);
                
                float size=MathUtils.getRandomNumberInRange(8, 16);
                float glowth=MathUtils.getRandomNumberInRange(64, 128);
                
                MagicRender.battlespace(
                        Global.getSettings().getSprite("fx","zap_0"+MathUtils.getRandomNumberInRange(0, 7)),
                        new Vector2f(loc),
                        new Vector2f(weapon.getShip().getVelocity()),
                        new Vector2f(size,size),
                        new Vector2f(glowth,glowth), 
                        MathUtils.getRandomNumberInRange(0, 360), 
                        MathUtils.getRandomNumberInRange(-10, 10), 
                        new Color(100,255,255,255), 
                        true,
                        0,
                        MathUtils.getRandomNumberInRange(0.05f, 0.15f), 
                        MathUtils.getRandomNumberInRange(0.1f, 0.2f)
                );
            }
        }
    }
}