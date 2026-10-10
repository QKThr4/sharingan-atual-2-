package com.sharinganmod.net;

import com.sharinganmod.SharinganMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0 sharingan, 1 mangekyou, 2-5 habilidades 1-4, 7 partilhar Obito, 8 equipar, 9 extrair olho. */
public record ActionPayload(int action) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ActionPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SharinganMod.MODID, "action"));
    public static final StreamCodec<ByteBuf, ActionPayload> CODEC =
            ByteBufCodecs.VAR_INT.map(ActionPayload::new, ActionPayload::action);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
