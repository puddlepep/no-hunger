package puddlepep.nohunger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public abstract class HungerToStamina {

    // prevents food from ever (passively) healing you.
    @Inject(method = "canFoodHeal", at = @At("RETURN"), cancellable = true)
    private void noFoodHeal(CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(false);
    }

    // prevent "starving" damage.
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (source == DamageSource.STARVE) {
            info.setReturnValue(false);
            info.cancel();
        }
    }

    // removes exhaustion from the game. (as I will be manually handling it to make it feel smoother.)
    @ModifyVariable(method = "addExhaustion", at = @At("HEAD"), ordinal = 0)
    private float multiplyExhaustion(float exhaustion) {
        return 0;
    }
}
