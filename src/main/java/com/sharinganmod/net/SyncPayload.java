package com.sharinganmod.net;

import com.sharinganmod.SharinganMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncPayload(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SharinganMod.MODID, "sync"));
    public static final StreamCodec<ByteBuf, SyncPayload> CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(SyncPayload::new, SyncPayload::tag);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
