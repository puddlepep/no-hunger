package puddlepep.nohunger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import puddlepep.nohunger.NoHunger;
import puddlepep.nohunger.config.Config;

@Mixin(HungerManager.class)
public abstract class StaminaRegen {

    private PlayerEntity playerToHealWithFood;
    private int ticksSinceStaminaRegen = 0;
    private int ticksSinceStaminaLoss = 0;
    public int sprintCooldown = 0;

    private int oldFoodLevel = -1;

    // makes food heal instead of give hunger.
    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void healOnEat(int food, float saturationModifier, CallbackInfo info) {
        if (playerToHealWithFood != null) {
            int saturation = (int)Math.ceil(Math.floor(saturationModifier * 2.0F * food) / 3);
            playerToHealWithFood.heal(food + saturation);
        }
        info.cancel();
    }

    // stamina should regenerate non-linearly, as in, the less stamina you have, the slower it is to regenerate.
    // this should encourage rationing your stamina out better, rather than just using it all in one go.
    // manages stamina usage and regeneration.
    @Inject(method = "update", at = @At("HEAD"))
    private void updateHunger(PlayerEntity player, CallbackInfo info) {

        HungerManager manager = ((HungerManager)(Object)this);
        playerToHealWithFood = player;
        int foodLevel = manager.getFoodLevel();

        // manage creative mode.
        if (player.getAbilities().creativeMode) {
            if (oldFoodLevel == -1) {
                oldFoodLevel = manager.getFoodLevel();
            }

            manager.setFoodLevel(20);
            return;
        }
        else {
            if (oldFoodLevel != -1) {
                manager.setFoodLevel(oldFoodLevel);
                oldFoodLevel = -1;
            }
        }

        // allow indefinite swimming.
        if (player.isSwimming()) {
            return;
        }

        // if you're hungry, disable stamina regeneration.
        StatusEffectInstance hunger = player.getStatusEffect(StatusEffects.HUNGER);
        if (hunger != null) {
            sprintCooldown = Math.max(60, sprintCooldown);
        }

        // regeneration.
        manager.setSaturationLevel(0);
        if (!player.isSprinting()) {

            ticksSinceStaminaLoss = 0;
            if (sprintCooldown <= 0) {
                ++ticksSinceStaminaRegen;

                int regenRate = (int) Math.floor(NoHunger.CONFIG.baseStaminaRegenRate * 20.0f);
                double staminaUsed = 1.0 - Math.min(1.0, manager.getFoodLevel() / 14.0);
                double staminaMultiplier = (staminaUsed * (NoHunger.CONFIG.exhaustionMultiplier - 1.0f)) + 1.0;

                if (ticksSinceStaminaRegen > regenRate * staminaMultiplier) {
                    ticksSinceStaminaRegen = 0;
                    manager.setFoodLevel(Math.min(foodLevel + 1, 20));
                }
            }
            else {
                --sprintCooldown;
            }
        }

        // usage.
        else {
            ticksSinceStaminaRegen = 0;
            ++ticksSinceStaminaLoss;
            if (ticksSinceStaminaLoss > 8) {
                ticksSinceStaminaLoss = 0;
                manager.setFoodLevel(Math.max(foodLevel - 1, 0));

                // if you completely use up your sprint, you need to wait 3 seconds before you can regenerate.
                if (manager.getFoodLevel() == 0) sprintCooldown = (int)Math.floor(NoHunger.CONFIG.exhaustedSprintTimeout * 20.0f);
            }
        }

        manager.setExhaustion(0);
        manager.setSaturationLevel(0);
    }
}
