package com.sharinganmod.net;

import com.sharinganmod.SharinganMod;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Qual olho aparece no rosto de um jogador: 0 nenhum, 1-3 tomoe, 4 Itachi, 5 Sasuke, 6 Obito, 7 Shisui, 8 Eterno. */
public record FacePayload(UUID uuid, int code) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FacePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SharinganMod.MODID, "face"));
    public static final StreamCodec<ByteBuf, FacePayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, FacePayload::uuid,
            ByteBufCodecs.VAR_INT, FacePayload::code,
            FacePayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
