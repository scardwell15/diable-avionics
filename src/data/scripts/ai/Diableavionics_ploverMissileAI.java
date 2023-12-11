package data.scripts.ai;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;

public class Diableavionics_ploverMissileAI implements MissileAIPlugin, GuidedMissileAI {

    private final MissileAPI missile;
    private CombatEntityAPI target;
    
    public Diableavionics_ploverMissileAI(MissileAPI missile, ShipAPI launchingShip) {	
        this.missile = missile;
        //halve the damage due to the onHit effect
        missile.setDamageAmount(missile.getBaseDamageAmount()/2);
    }

    @Override
    public void advance(float amount) {
        if(missile.isArmed() && !missile.isFizzling() && !missile.isFading()){
            missile.giveCommand(ShipCommand.ACCELERATE);
        }
    }

    @Override
    public CombatEntityAPI getTarget()
    {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target)
    {
        this.target = target;
    }
}