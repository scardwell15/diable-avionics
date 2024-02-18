package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

public class BurchelOnHitEffect implements OnHitEffectPlugin {
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if ((target instanceof ShipAPI && ((ShipAPI) target).isFighter()) || target instanceof MissileAPI) {
            damageResult.setDamageToShields(damageResult.getDamageToShields() * 1.25f);
            damageResult.setDamageToHull(damageResult.getDamageToHull() * 1.25f);
            damageResult.setDamageToPrimaryArmorCell(damageResult.getDamageToPrimaryArmorCell() * 1.25f);
            damageResult.setTotalDamageToArmor(damageResult.getTotalDamageToArmor() * 1.25f);
            damageResult.setEmpDamage(damageResult.getEmpDamage() * 1.25f);
            damageResult.setOverMaxDamageToShields(damageResult.getOverMaxDamageToShields() * 1.25f);
        }
    }
}