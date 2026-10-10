package com.sharinganmod.registry;

import com.sharinganmod.SharinganMod;
import com.sharinganmod.data.SharinganData;
import com.sharinganmod.logic.AmaterasuEffect;
import com.sharinganmod.logic.EyeItem;
import com.sharinganmod.logic.SharinganItem;
import com.sharinganmod.logic.UchihaEmblemItem;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModRegistries {
    private ModRegistries() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SharinganMod.MODID);
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, SharinganMod.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SharinganMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SharinganMod.MODID);

    public static final Supplier<AttachmentType<SharinganData>> SHARINGAN =
            ATTACHMENTS.register("sharingan",
                    () -> AttachmentType.serializable(() -> new SharinganData()).copyOnDeath().build());

    public static final DeferredHolder<MobEffect, AmaterasuEffect> AMATERASU =
            EFFECTS.register("amaterasu", () -> new AmaterasuEffect());

    public static final DeferredItem<EyeItem> EYE_ITACHI = ITEMS.register("eye_itachi",
            () -> new EyeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 1));
    public static final DeferredItem<EyeItem> EYE_SASUKE = ITEMS.register("eye_sasuke",
            () -> new EyeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 2));
    public static final DeferredItem<EyeItem> EYE_OBITO = ITEMS.register("eye_obito",
            () -> new EyeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 3));
    public static final DeferredItem<EyeItem> EYE_SHISUI = ITEMS.register("eye_shisui",
            () -> new EyeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 4));

    public static final DeferredItem<EyeItem> EYE_ETERNO = ITEMS.register("eye_eterno",
            () -> new EyeItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 5));

    public static final DeferredItem<UchihaEmblemItem> UCHIHA_EMBLEM = ITEMS.register("uchiha_emblem",
            () -> new UchihaEmblemItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredItem<SharinganItem> SHARINGAN_1 = ITEMS.register("sharingan_1",
            () -> new SharinganItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 1));
    public static final DeferredItem<SharinganItem> SHARINGAN_2 = ITEMS.register("sharingan_2",
            () -> new SharinganItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 2));
    public static final DeferredItem<SharinganItem> SHARINGAN_3 = ITEMS.register("sharingan_3",
            () -> new SharinganItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 3));

    public static final Supplier<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sharingan"))
                    .icon(() -> new ItemStack(EYE_ITACHI.get()))
                    .displayItems((params, out) -> {
                        out.accept(UCHIHA_EMBLEM.get());
                        out.accept(SHARINGAN_1.get());
                        out.accept(SHARINGAN_2.get());
                        out.accept(SHARINGAN_3.get());
                        out.accept(EYE_ITACHI.get());
                        out.accept(EYE_SASUKE.get());
                        out.accept(EYE_OBITO.get());
                        out.accept(EYE_SHISUI.get());
                        out.accept(EYE_ETERNO.get());
                    })
                    .build());

    public static Item sharinganItem(int tier) {
        return switch (tier) {
            case 1 -> SHARINGAN_1.get();
            case 2 -> SHARINGAN_2.get();
            default -> SHARINGAN_3.get();
        };
    }

    public static Item eyeFor(int type) {
        return switch (type) {
            case 1 -> EYE_ITACHI.get();
            case 2 -> EYE_SASUKE.get();
            case 3 -> EYE_OBITO.get();
            case 5 -> EYE_ETERNO.get();
            default -> EYE_SHISUI.get();
        };
    }
}
