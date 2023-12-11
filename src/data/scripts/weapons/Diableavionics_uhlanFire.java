package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.util.Diableavionics_graphicLibEffects;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_uhlanFire implements EveryFrameWeaponEffectPlugin {
        
    private boolean runOnce=false, firing=false, hidden=false, light=false;
    private SpriteAPI barrel;
    private float barrelHeight=0, recoil=0, ammo=0;
    private final float maxRecoil=-15;
    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1){return;}
        
        if(!runOnce){
            runOnce=true;
            
            if(Global.getSettings().getModManager().isModEnabled("shaderLib")){
                light=true;
            }
            
            if(weapon.getSlot().isHidden()){
                hidden=true;
            } else {
                barrel=weapon.getBarrelSpriteAPI();
                if(weapon.getSlot().isTurret()){
                    barrelHeight=barrel.getHeight()/2;
                } else {                    
                    barrelHeight=barrel.getHeight()/4;
                }
            }
            return;
        }
        
        if(!hidden){

            if(weapon.getChargeLevel()==1 && weapon.getAmmo()<ammo){

                Vector2f muzzle;

                //recoil
                recoil=Math.min(1, recoil+0.33f);

                //muzzle non hidden weapon
                if(MagicRender.screenCheck(0.1f, weapon.getLocation())){

                    muzzle = MathUtils.getPoint(
                            weapon.getLocation(),
                            40-(recoil*maxRecoil),
                            weapon.getCurrAngle()
                    );

                    engine.addHitParticle(
                            muzzle, 
                            weapon.getShip().getVelocity(), 
                            100, 
                            0.5f, 
                            1, 
                            Color.blue
                    );
                    engine.addHitParticle(
                            muzzle, 
                            weapon.getShip().getVelocity(), 
                            150, 
                            1f, 
                            0.3f, 
                            Color.red
                    );
                    engine.addSmoothParticle(
                            muzzle, 
                            weapon.getShip().getVelocity(), 
                            200, 
                            2f, 
                            0.15f, 
                            Color.white
                    );
                    engine.addSmoothParticle(
                            muzzle, 
                            weapon.getShip().getVelocity(), 
                            250, 
                            2f, 
                            0.1f, 
                            Color.white
                    );

                    if(light){
                        Diableavionics_graphicLibEffects.CustomRippleDistortion(
                                muzzle,
                                weapon.getShip().getVelocity(),
                                50,
                                2,
                                false,
                                0,
                                360,
                                0,
                                0.1f,
                                0.15f,
                                0.25f,
                                0.5f,
                                0f
                        );
                    }
                }
            } else {
                //recoil back
                recoil=Math.max(0, recoil-(0.2f*amount));
            }
            
        barrel.setCenterY(barrelHeight-(recoil*maxRecoil));
        ammo=weapon.getAmmo();     
        
        } else {
            //muzzle hidden weapon
            if(MagicRender.screenCheck(0.1f, weapon.getLocation())){

                engine.addHitParticle(
                        weapon.getLocation(), 
                        weapon.getShip().getVelocity(), 
                        150, 
                        0.5f, 
                        1, 
                        Color.blue
                );
                engine.addHitParticle(
                        weapon.getLocation(), 
                        weapon.getShip().getVelocity(), 
                        200, 
                        1f, 
                        0.3f, 
                        Color.red
                );
                engine.addHitParticle(
                        weapon.getLocation(), 
                        weapon.getShip().getVelocity(), 
                        300, 
                        2f, 
                        0.15f, 
                        Color.white
                );

                if(light){
                    Diableavionics_graphicLibEffects.CustomRippleDistortion(
                            weapon.getLocation(),
                            weapon.getShip().getVelocity(), 
                            150, 
                            5, 
                            false,
                            0, 
                            360, 
                            0,
                            0.15f, 
                            0.15f, 
                            1,
                            0,
                            0
                    );
                }
            }
        }
                
        if(firing && weapon.getChargeLevel()<1){
            //sound
            Global.getSoundPlayer().playSound("diableavionics_uhlan_chargedown", 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
            firing=false;
        } else if (weapon.getChargeLevel()==1){
            firing=true;
        }
    }
}