package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicRender;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_deepStrikePodEffect implements EveryFrameWeaponEffectPlugin {    

    private float clock=0, light=0, delay=0;
    private boolean runOnce=false, hold=true;
    private final IntervalUtil checkEnemies = new IntervalUtil (1,2);
    private final List<Integer> ANIMATION = new ArrayList<>();
    {
        ANIMATION.add(0, 0);
        ANIMATION.add(1, 1);
        ANIMATION.add(2, 0);
        ANIMATION.add(3, 1);
        ANIMATION.add(4, 0);
        ANIMATION.add(5, 0);
        ANIMATION.add(6, 0);
        ANIMATION.add(7, 0);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused())return;
        if(weapon.getShip().getOriginalOwner()!=-1 && !weapon.getShip().isAlive()){
            weapon.getSprite().setColor(Color.BLACK);
            weapon.getSprite().setAdditiveBlend();
            if(weapon.getShip().isHulk()){
                engine.removeEntity(weapon.getShip());
            }
            return;
        }
        
        checkEnemies.advance(amount);
        if(checkEnemies.intervalElapsed()){
            ShipAPI enemy = AIUtils.getNearestEnemy(weapon.getShip());
            hold = enemy!=null && MathUtils.isWithinRange(weapon.getLocation(), enemy.getLocation(),2000);
        }
        
        if(hold){
            weapon.getShip().giveCommand(ShipCommand.DECELERATE, null, 0);
        }
        
        if(delay<3){
            delay+=amount;
            for(FighterWingAPI w : weapon.getShip().getAllWings()){
                for(ShipAPI f : w.getWingMembers()){
                    w.getSource().land(f);
                }
            }
        } else {
            if(!runOnce){
                runOnce=true;
                
                //eject doors
                if(MagicRender.screenCheck(1f,weapon.getLocation())){
                    
                    Vector2f vel = new Vector2f(weapon.getShip().getVelocity());
                    Vector2f.add(vel, MathUtils.getPoint(new Vector2f(), 30, weapon.getShip().getFacing()), vel);
                    float turn = weapon.getShip().getAngularVelocity();
                    
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("diableavionics", "DS_doorUp"),
                            weapon.getLocation(),
                            new Vector2f(vel),
                            new Vector2f(14,33),
                            new Vector2f(1,-10),
                            weapon.getShip().getFacing()-90, 
                            turn,
                            Color.WHITE,
                            false,
                            0,
                            1, 
                            1f
                    );
                    
                    Vector2f.add(new Vector2f(weapon.getShip().getVelocity()), MathUtils.getPoint(new Vector2f(), 50, weapon.getShip().getFacing()-60), vel);
                    turn = weapon.getShip().getAngularVelocity()+MathUtils.getRandomNumberInRange(10, 30);
                    
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("diableavionics", "DS_doorRight"),
                            MathUtils.getPoint(weapon.getLocation(),10,weapon.getShip().getFacing()-90),
                            new Vector2f(vel),
                            new Vector2f(8,33),
                            new Vector2f(-1,0),
                            weapon.getShip().getFacing()-90, 
                            turn,
                            Color.WHITE,
                            false,
                            0,
                            1, 
                            1f
                    );
                    
                    Vector2f.add(new Vector2f(weapon.getShip().getVelocity()), MathUtils.getPoint(new Vector2f(), 50, weapon.getShip().getFacing()+60), vel);
                    turn = weapon.getShip().getAngularVelocity()+MathUtils.getRandomNumberInRange(-10, -30);
                    
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("diableavionics", "DS_doorLeft"),
                            MathUtils.getPoint(weapon.getLocation(),10,weapon.getShip().getFacing()+90),
                            new Vector2f(vel),
                            new Vector2f(8,33),
                            new Vector2f(-1,0),
                            weapon.getShip().getFacing()-90, 
                            turn,
                            Color.WHITE,
                            false,
                            0,
                            1, 
                            1f
                    );
                    
                    //flash
                    engine.addHitParticle(weapon.getLocation(), weapon.getShip().getVelocity(), 75, 1, 0.2f, Color.yellow);
                    //sparkles
                    for(int i=0; i<15; i++){
                        Vector2f.add(
                                new Vector2f(weapon.getShip().getVelocity()),
//                                MathUtils.getRandomPointInCircle(new Vector2f(), 20),
                                MathUtils.getPoint(
                                        new Vector2f(),
                                        MathUtils.getRandomNumberInRange(10, 30), 
                                        weapon.getCurrAngle()-((MathUtils.getRandomNumberInRange(0, 1)*2)-1)*MathUtils.getRandomNumberInRange(80, 100)),
                                
                                vel
                        );
                        
                        engine.addHitParticle(
                                MathUtils.getRandomPointInCircle(
                                        weapon.getLocation(), 
                                        15
                                ),
                                vel,
                                MathUtils.getRandomNumberInRange(2,4),
                                1,
                                MathUtils.getRandomNumberInRange(0.5f, 2f),
                                Color.orange
                        );
                    }
                    
                    //sound
                    Global.getSoundPlayer().playSound("diableavionics_deepStrike_open", 1, 0.75f, weapon.getLocation(), weapon.getShip().getVelocity());
                }
            }
            clock+=amount*10;
            if(clock>ANIMATION.size()-1){
                clock-=(ANIMATION.size()-1);
            }
            float mult=-1;
            if(ANIMATION.get(Math.round(clock))>0){
                mult=1;
            }

            light = Math.min(1, Math.max(0,light+(mult*amount*20)));

            weapon.getAnimation().setFrame(1);
            weapon.getSprite().setAdditiveBlend();
            weapon.getSprite().setColor(new Color(1,1,1,light));
        }
    }
}
