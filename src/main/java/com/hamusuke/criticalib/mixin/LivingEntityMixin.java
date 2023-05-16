package com.hamusuke.criticalib.mixin;

import com.hamusuke.criticalib.invoker.LivingEntityInvoker;
import com.hamusuke.criticalib.network.SyncCritFlagPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityInvoker {
    private boolean crit;

    LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
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
        this.crit = flag;

        if (!this.world.isClient) {
            PacketByteBuf byteBuf = new SyncCritFlagPacket(this.getId(), this.crit).write(PacketByteBufs.create());
            this.world.getServer().getPlayerManager().sendToAll(new CustomPayloadS2CPacket(SyncCritFlagPacket.PACKET, byteBuf));
        }
    }

    @Override
    public boolean isCritical() {
        return this.crit;
    }
}
