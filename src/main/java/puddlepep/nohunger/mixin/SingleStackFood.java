package puddlepep.nohunger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item.Settings;

// food can be of stack size 1, 2, 4, and 8. which is 2^n
// stack size 1 is reserved for the largest foods, foods that are above a value of 10 (sat/3 + food val).
// from the way the formula is constructed i don't think... 2-stack items are possible? which is ok, i think.

// sets all food in the game to be single-stack.
@Mixin(Settings.class)
public abstract class SingleStackFood {
    @Inject(method = "food", at = @At("HEAD"))
    private void updateStack(FoodComponent foodComponent, CallbackInfoReturnable<Settings> info) {

        double food = foodComponent.getHunger();
        double saturation = Math.floor(foodComponent.getSaturationModifier() * 2.0 * food);
        double lowSaturation = Math.floor(saturation / 3.0);

        // map the health value of the food to a 1-4 range, which will be used to determine its stack size.
        double stackRatio = 1.0 - Math.min((food + lowSaturation) / 10.0, 1.0);
        if (stackRatio < 0.5) stackRatio *= 0.5;

        double stackLevel = Math.round(stackRatio * 3.0);
        int clampedStackLevel = (int)Math.min(3, Math.max(0, stackLevel));
        ((Settings)(Object)this).maxCount((int)Math.pow(2, clampedStackLevel));
    }
}
