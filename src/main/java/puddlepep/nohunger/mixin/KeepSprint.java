package puddlepep.nohunger.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientPlayerEntity.class)
public class KeepSprint {

    // by default, the game prevents you from running once your hunger reaches 6.0 or lower.
    // with a stamina bar this sucks. set that limit to 0.0, so you can use the whole thing.

    @ModifyConstant(method="tickMovement()V", constant = @Constant(floatValue = 6.0F))
    private float removeStaminaRequirement(float value) {
        return 0.0F;
    }
}
