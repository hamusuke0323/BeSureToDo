package com.hamusuke.criticalib.mixin;

import com.hamusuke.criticalib.invoker.LivingEntityInvoker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityInvoker {
    private static final TrackedData<Boolean> CRITICAL = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(CRITICAL, false);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void damageFirst(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source instanceof ProjectileDamageSource && source.getAttacker() instanceof LivingEntityInvoker livingEntityInvoker && source.getSource() instanceof PersistentProjectileEntity projectile) {
            livingEntityInvoker.setCritical(projectile.isCritical());
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void damage$Return(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getAttacker() instanceof LivingEntityInvoker livingEntityInvoker) {
            livingEntityInvoker.setCritical(false);
        }
    }

    @Override
    public void setCritical(boolean flag) {
        this.dataTracker.set(CRITICAL, flag);
    }

    @Override
    public boolean isCritical() {
        return this.dataTracker.get(CRITICAL);
    }
}
