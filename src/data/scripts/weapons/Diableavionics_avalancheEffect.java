package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.magiclib.util.MagicAnim;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 * @author Tartiflette
 */

public class Diableavionics_avalancheEffect implements EveryFrameWeaponEffectPlugin{    
    
    private boolean runOnce=false, refit=false, doubletake=false, sound=false;
    private ShipSystemAPI system;
    private ShipAPI ship;
    private WeaponAPI ARM, PAULDRON_L, PAULDRON_R, TORSO;
    private SpriteAPI HANDLE, HAND, BARREL;
    private float HANDLE_HEIGHT, HAND_WIDTH, BARREL_HEIGHT;
    private float acceleration=0;
    private final float MAX_ACCELERATION=10, PUMP_ACTION=3, RECOIL=3;
    
    private final String NORMAL="diableavionics_avalancheProjNormal";
    private final String SUPER="diableavionics_avalancheProjSuper";
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system = ship.getSystem();
            for(WeaponAPI w : ship.getAllWeapons()){
                switch (w.getSlot().getId()){
                    case "ARM" :
                        ARM=w;
                        HAND = w.getSprite();
                        HAND_WIDTH=HAND.getWidth()/2;
                        break;
                    case "PAULDRON_L" :
                        PAULDRON_L=w;
                        break;
                    case "PAULDRON_R" :
                        PAULDRON_R=w;
                        break;
                    case "TORSO" :
                        TORSO=w;
                        break;
                }
            }
            
            BARREL=weapon.getSprite();
            BARREL_HEIGHT=BARREL.getHeight()/2;
            HANDLE=weapon.getBarrelSpriteAPI();
            HANDLE_HEIGHT=HANDLE.getHeight()/2;
            
            if(ship.getOriginalOwner()==-1){
                refit=true;
            }
        }
        
        if (engine.isPaused() || refit) {
            return;
        } 
        
        float berserk=system.getEffectLevel();
        float shipFacing=ship.getFacing();
        float weaponOffset=MathUtils.getShortestRotation(shipFacing, weapon.getCurrAngle());
        
        //WEAPON projectiles
        if(weapon.getChargeLevel()==1 || doubletake){
            doubletake = false;
            Vector2f point=null;
            
            for( DamagingProjectileAPI p:CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 400)){
                if(p.getWeapon()==weapon){
                    point=new Vector2f(p.getLocation());
                    engine.removeEntity(p);
                    break;
                }
            }
            
            if(point==null){
                //sometimes the weapon has a frame or two of delay before actually firing at low fps?
                doubletake = true;
            } else {
                if(berserk==1){

                    Vector2f.add(MathUtils.getPoint(new Vector2f(), 150, weapon.getCurrAngle()+180), ship.getVelocity(), ship.getVelocity());

                    for(int i=0; i<6; i++){

                        Vector2f drift = MathUtils.getPoint(new Vector2f(), MathUtils.getRandomNumberInRange(0, 150), weapon.getCurrAngle());
                        Vector2f.add(drift, new Vector2f(ship.getVelocity()), drift);

                        engine.spawnProjectile(
                                ship,
                                weapon,
                                SUPER,
                                point,
                                weapon.getCurrAngle()+MathUtils.getRandomNumberInRange(-weapon.getCurrSpread(), weapon.getCurrSpread()),
                                drift
                        );
                    }
                } else {

                    Vector2f.add(MathUtils.getPoint(new Vector2f(), 100, weapon.getCurrAngle()+180), ship.getVelocity(), ship.getVelocity());

                    for(int i=0; i<6; i++){

                        Vector2f drift = MathUtils.getPoint(new Vector2f(), MathUtils.getRandomNumberInRange(0, 150), weapon.getCurrAngle());
                        Vector2f.add(drift, new Vector2f(ship.getVelocity()), drift);

                        engine.spawnProjectile(
                                ship,
                                weapon,
                                NORMAL,
                                point,
                                weapon.getCurrAngle()+MathUtils.getRandomNumberInRange(-weapon.getCurrSpread(), weapon.getCurrSpread()),
                                drift
                        );
                    }
                }
            }    
        }
        
        //arm acceleration overlap
        float targetAcceleration=0;
        if(ship.getEngineController().isAccelerating()){
            targetAcceleration=-MAX_ACCELERATION;
        } else if (ship.getEngineController().isDecelerating() || ship.getEngineController().isAcceleratingBackwards()){
            targetAcceleration=MAX_ACCELERATION;
        }
        acceleration=Math.max(
                -MAX_ACCELERATION,
                Math.min(
                        MAX_ACCELERATION,
                        acceleration+targetAcceleration*amount/10+(targetAcceleration-acceleration)/20
                )
        );
        
        //TORSO and ARM rotations
        TORSO.setCurrAngle( 
                        shipFacing 
                        +weaponOffset*(0.33f+0.33f*berserk)
                        +45*MagicAnim.smooth(berserk) //turn 45deg in berserk mode
        );
        
        ARM.setCurrAngle(
                        shipFacing
                        -10 
                        +weaponOffset*(0.2f+0.8f*berserk) 
                        +acceleration*(1-berserk) 
                        +90*MagicAnim.smoothNormalizeRange(berserk, 0.25f, 0.75f)
        );
        
        //PAULDRONS always mid way between their arm and torso, not additional computation        
        PAULDRON_R.setCurrAngle(
                        TORSO.getCurrAngle() 
                        + MathUtils.getShortestRotation(TORSO.getCurrAngle(), ARM.getCurrAngle())/2
        );
        PAULDRON_L.setCurrAngle(
                        TORSO.getCurrAngle() 
                        + MathUtils.getShortestRotation(TORSO.getCurrAngle(), weapon.getCurrAngle())*0.33f
        );
        
        //cocking action
        if(weapon.getChargeLevel()>0){
            if(weapon.getChargeLevel()<0.85f && !sound){
                sound = true;
                Global.getSoundPlayer().playSound("diableavionics_snowblast_reload", 1, 0.5f, weapon.getLocation(), ship.getVelocity());
            }
            float pump = MagicAnim.smoothReturnNormalizeRange(weapon.getChargeLevel(), 0.3f, 0.7f);
            float recoil = MagicAnim.smoothNormalizeRange(weapon.getChargeLevel(), 0.6f, 1);
            
            if(berserk>0){
                HAND.setCenterX(HAND_WIDTH+RECOIL/2*recoil+PUMP_ACTION*pump);
                HANDLE.setCenterY(HANDLE_HEIGHT+RECOIL/2*recoil+PUMP_ACTION*pump);
                BARREL.setCenterY(BARREL_HEIGHT+RECOIL/2*recoil);
            } else {
                BARREL.setCenterY(BARREL_HEIGHT+RECOIL*recoil);
                HANDLE.setCenterY(HANDLE_HEIGHT+RECOIL*recoil+PUMP_ACTION*pump);
            }
        } else if(sound){
            sound=false;
        }
    }
}
