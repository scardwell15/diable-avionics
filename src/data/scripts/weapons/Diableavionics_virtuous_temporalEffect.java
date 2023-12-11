//by Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.magiclib.plugins.MagicTrailPlugin;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class Diableavionics_virtuous_temporalEffect implements EveryFrameWeaponEffectPlugin {

    private WeaponAPI lpauldronglow;
    private WeaponAPI rpauldronglow;
    private ShipAPI ship;   
    private ShipSystemAPI system;
    
    private final String lpauldronglowID = "PAULDRON_LG"; 
    private final String rpauldronglowID = "PAULDRON_RG"; 
    
    //trails
    private float idL, idR;
    private final SpriteAPI TRAIL = Global.getSettings().getSprite("fx", "base_trail_smooth");
    
    private boolean runOnce=false, activated=false;
    
    private float tick=0;
    
    private final Color TRAIL_IN= Color.magenta;
    private final Color TRAIL_OUT= new Color(25,140,200);
    private final Color JITTER= new Color(100,160,255);
    private final Color AFTERIMAGE_IN= new Color(150,200,255);
    private final Color AFTERIMAGE_OUT= new Color(25,0,25);
	
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        
        //initialise the variables
        if (!runOnce || ship==null || system==null){
            ship=weapon.getShip();
            system = ship.getSystem();
            List <WeaponAPI> weapons = ship.getAllWeapons();
            for (WeaponAPI w : weapons){
                switch(w.getSlot().getId()){
                    case lpauldronglowID:
                        lpauldronglow=w;
                        break;
                    case rpauldronglowID:
                        rpauldronglow=w;
                        break;
                }                
            }            
            
            idL = MagicTrailPlugin.getUniqueID();
            idR = MagicTrailPlugin.getUniqueID();
            
            runOnce=true;
            //return to avoid a null error on the ship
            return;
        }
        
        if(system.isActive()){
            activated=true;
            temporalEffect(amount);
        } else if(activated){
            
            idL = MagicTrailPlugin.getUniqueID();
            idR = MagicTrailPlugin.getUniqueID();
        }
    }
    
    private void temporalEffect(float amount){
        float level=system.getEffectLevel();
        
        ship.setJitter(ship, JITTER, 0.25f*level, 3, 50*level);
        ship.setJitterUnder(ship, JITTER, level, 5, 5*level);
        ship.setJitterShields(true);
            
        tick+=amount;        
        if(tick>0.05f){
            tick-=0.05f;
        
            //body trail
            ship.addAfterimage(
                    AFTERIMAGE_OUT,
                    0, 0,
                    -ship.getVelocity().x, -ship.getVelocity().y,
                    0,
                    0.2f, 0.1f, 0.7f, 
                    true, false, false
            );
            ship.addAfterimage(
                    AFTERIMAGE_IN,
                    0, 0,
                    -ship.getVelocity().x, -ship.getVelocity().y,
                    0,
                    0.05f, 0.05f, 0.3f, 
                    true, false, false
            );            
            
            /**
            * Spawns a trail piece, which links up with other pieces with the same ID
            * to form a smooth trail. This function has all available functions; if you
            * just want to spawn a normal trail without all the extra configuration involved,
            * use AddTrailMemberSimple instead.
            *
            * @param linkedEntity         The entity this trail is attached to, used for cutting trails.
            *                             Can be Null, but that should really only be done in weird, edge-case scenarios
            * @param ID                   The ID for this specific trail. Preferably get this from getUniqueID,
            *                             but it's not required: just expect very weird results if you don't
            * @param sprite               Which sprite to draw for this trail: do *not* change this halfway through a trail,
            *                             as that will split it into two trails
            * @param position             Starting position for this piece of trail
            * @param startSpeed           The starting speed, in SU, this trail piece is moving at. The trail piece smoothly
            *                             transitions from its startSpeed to its endSpeed over its duration
            * @param endSpeed             The ending speed, in SU, this trail piece is moving at. The trail piece smoothly
            *                             transitions from its startSpeed to its endSpeed over its duration
            * @param angle                Which angle this piece of trail has in degrees; determines which direction it moves,
            *                             and which direction its size is measured over
            * @param startAngularVelocity The angular velocity this trail piece has when spawned. The angular velocity
            *                             of a trail piece smoothly transitions from startAngularVelocity to
            *                             endAngularVelocity over its duration
            * @param endAngularVelocity   The angular velocity this trail piece has just before disappearing.
            *                             The angular velocity of a trail piece smoothly transitions from
            *                             startAngularVelocity to endAngularVelocity over its duration
            * @param startSize            The starting size (or rather width) this piece of trail has. Measured in SU. A trail
            *                             smoothly transitions from its startSize to its endSize over its duration
            * @param endSize              The ending size (or rather width) this trail piece has. Measured in SU. A trail smoothly
            *                             transitions from its startSize to its endSize over its duration
            * @param startColor           The color this piece of trail has when spawned. Can be changed in the middle of a trail,
            *                             and will blend smoothly between pieces. Ignores alpha component entirely. Each trail piece
            *                             smoothly transitions from startColor to endColor over its duration
            * @param endColor             The color this piece of trail has just before disappearing. Can be changed in the middle of a
            *                             trail, and will blend smoothly between pieces. Ignores alpha component entirely. Each trail piece
            *                             smoothly transitions from startColor to endColor over its duration
            * @param opacity              The starting opacity of this piece of trail. Is a value between 0f and 1f. The opacity
            *                             gradually approaches 0f over the trail's duration
            * @param inDuration           How long this trail spends "fading in"; for this many seconds, the opacity of the trail piece
            *                             steadily increases until reaching "opacity". A trail's total duration is
            *                             inDuration + mainDuration + outDuration
            * @param mainDuration         How long a trail uses its maximum opacity. A trail's total duration is
            *                             inDuration + mainDuration + outDuration
            * @param outDuration          How long a trail spends "fading out"; over this many seconds at the end of the trail's
            *                             duration, its opacity goes from "opacity" to 0f. A trail's total duration is
            *                             inDuration + mainDuration + outDuration
            * @param additive             Whether this trail will use additive blending or not. Does not support being changed in
            *                             the middle of a trail
            * @param textureLoopLength    How many SU it takes for the texture to loop. Should preferably be non-zero. If the
            *                             trail is not supposed to loop, put this as -1f
            * @param textureScrollSpeed   How fast, and in which direction, the texture scrolls over the trail. Defined so that
            *                             1000 means scrolling the entire texture length once per second, and 2000 means
            *                             scrolling the entire texture length twice per second
            * @param textureOffset        Optional texture offset to prevent repetitions between trails, default:0, fixed random offset: -1;
            * @param offsetVelocity       The offset velocity of the trail; this is an additional velocity that is
            *                             unaffected by rotation and facing, and will never change over the trail's lifetime
            * @param advancedOptions      The most unique and special options go in a special Map<> here. Be careful to input the
            *                             correct type values and use the right data keys. Any new features will be added here to
            *                             keep compatibility with old mod versions. Can be null. Currently supported keys:
            *                             "SIZE_PULSE_WIDTH" :  Float - How much additional width the trail gains each "pulse", at
            *                             most. Used in conjunction with SIZE_PULSE_COUNT
            *                             "SIZE_PULSE_COUNT" :  Integer - How many times the trail "pulses" its width over its
            *                             lifetime. Used in conjunction with SIZE_PULSE_WIDTH
            *                             "FORWARD_PROPAGATION" :  Boolean - If the trail uses the legacy render method of
            *                             "forward propagation". Used to be the default. CANNOT be
            *                             changed mid-trail
            * @param layerToRenderOn      Which combat layer to render the trail on. All available layers are specified in
            *                             CombatEngineLayers. Old behaviour was CombatEngineLayers.BELOW_INDICATORS_LAYER.
            *                             CANNOT change mid-trail, under any circumstance
            * @param frameOffsetMult      The per-frame multiplier for the per-frame velocity offset magnitude. Used to finely
            *                             adjust trail offset at different speeds. Default: 1f
            */
            
            MagicTrailPlugin.addTrailMemberAdvanced(
                    ship, 
                    idL, 
                    TRAIL,
                    MathUtils.getPoint(lpauldronglow.getLocation(), 30, lpauldronglow.getCurrAngle()+115), 
                    100, 50, //speed
//                    lpauldronglow.getCurrAngle()+115,
                    VectorUtils.getFacing(ship.getVelocity()),
                    -ship.getAngularVelocity(),
                    0, //angle
                    24, 64, //size
                    TRAIL_IN, TRAIL_OUT, //colors
                    0.75f*level, //opacity
                    0.05f, 0.1f, 0.7f, //time
                    true, //additive
                    256, 32, //texture size and scroll
                    -1f,
                    null,
                    null, 
                    null, 
                    0
            );
            
            MagicTrailPlugin.addTrailMemberAdvanced(
                    ship, 
                    idR, 
                    TRAIL,
                    MathUtils.getPoint(rpauldronglow.getLocation(), 30, rpauldronglow.getCurrAngle()-115), 
                    100, 50, //speed
                    //lpauldronglow.getCurrAngle()-115,
                    VectorUtils.getFacing(ship.getVelocity()),
                    -ship.getAngularVelocity(),
                    0, //angle
                    24, 64, //size
                    TRAIL_IN, TRAIL_OUT, //colors
                    0.75f*level, //opacity
                    0.05f, 0.1f, 0.7f, //time
                    true, //additive
                    256, 32, //texture size and scroll
                    -1f,
                    null,
                    null, 
                    null, 
                    0
            );
        }
    }
}
