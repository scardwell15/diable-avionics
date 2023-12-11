package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_nocturneEffect implements BeamEffectPlugin {

    private final IntervalUtil fireInterval = new IntervalUtil(0.05f, 0.25f);
    private final IntervalUtil muzzleInterval = new IntervalUtil(0.1f, 0.1f);
    private boolean wasZero = true;  
    private final String zapSprite="zap_0";
    private final int zapFrames=8;
    
    private boolean runOnce=false, hidden=false;
    private final String hardpoint="nocturneH";
    private final String turret="nocturneT";
    private String muzzle;
    private Vector2f offset = new Vector2f();

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        
        
        if(engine.isPaused() || beam.getWeapon().getShip().getOriginalOwner()==-1){return;}
        if(!runOnce){
            runOnce=true;
            if(beam.getWeapon().getSlot().isHidden()){
                hidden=true;
            }
            if(beam.getWeapon().getSlot().isHardpoint()){
                muzzle=hardpoint;
                offset=new Vector2f(8,0);
            } else {
                muzzle=turret;
            }
        }
        
        //muzzle
        if(!hidden && MagicRender.screenCheck(0.25f, beam.getFrom())){
            muzzleInterval.advance(amount);
            if(muzzleInterval.intervalElapsed()){

                Vector2f vel = new Vector2f(beam.getSource().getVelocity());
                vel.scale(0.8f);
                Vector2f.add(vel, MathUtils.getPoint(new Vector2f(), 50, beam.getWeapon().getCurrAngle()), vel);

                Vector2f loc=new Vector2f(); 
                if(offset!=new Vector2f()){
                    loc = new Vector2f(offset);
                    VectorUtils.rotate(loc, beam.getWeapon().getCurrAngle());
                }
                Vector2f.add(loc,beam.getWeapon().getLocation(), loc);

                MagicRender.battlespace(
                        Global.getSettings().getSprite("fx",muzzle),
                        new Vector2f(loc),
                        new Vector2f(vel),
                        new Vector2f(beam.getWeapon().getSprite().getWidth(),beam.getWeapon().getSprite().getHeight()),
                        new Vector2f(), 
                        beam.getWeapon().getCurrAngle()-90, 
                        0, 
                        new Color(200,100,160,255), 
                        true,
                        0,
                        0.1f*beam.getBrightness(),
                        0.3f*beam.getBrightness()
                );
            }
        }
        
        CombatEntityAPI target = beam.getDamageTarget();
        
        if (target!=null && beam.getBrightness() >= 0.5f) {
                        
            float dur = beam.getDamage().getDpsDuration();
            // needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
            if (!wasZero) dur = 0;
            wasZero = beam.getDamage().getDpsDuration() <= 0;
                        
            fireInterval.advance(dur);            
            if (fireInterval.intervalElapsed()) {
                                
                if( MagicRender.screenCheck(0.25f, beam.getTo())){
                    
                    //SOUND (clamped in screenspace because it's short)
                    if(Math.random()>0.5f){
                        Global.getSoundPlayer().playSound("diableavionics_nocturne_hit", MathUtils.getRandomNumberInRange(0.9f, 1.1f), 1, beam.getTo(), target.getVelocity());
                    }
                    
                    //ZAPS
                    int chooser = new Random().nextInt(zapFrames - 1) + 1;
                    float rand = 0.25f*(float)Math.random()+0.75f;

                    Vector2f point = MathUtils.getRandomPointInCircle(new Vector2f(), 50);
                    Vector2f vel = new Vector2f((Vector2f)target.getVelocity());
                    vel.scale(0.8f);
                    Vector2f.add(vel, point, vel);

                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx",zapSprite+chooser),
                            new Vector2f(beam.getTo()),
                            new Vector2f(vel),
                            new Vector2f(24*rand,24*rand),
                            new Vector2f((float)Math.random()*20,(float)Math.random()*20),
                            (float)Math.random()*360,
                            (float)(Math.random()-0.5f)*10,
                            new Color(255,175,255),
                            true,
                            0,
                            0.2f+(float)Math.random()*0.5f,
                            0.2f
                    );

                    engine.addHitParticle(
                            beam.getTo(),
                            vel,
                            60*rand,
                            1,
                            0.2f,
                            new Color(100,150,255,255)
                    );
                    engine.addHitParticle(
                            beam.getTo(),
                            vel,
                            75*rand,
                            1,
                            0.05f,
                            Color.WHITE
                    );
                }
                
                //ARCS
                if(target instanceof ShipAPI && Math.random()>0.5f){
                    ShipAPI ship = (ShipAPI) target;
                    boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
                    float pierceChance = ((ShipAPI)target).getHardFluxLevel() - 0.1f;
                    pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

                    boolean piercedShield = hitShield && (float) Math.random() < pierceChance;

                    if (!hitShield || piercedShield) {
                        float emp = beam.getWeapon().getDamage().getFluxComponent() * 0.5f;

                        engine.spawnEmpArcPierceShields(
                                beam.getSource(),
                                beam.getTo(),
                                beam.getDamageTarget(),
                                beam.getDamageTarget(),
                                DamageType.ENERGY, 
                                0,
                                emp,
                                100000f,
                                "tachyon_lance_emp_impact",
                                beam.getWidth() + 5f,
                                beam.getFringeColor(),
                                beam.getCoreColor()
                        );
                    }
                }
            }
        }
    }
}
