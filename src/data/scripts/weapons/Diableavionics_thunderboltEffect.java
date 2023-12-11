package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_thunderboltEffect implements OnHitEffectPlugin {

    private final float MAX_DEPTH = 40, MIN_DEPTH = 5, THRESHOLD = 100, NUM_PARTICLES=25;
    private Vector2f wound = new Vector2f();
    private final WeightedRandomPicker<String> DEBRIS = new WeightedRandomPicker<>();
    {
        DEBRIS.add("DEBRIS_sml0");
        DEBRIS.add("DEBRIS_sml1");
        DEBRIS.add("DEBRIS_sml2");
        DEBRIS.add("DEBRIS_sml3");
        DEBRIS.add("DEBRIS_med0");
        DEBRIS.add("DEBRIS_med1");
        DEBRIS.add("DEBRIS_med2");
        DEBRIS.add("DEBRIS_lrg0");
        DEBRIS.add("DEBRIS_lrg1");
    }
    private boolean inside=true;
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if(shieldHit){
            //double the damage to compensate the lack of effect
            engine.applyDamage(
                    target,
                    point, 
                    projectile.getBaseDamageAmount(),
                    projectile.getDamageType(), 
                    0,
                    false, 
                    false,
                    projectile.getSource()
            );
            
            //visual effect on shield
            engine.addHitParticle(
                    point,
                    new Vector2f(),
                    150,
                    1,
                    0.1f,
                    Color.white
            );
            
            float angle = VectorUtils.getAngle(point, target.getLocation());            
            for(int i=1; i<=NUM_PARTICLES; i++){
                float mult = (float)i/NUM_PARTICLES;
                float grey = MathUtils.getRandomNumberInRange(0.1f+mult/4, 0.2f+mult/2);
                Integer direction = -1 + (MathUtils.getRandomNumberInRange(0, 1))*2;
                
                engine.addNebulaParticle(
                        MathUtils.getPoint(point, 50*mult, angle+direction*90),
                        MathUtils.getPoint(target.getVelocity(), 25+25*mult, angle+direction*90),
                        MathUtils.getRandomNumberInRange(35-mult*25, 45-mult*30),
                        MathUtils.getRandomNumberInRange(1+mult*2, 1+mult*5),
                        mult/2,
                        0.1f,
                        MathUtils.getRandomNumberInRange(0.5f+mult/2, 0.1f+mult/2), 
                        new Color(
                                grey,
                                grey,
                                grey,
                                MathUtils.getRandomNumberInRange(0.2f, 0.5f)
                        ),
                        false
                );
                //flames
                engine.addSwirlyNebulaParticle(
                        MathUtils.getPoint(point, 50*mult, angle+direction*90),
                        MathUtils.getPoint(target.getVelocity(), 25+50*mult, angle+direction*90),
                        MathUtils.getRandomNumberInRange(30-mult*25, 40-mult*30),
                        MathUtils.getRandomNumberInRange(1+mult, 1+mult*2),
                        0.05f,
                        0.05f,
                        MathUtils.getRandomNumberInRange(0.05f+(mult/4), 0.1f+(mult/2)), 
                        new Color(
                                1f,
                                MathUtils.getRandomNumberInRange(0.75f-mult*0.75f, 1f-mult*0.75f),
                                0,
                                1f),
                        true
                );
            }
            
        } else {
            

            //if the target is a ship, that is to say it will not work on asteroids or fighters
//            if (target instanceof ShipAPI && !((ShipAPI)target).isFighter() && !((ShipAPI)target).isDrone() ) {
                
//                ShipAPI ship = (ShipAPI)target;
                wound=point;

                //recursive damage spots
                for(int i=1; i<=5; i++){

//                    wound = penetration(ship,wound,projectile.getFacing());
                    wound = penetration(target,wound,projectile.getFacing());

                    engine.applyDamage(
                            target,
                            wound, 
                            projectile.getBaseDamageAmount()/5f,
                            projectile.getDamageType(), 
                            projectile.getBaseDamageAmount()/5f,
                            true, 
                            false,
                            projectile.getSource()
                    );

                    //debug
//                    engine.addFloatingText(wound, projectile.getBaseDamageAmount()/5+"",10, Color.green, target, 0.1f,0.1f);
                }

                //visual fluff
                //use the end wound to find a penetration vector
                Vector2f.sub(wound, point, wound);

                for(int i=1;i<=NUM_PARTICLES;i++){
                    float mult = (float)i/NUM_PARTICLES;
                    Vector2f pos = new Vector2f();
                    Vector2f.add(point, (Vector2f)(new Vector2f(wound)).scale(mult), pos);
                    Vector2f vel = MathUtils.getRandomPointInCone(target.getVelocity(), MathUtils.getRandomNumberInRange(10-5*mult,30-20*mult), projectile.getFacing()-5, projectile.getFacing()+5);

                    //smoke
                    float grey = MathUtils.getRandomNumberInRange(0.1f+mult/4, 0.2f+mult/2);
                    engine.addNebulaParticle(
                            pos, (Vector2f)(new Vector2f(vel)).scale(2),
                            MathUtils.getRandomNumberInRange(35-mult*25, 45-mult*30),
                            MathUtils.getRandomNumberInRange(1+mult*2, 1+mult*5),
                            mult/2,
                            0.1f,
                            MathUtils.getRandomNumberInRange(0.5f+mult, 2f+mult), 
                            new Color(
                                    grey,
                                    grey,
                                    grey,
                                    MathUtils.getRandomNumberInRange(0.2f, 0.5f)
                            ),
                            false
                    );

                    //flames
                    engine.addSwirlyNebulaParticle(
                            pos, vel,
                            MathUtils.getRandomNumberInRange(30-mult*25, 40-mult*30),
                            MathUtils.getRandomNumberInRange(1+mult, 1+mult*2),
                            0.05f,
                            0.05f,
                            MathUtils.getRandomNumberInRange(0.05f+(mult/4), 0.1f+(mult/2)), 
                            new Color(
                                    1f,
                                    MathUtils.getRandomNumberInRange(0.75f-mult*0.75f, 1f-mult*0.75f),
                                    0,
                                    1f),
                            true
                    );
                    engine.addHitParticle(
                            pos,
                            vel,
                            MathUtils.getRandomNumberInRange(35-mult*25, 45-mult*30),
                            1, 
                            MathUtils.getRandomNumberInRange(0.05f+mult/20, 0.05f+mult/10),
                            Color.white
                    );


                    //debris
                    //make sure debris don't spawn in empty space
                    if(!CollisionUtils.isPointWithinBounds(pos, target)){
                        inside=false;
                    }
                    if(inside && Math.random()>0.66f){
                        String picked = DEBRIS.pick();
                        vel=MathUtils.getRandomPointInCircle((Vector2f)(new Vector2f(vel)).scale(3), 15);
                        float size = MathUtils.getRandomNumberInRange(4, 12);
                        float angle = MathUtils.getRandomNumberInRange(-180, 180);
                        float spin = MathUtils.getRandomNumberInRange(-720, 720);
                        float lifetime = MathUtils.getRandomNumberInRange(1f,3f);

                        MagicRender.battlespace(
                                Global.getSettings().getSprite("diableavionics", picked),
                                pos, 
                                vel,
                                new Vector2f(size,size),
                                new Vector2f(),
                                angle,
                                spin,
                                Color.WHITE,
                                false,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                lifetime,
                                lifetime/2,
                                CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER
                        );
                        MagicRender.battlespace(
                                Global.getSettings().getSprite("diableavionics", picked),
                                pos, 
                                vel,
                                new Vector2f(size,size),
                                new Vector2f(),
                                angle,
                                spin,
                                Color.RED,
                                true,
                                0,
                                0,
                                0,
                                0.75f,
                                0.75f,
                                0.1f,
                                lifetime/2,
                                lifetime/2,
                                CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER
                        );
                    }
                }
//            }
        }
    }
    
