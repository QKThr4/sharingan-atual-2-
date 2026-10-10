package com.sharinganmod.client;

import com.sharinganmod.SharinganMod;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = SharinganMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(ModKeys.SCREEN);
        e.register(ModKeys.SHARINGAN);
        e.register(ModKeys.MANGEKYOU);
        e.register(ModKeys.SKILL1);
        e.register(ModKeys.SKILL2);
        e.register(ModKeys.SKILL3);
        e.register(ModKeys.SKILL4);
    }

    @SubscribeEvent
    public static void guiLayers(RegisterGuiLayersEvent e) {
        e.registerBelow(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(SharinganMod.MODID, "vision"), ClientOverlay::renderVision);
        e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(SharinganMod.MODID, "hud"), ClientOverlay::renderHud);
    }

    /** Coloca os olhos no rosto dos dois modelos de jogador (wide e slim). */
    @SubscribeEvent
    public static void playerLayers(EntityRenderersEvent.AddLayers e) {
        for (var skin : e.getSkins()) {
            var renderer = e.getSkin(skin);
            if (renderer instanceof PlayerRenderer pr) {
                pr.addLayer(new FaceLayer(pr));
            }
        }
    }
}
