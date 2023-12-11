//by Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.MagicRender;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_virtuous_teleportEffect implements EveryFrameWeaponEffectPlugin {

    private ShipAPI ship;   
    private ShipSystemAPI system;
    
    private final String section = "diableavionics";
    
    //FLICKER VALUES
    private final String trail = "BUBBLE_trail";    
    private final Map<Integer, String> bottomBubble = new HashMap<>();
    {
        bottomBubble.put(0, "BUBBLE_bottom00");
        bottomBubble.put(1, "BUBBLE_bottom01");
        bottomBubble.put(2, "BUBBLE_bottom02");
        bottomBubble.put(3, "BUBBLE_bottom03");
        bottomBubble.put(4, "BUBBLE_bottom04");
        bottomBubble.put(5, "BUBBLE_bottom05");
        bottomBubble.put(6, "BUBBLE_bottom06");
        bottomBubble.put(7, "BUBBLE_bottom07");
        bottomBubble.put(8, "BUBBLE_bottom08");
        bottomBubble.put(9, "BUBBLE_bottom09");
        bottomBubble.put(10, "BUBBLE_bottom10");
        bottomBubble.put(11, "BUBBLE_bottom11");
        bottomBubble.put(12, "BUBBLE_bottom12");
        bottomBubble.put(13, "BUBBLE_bottom13");
        bottomBubble.put(14, "BUBBLE_bottom14");
        bottomBubble.put(15, "BUBBLE_bottom15");
        bottomBubble.put(16, "BUBBLE_bottom16");
        bottomBubble.put(17, "BUBBLE_bottom17");
        bottomBubble.put(18, "BUBBLE_bottom18");
        bottomBubble.put(19, "BUBBLE_bottom19");
        bottomBubble.put(20, "BUBBLE_bottom20");
        bottomBubble.put(21, "BUBBLE_bottom21");
        bottomBubble.put(22, "BUBBLE_bottom22");
        bottomBubble.put(23, "BUBBLE_bottom23");
        bottomBubble.put(24, "BUBBLE_bottom24");
        bottomBubble.put(25, "BUBBLE_bottom25");
        bottomBubble.put(26, "BUBBLE_bottom26");
        bottomBubble.put(27, "BUBBLE_bottom27");
        bottomBubble.put(28, "BUBBLE_bottom28");
        bottomBubble.put(29, "BUBBLE_bottom29");
    }
    private final Map<Integer, String> topBubble = new HashMap<>();
    {
        topBubble.put(0, "BUBBLE_top00");
        topBubble.put(1, "BUBBLE_top01");
        topBubble.put(2, "BUBBLE_top02");
        topBubble.put(3, "BUBBLE_top03");
        topBubble.put(4, "BUBBLE_top04");
        topBubble.put(5, "BUBBLE_top05");
        topBubble.put(6, "BUBBLE_top06");
        topBubble.put(7, "BUBBLE_top07");
        topBubble.put(8, "BUBBLE_top08");
        topBubble.put(9, "BUBBLE_top09");
        topBubble.put(10, "BUBBLE_top10");
        topBubble.put(11, "BUBBLE_top11");
        topBubble.put(12, "BUBBLE_top12");
        topBubble.put(13, "BUBBLE_top13");
        topBubble.put(14, "BUBBLE_top14");
        topBubble.put(15, "BUBBLE_top15");
        topBubble.put(16, "BUBBLE_top16");
        topBubble.put(17, "BUBBLE_top17");
        topBubble.put(18, "BUBBLE_top18");
        topBubble.put(19, "BUBBLE_top19");
        topBubble.put(20, "BUBBLE_top20");
        topBubble.put(21, "BUBBLE_top21");
        topBubble.put(22, "BUBBLE_top22");
        topBubble.put(23, "BUBBLE_top23");
        topBubble.put(24, "BUBBLE_top24");
        topBubble.put(25, "BUBBLE_top25");
        topBubble.put(26, "BUBBLE_top26");
        topBubble.put(27, "BUBBLE_top27");
        topBubble.put(28, "BUBBLE_top28");
        topBubble.put(29, "BUBBLE_top29");
    }
    private Vector2f from=new Vector2f();
    
    private boolean runOnce=false;
	
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        
        //initialise the variables
        if (!runOnce || ship==null || system==null){
            ship=weapon.getShip();
            system = ship.getSystem();         
            runOnce=true;
            //return to avoid a null error on the ship
            return;
        }
        
        if(system.isActive()){
            flickerEffect();
        } 
    }
    
    private void flickerEffect(){
        float level=system.getEffectLevel();
  
        //bubble
        if(MagicRender.screenCheck(1,ship.getLocation())){

            //extract animation frame from system level
            float anim;
            if(system.isChargeup()){
                anim=level*14.5f;
                from=new Vector2f(ship.getLocation());
            } else if (system.isChargedown()){
                anim=(1-level)*14.5f + 14.5f;
            } else {
                anim=14.5f;
            }

            //bubble top
            MagicRender.singleframe(
                    Global.getSettings().getSprite(section, topBubble.get((int)(anim))),
                    ship.getLocation(), 
                    new Vector2f(256,256), 
                    VectorUtils.getFacing(ship.getVelocity()),
                    new Color(1f,1f,1f,0.5f+0.25f*level),
                    true,
                    CombatEngineLayers.ABOVE_SHIPS_LAYER
            );

            //bubble bottom
            MagicRender.singleframe(
                    Global.getSettings().getSprite(section, bottomBubble.get((int)(anim))),
                    ship.getLocation(), 
                    new Vector2f(256,256), 
                    VectorUtils.getFacing(ship.getVelocity()),
                    new Color(1f,1f,1f,1-0.5f*level),
                    false,
                    CombatEngineLayers.BELOW_SHIPS_LAYER
            );

            //trail on  displacement
            if(level==1){
                MagicRender.battlespace(
                        Global.getSettings().getSprite(section,trail),
                        MathUtils.getMidpoint(ship.getLocation(), from),
                        new Vector2f(),
                        new Vector2f(512,256),
                        new Vector2f(-128,0),
                        VectorUtils.getAngle(from, ship.getLocation()),
                        0,
                        Color.white,
                        true, 
                        0,0,0,0,0,
                        0,
                        0.05f,
                        0.1f,
                        CombatEngineLayers.BELOW_SHIPS_LAYER
                );
            }
        }
    }
}
