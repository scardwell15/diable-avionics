/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicAnim;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Tartiflette
 */
public class Diableavionics_frostEffect implements EveryFrameWeaponEffectPlugin{    
    
    private boolean runOnce=false, slash=false, reffit=false;
    private ShipSystemAPI system;
    private ShipAPI ship, target;
    private AnimationAPI anim;
    private WeaponAPI blade, beam, pauldron, head, gun;
    private float restAngle, effect=0, currentAngle=0, dashAngle=0;
//    private final String ID="diableavionics_frost_effect";
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system = ship.getSystem();
            for(WeaponAPI w : ship.getAllWeapons()){
                switch (w.getSlot().getId()){
                    case "WS0003" :
                        beam=w;
//                        beam.disable(true);
                        break;
                    case "WS0001" :
                        head=w;
                        break;
                    case "WS0002" :
                        gun=w;
                        break;
                    case "ARM" :
                        blade=w;
                        anim=w.getAnimation();
                        restAngle=w.getSlot().getAngle();
                        break;
                    case "PAULDRON" :
                        pauldron=w;
                        break;
                }
            }
            if(ship.getOriginalOwner()==-1){
                reffit=true;
            }
            
            //lock beam
//            ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyPercent(ID, 5000);
            beam.setAmmo(0);
        }
        
        if (engine.isPaused() || reffit || beam==null) {
            return;
        } 
        
        //EFFECT
        if(system.isActive()){
            
            //chargeup
            if(!slash && system.getEffectLevel()<1){
                effect=system.getEffectLevel();
                currentAngle=MagicAnim.smoothNormalizeRange(effect,0f,0.75f)*-60;
                blade.setCurrAngle(ship.getFacing()+restAngle+currentAngle);
                pauldron.setCurrAngle(ship.getFacing()+restAngle+(currentAngle*0.66f));
                
                int frame=Math.round(6*MagicAnim.smoothNormalizeRange(effect,0.25f,1f));
                anim.setFrame(frame);
            }
            
            if(system.getEffectLevel()==1){
                //active                
                if(!slash){    
                    slash=true;
                    effect=0;
                    beam.repair();
                    beam.setAmmo(1);
                    ship.getWeaponGroupFor(beam).toggleOn();
                    ship.getWeaponGroupFor(beam).getAutofirePlugin(beam).shouldFire();
                    
                    //dash angle
                    if(target!=null){
                        dashAngle = VectorUtils.getAngle(ship.getLocation(), target.getLocation());  
                    } else {
                        dashAngle = ship.getFacing();
                    }
                }            
                
//                CombatUtils.applyForce(ship, dashAngle, amount*(20000000/ship.getVelocity().lengthSquared()));
                
                
                Vector2f dash = new Vector2f(MathUtils.getPointOnCircumference(
                        null, //center (0,0)
                        Math.min((50000000/ship.getVelocity().lengthSquared()),10000)*amount, //capped speed increase
                        dashAngle //direction
                ));
                // Apply our velocity change
                Vector2f.add(dash, ship.getVelocity(), ship.getVelocity());
                
                if(MagicRender.screenCheck(0.1f, ship.getLocation())){
                    //trail                
                    ship.addAfterimage(
                            new Color (255,255,255,100),
                            0,      
                            0,      
                            -0.5f*ship.getVelocity().x,
                            -0.5f*ship.getVelocity().y,
                            1f,
                            0.1f,
                            0.1f,
                            0.1f,
                            false,
                            true,
                            false
                    );
                }
                gun.setRemainingCooldownTo(0.5f);
                head.setRemainingCooldownTo(0.5f);
                
                effect=Math.min(effect+amount*6,1);                
                currentAngle=(MagicAnim.smoothNormalizeRange(effect,0,1)*200) - 60;
                blade.setCurrAngle(ship.getFacing()+restAngle+currentAngle);
                pauldron.setCurrAngle(ship.getFacing()+restAngle+(currentAngle*0.66f));
                if(effect<1){                    
                    anim.setFrame(7);
                } else {
                    anim.setFrame(6);
                }
                
            } else if (slash){            
                //chargedown                
                effect=system.getEffectLevel();
                
                currentAngle=MagicAnim.smoothNormalizeRange(effect,0,0.8f)*140;
                blade.setCurrAngle(ship.getFacing()+restAngle+currentAngle);
                pauldron.setCurrAngle(ship.getFacing()+restAngle+(currentAngle*0.66f));
                
                int frame=Math.round(6*MagicAnim.smoothNormalizeRange(effect,0,0.8f));
                anim.setFrame(frame);
            }
            
            
        } else {
            //reset
            if(slash){
                slash=false;
//                beam.disable(true); 
                beam.setAmmo(0);
                blade.setCurrAngle(ship.getFacing()+restAngle);
                pauldron.setCurrAngle(ship.getFacing()+restAngle);                
                anim.setFrame(0);
            }
        }
    }
}
