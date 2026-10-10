package com.sharinganmod.client;

import com.sharinganmod.SharinganMod;
import com.sharinganmod.net.ActionPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = SharinganMod.MODID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        while (ModKeys.SCREEN.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new SharinganScreen());
        }
        poll(ModKeys.SHARINGAN, 0);
        poll(ModKeys.MANGEKYOU, 1);
        poll(ModKeys.SKILL1, 2);
        poll(ModKeys.SKILL2, 3);
        poll(ModKeys.SKILL3, 4);
        poll(ModKeys.SKILL4, 5);
    }

    private static void poll(KeyMapping key, int action) {
        while (key.consumeClick()) {
            PacketDistributor.sendToServer(new ActionPayload(action));
        }
    }

    /** Ao sair do mundo, limpa o estado do cliente para nao vazar para o proximo mundo. */
    @SubscribeEvent
    public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientFace.clear();
        ClientData.DATA.deserializeNBT(RegistryAccess.EMPTY, new CompoundTag());
    }
}
