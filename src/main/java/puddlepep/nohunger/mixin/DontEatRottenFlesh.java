package puddlepep.nohunger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.FoodComponents;

@Mixin(FoodComponents.class)
public abstract class DontEatRottenFlesh {
    
    @Shadow
    public static final FoodComponent ROTTEN_FLESH = new FoodComponent.Builder().hunger(1).saturationModifier(0.1f).statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0), 0.8f).meat().build();

}
