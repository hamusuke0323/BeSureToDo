package com.hamusuke.criticalib.client;

import com.hamusuke.criticalib.invoker.LivingEntityInvoker;
import com.hamusuke.criticalib.network.SyncCritFlagPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class CriticaLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncCritFlagPacket.PACKET, (client, handler, buf, responseSender) -> {
            SyncCritFlagPacket packet = new SyncCritFlagPacket(buf);
            if (client.world != null && client.world.getEntityById(packet.getEntityId()) instanceof LivingEntityInvoker invoker) {
                invoker.setCritical(packet.getFlag());
            }
        });
    }
}
