package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;

public class Diableavionics_hexafireFire implements EveryFrameWeaponEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon.getShip().getOriginalOwner() == -1) {
            return;
        }

        if (weapon.getChargeLevel() == 1) {
            float fluxBoost = Math.min(weapon.getShip().getFluxLevel(), 0.75f) / 0.75f;

            for (DamagingProjectileAPI p : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 100)) {

                if (p.getWeapon() != weapon) continue;

                p.getVelocity().scale(MathUtils.getRandomNumberInRange(0.9f, 1.05f));

                //extra damage with hard flux levels
                p.setDamageAmount(p.getBaseDamageAmount() * (1 + fluxBoost));

                if (MagicRender.screenCheck(0.25f, weapon.getLocation())) {

                    float growth = MathUtils.getRandomNumberInRange(100, 200);

                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx", "muzzleHexafire"),
                            new Vector2f(p.getLocation()),
                            new Vector2f(MathUtils.getPoint(
                                    new Vector2f(),
                                    MathUtils.getRandomNumberInRange(
                                            50,
                                            100
                                    ),
                                    MathUtils.getRandomNumberInRange(
                                            p.getFacing() - 5,
                                            p.getFacing() + 5
                                    )
                            )),
                            new Vector2f(32, 32),
                            new Vector2f(growth, 2 * growth),
                            MathUtils.getRandomNumberInRange(
                                    p.getFacing() - 85,
                                    p.getFacing() - 95
                            ),
                            0,
                            new Color(255, 255, 255, 128),
                            true,
                            0,
                            0.05f,
                            MathUtils.getRandomNumberInRange(0.1f, 0.2f)
                    );
                }
            }
        }
    }
}