//    private Vector2f penetration(ShipAPI ship, Vector2f point, float direction){
    private Vector2f penetration(CombatEntityAPI target, Vector2f point, float direction){
        //check if the point is within the ship
        if(CollisionUtils.isPointWithinBounds(point, target) && target instanceof ShipAPI){
            ShipAPI ship = (ShipAPI)target;
            //find the state of the armor at the impact point
            if(ship.getArmorGrid().getCellAtLocation(point)!=null){
                List <Integer> armorCell = new ArrayList<>();
                for(int i : ship.getArmorGrid().getCellAtLocation(point)){
                    armorCell.add(i);
                }
                float armorAmount = ship.getArmorGrid().getArmorValue(armorCell.get(0),armorCell.get(1));


        //        //debug
        //        Global.getCombatEngine().addFloatingText(point, ""+armorAmount, 15, Color.BLUE, ship, 0.1f, 0.1f);


                //find the level of protection it offers
                float penAmount = 1 - Math.min(1, armorAmount/THRESHOLD);
                //apply modifiers
                penAmount*=ship.getMutableStats().getArmorDamageTakenMult().getModifiedValue();
                //find the actual penetration distance
                float penDepth = Math.max(MIN_DEPTH, penAmount*MAX_DEPTH);
                //return the actual hole
                return MathUtils.getPoint(point, penDepth, direction);
            } else {
            //if the point is outside the armor grid, just return the max distance
            return MathUtils.getPoint(point, MAX_DEPTH, direction);
            }
        } else {
            //if the point is outside the bounds, just return the max distance
            return MathUtils.getPoint(point, MAX_DEPTH, direction);
        }
    }
}
