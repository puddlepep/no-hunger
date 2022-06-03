package puddlepep.nohunger.mixin;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.MathHelper;

// this code kind of sucks. overwriting isn't the best idea here. but it works.
// i'll fix it up later.

@Mixin(ClientPlayerEntity.class)
public abstract class KeepSprint extends AbstractClientPlayerEntity {
    
    @Shadow public int ticksSinceSprintingChanged;
    @Shadow protected int ticksLeftToDoubleTapSprint;
    @Shadow public Input input;
    @Shadow private boolean inSneakingPose;
    @Shadow protected MinecraftClient client;
    @Shadow public ClientPlayNetworkHandler networkHandler;
    @Shadow private int ticksToNextAutojump;
    @Shadow private boolean falling;
    @Shadow private int underwaterVisibilityTicks;
    @Shadow private int field_3938;
    @Shadow private float mountJumpStrength;

    @Shadow private void updateNausea() { };
    @Shadow private boolean isWalking() { return false; };
    @Shadow public boolean shouldSlowDown() { return false; };
    @Shadow private void pushOutOfBlocks(double x, double y) { };
    @Shadow public boolean hasJumpingMount() { return false; };
    @Shadow public float getMountJumpStrength() { return 0; };
    @Shadow protected void startRidingJump() { };
    @Shadow protected boolean isCamera() { return false; };

    public KeepSprint(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void onTickMovement(CallbackInfo info) {
        int i;
        ItemStack itemStack;
        boolean bl6;
        boolean bl5;
        ++this.ticksSinceSprintingChanged;
        if (this.ticksLeftToDoubleTapSprint > 0) {
            --this.ticksLeftToDoubleTapSprint;
        }
        this.updateNausea();
        boolean bl = this.input.jumping;
        boolean bl2 = this.input.sneaking;
        boolean bl3 = this.isWalking();
        this.inSneakingPose = !this.getAbilities().flying && !this.isSwimming() && this.wouldPoseNotCollide(EntityPose.CROUCHING) && (this.isSneaking() || !this.isSleeping() && !this.wouldPoseNotCollide(EntityPose.STANDING));
        this.input.tick(this.shouldSlowDown());
        this.client.getTutorialManager().onMovement(this.input);
        if (this.isUsingItem() && !this.hasVehicle()) {
            this.input.movementSideways *= 0.2f;
            this.input.movementForward *= 0.2f;
            this.ticksLeftToDoubleTapSprint = 0;
        }
        boolean bl4 = false;
        if (this.ticksToNextAutojump > 0) {
            --this.ticksToNextAutojump;
            bl4 = true;
            this.input.jumping = true;
        }
        if (!this.noClip) {
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
        }
        if (bl2) {
            this.ticksLeftToDoubleTapSprint = 0;
        }
        float fl = (float)this.getHungerManager().getFoodLevel();           // inserted
        boolean bl7 = bl5 = fl > 0.0f || this.getAbilities().allowFlying;   // modified
        if (!(!this.onGround && !this.isSubmergedInWater() || bl2 || bl3 || !this.isWalking() || this.isSprinting() || !bl5 || this.isUsingItem() || this.hasStatusEffect(StatusEffects.BLINDNESS))) {
            if (this.ticksLeftToDoubleTapSprint > 0 || this.client.options.sprintKey.isPressed()) {
                this.setSprinting(true);
            } else {
                this.ticksLeftToDoubleTapSprint = 7;
            }
        }
        if (!this.isSprinting() && (!this.isTouchingWater() || this.isSubmergedInWater()) && this.isWalking() && bl5 && !this.isUsingItem() && !this.hasStatusEffect(StatusEffects.BLINDNESS) && this.client.options.sprintKey.isPressed()) {
            this.setSprinting(true);
        }
        if (this.isSprinting()) {
            boolean bl72;
            bl6 = !this.input.hasForwardMovement(); // modified
            boolean bl8 = bl72 = bl6 || this.horizontalCollision && !this.collidedSoftly || this.isTouchingWater() && !this.isSubmergedInWater();
            if (this.isSwimming()) {
                if (!this.onGround && !this.input.sneaking && bl6 || !this.isTouchingWater()) {
                    this.setSprinting(false);
                }
            } else if (bl72 || fl == 0) {   // modified
                this.setSprinting(false);
            }
        }
        bl6 = false;
        if (this.getAbilities().allowFlying) {
            if (this.client.interactionManager.isFlyingLocked()) {
                if (!this.getAbilities().flying) {
                    this.getAbilities().flying = true;
                    bl6 = true;
                    this.sendAbilitiesUpdate();
                }
            } else if (!bl && this.input.jumping && !bl4) {
                if (this.abilityResyncCountdown == 0) {
                    this.abilityResyncCountdown = 7;
                } else if (!this.isSwimming()) {
                    this.getAbilities().flying = !this.getAbilities().flying;
                    bl6 = true;
                    this.sendAbilitiesUpdate();
                    this.abilityResyncCountdown = 0;
                }
            }
        }
        if (this.input.jumping && !bl6 && !bl && !this.getAbilities().flying && !this.hasVehicle() && !this.isClimbing() && (itemStack = this.getEquippedStack(EquipmentSlot.CHEST)).isOf(Items.ELYTRA) && ElytraItem.isUsable(itemStack) && this.checkFallFlying()) {
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
        this.falling = this.isFallFlying();
        if (this.isTouchingWater() && this.input.sneaking && this.shouldSwimInFluids()) {
            this.knockDownwards();
        }
        if (this.isSubmergedIn(FluidTags.WATER)) {
            i = this.isSpectator() ? 10 : 1;
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks + i, 0, 600);
        } else if (this.underwaterVisibilityTicks > 0) {
            this.isSubmergedIn(FluidTags.WATER);
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks - 10, 0, 600);
        }
        if (this.getAbilities().flying && this.isCamera()) {
            i = 0;
            if (this.input.sneaking) {
                --i;
            }
            if (this.input.jumping) {
                ++i;
            }
            if (i != 0) {
                this.setVelocity(this.getVelocity().add(0.0, (float)i * this.getAbilities().getFlySpeed() * 3.0f, 0.0));
            }
        }
        if (this.hasJumpingMount()) {
            JumpingMount jumpingMount = (JumpingMount)((Object)this.getVehicle());
            if (this.field_3938 < 0) {
                ++this.field_3938;
                if (this.field_3938 == 0) {
                    this.mountJumpStrength = 0.0f;
                }
            }
            if (bl && !this.input.jumping) {
                this.field_3938 = -10;
                jumpingMount.setJumpStrength(MathHelper.floor(this.getMountJumpStrength() * 100.0f));
                this.startRidingJump();
            } else if (!bl && this.input.jumping) {
                this.field_3938 = 0;
                this.mountJumpStrength = 0.0f;
            } else if (bl) {
                ++this.field_3938;
                this.mountJumpStrength = this.field_3938 < 10 ? (float)this.field_3938 * 0.1f : 0.8f + 2.0f / (float)(this.field_3938 - 9) * 0.1f;
            }
        } else {
            this.mountJumpStrength = 0.0f;
        }
        super.tickMovement();
        if (this.onGround && this.getAbilities().flying && !this.client.interactionManager.isFlyingLocked()) {
            this.getAbilities().flying = false;
            this.sendAbilitiesUpdate();
        }
        
        info.cancel();
    }
}
