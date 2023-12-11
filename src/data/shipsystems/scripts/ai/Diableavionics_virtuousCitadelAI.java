package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_virtuousCitadelAI implements ShipSystemAIScript {
    
    private ShipAPI ship;
    private ShipSystemAPI system;
    private final IntervalUtil TICK = new IntervalUtil (.25f,.75f);
    private final float RANGE=1000, PROJ_THRESHOLD=1499, SHIP_THRESHOLD=15;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){
        TICK.advance(amount);
        if(TICK.intervalElapsed()){
            //activation check
            if(!system.isActive()){
                if(!ship.isRetreating() && AIUtils.canUseSystemThisFrame(ship)){

                    //projectile danger
                    List<DamagingProjectileAPI>danger_projs=getProjectileThreats(ship);

                    float dangerRating = 0;
                    for(DamagingProjectileAPI p : danger_projs){
                        dangerRating+=p.getDamageAmount();
                    }

//                    //debug
//                    engine.addFloatingText(new Vector2f(ship.getLocation().x,ship.getLocation().y+20), "proj threat: "+dangerRating, 20, Color.GREEN, ship, .1f, 1f);

                    if(dangerRating>PROJ_THRESHOLD){
                        //immediate high damage danger, flicker the system on 
                        ship.useSystem();
                        TICK.setElapsed(-.5f);
                        ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON,1f);
                        return;
                    }

                    dangerRating=0;
                    //enemy ships threat
                    for(ShipAPI s : AIUtils.getNearbyEnemies(ship, RANGE)){
                        if(s.isDrone() || s.isFighter())continue;

                        dangerRating+= s.getHullSpec().getFleetPoints() *(1.5f-(MathUtils.getDistanceSquared(ship, s)/1000000));
                    }
//                    //debug
//                    engine.addFloatingText(ship.getLocation(), "ship threat: "+dangerRating, 20, Color.RED, ship, .1f, 1f);

                    //the higher the ship's flux, the more conservative the AI gets, non linear
                    float flux_rating = (float)Math.pow(0.25 + ship.getFluxLevel(),2)*SHIP_THRESHOLD;

//                    //debug
//                    engine.addFloatingText(new Vector2f(ship.getLocation().x,ship.getLocation().y-20), "flux rating: "+flux_rating, 20, Color.BLUE, ship, .1f, 1f);

                    if(dangerRating>flux_rating){
                        ship.useSystem();
                        TICK.setElapsed(-.5f);
                        ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON,1f);
                    }
                }
            } else {
                //deactivation logic, pretty simple because most times it will be automatic when the ship lowers its shield
                float deactivationRange = 1000;
                for(WeaponAPI w : ship.getAllWeapons()){
                    if(w.getSpec().getSize()==WeaponSize.MEDIUM){
                        if(w.getRange()>deactivationRange){
                            deactivationRange=w.getRange();
                        }
                    }
                }
                deactivationRange*=0.9f;
                deactivationRange*=ship.getMutableStats().getBallisticWeaponRangeBonus().getBonusMult();
                
                if(ship.isRetreating() || ship.getFluxLevel()>0.85f || getProjectileThreats(ship).isEmpty() && AIUtils.getNearbyEnemies(ship, deactivationRange).isEmpty()){
                    ship.useSystem();
                }
            }
        }
    }

    private List<DamagingProjectileAPI> getProjectileThreats(ShipAPI ship){

        //detect high danger missile and shots threats
        List<DamagingProjectileAPI> danger_projs = new ArrayList<>();

        //get high damage proj aimed at the ship
        for(DamagingProjectileAPI p: CombatUtils.getProjectilesWithinRange(ship.getLocation(), 300)){
            if(
                    p.getDamageAmount()>=100 && 
                    Math.abs(
                            MathUtils.getShortestRotation(
                                    p.getFacing(), 
                                    VectorUtils.getAngle(
                                            p.getLocation(),
                                            ship.getLocation()
                                    )
                            )
                    )<45
            ){
                danger_projs.add(p);
            }
        }

        for(MissileAPI p : AIUtils.getNearbyEnemyMissiles(ship, 300)){
            if(p.getDamageAmount()>=150){
                if(p.isGuided()){
                    danger_projs.add(p);
                } else if(Math.abs(
                            MathUtils.getShortestRotation(
                                    p.getFacing(), 
                                    VectorUtils.getAngle(
                                            p.getLocation(),
                                            ship.getLocation()
                                    )
                            )
                    )<45)
                {
                    danger_projs.add(p);
                }
            }
        }
        return danger_projs;
    }
}