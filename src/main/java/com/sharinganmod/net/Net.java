package com.sharinganmod.net;

import com.sharinganmod.SharinganMod;
import com.sharinganmod.client.ClientFace;
import com.sharinganmod.data.SharinganData;
import com.sharinganmod.logic.SharinganLogic;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SharinganMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class Net {
    private Net() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar("2");
        reg.playToClient(SyncPayload.TYPE, SyncPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientNet.handleSync(payload)));
        reg.playToClient(FacePayload.TYPE, FacePayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientFace.set(payload.uuid(), payload.code())));
        reg.playToServer(ActionPayload.TYPE, ActionPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer sp) {
                        SharinganLogic.handleAction(sp, payload.action());
                    }
                }));
    }

    public static void sync(ServerPlayer sp) {
        SharinganData d = SharinganData.get(sp);
        PacketDistributor.sendToPlayer(sp, new SyncPayload(d.serializeNBT(sp.level().registryAccess())));
    }
}
