package puddlepep.nohunger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.FoodComponent;

@Mixin(FoodComponent.class)
public class AlwaysEdible {
    
    // allows you to always eat food.
    @Inject(method = "isAlwaysEdible", at = @At("RETURN"), cancellable = true)
    private void yesAllFoodIsEdible(CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(true);
    }
}
