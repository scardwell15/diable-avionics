package data.scripts.weapons;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class Diableavionics_armorEffect implements EveryFrameWeaponEffectPlugin {
	
    private boolean runOnce=false;
    private final IntervalUtil timer = new IntervalUtil(0.04f,0.06f);
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(!runOnce){
            if(weapon.getShip().getCollisionClass() == CollisionClass.FIGHTER){
                weapon.getShip().setCollisionClass(CollisionClass.SHIP);
            }
            runOnce=true;
        }
        timer.advance(amount);
        if(timer.intervalElapsed()){
            if(weapon.getShip().getFluxTracker().isOverloaded() && weapon.getShip().getShield()!=null){
                engine.applyDamage(weapon.getShip(), weapon.getShip().getLocation(), weapon.getShip().getHitpoints()*2, DamageType.OTHER, 0, true, false, weapon.getShip());
            }
        }
    }
}
