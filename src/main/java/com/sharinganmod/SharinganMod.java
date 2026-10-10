package com.sharinganmod;

import com.sharinganmod.registry.ModRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(SharinganMod.MODID)
public class SharinganMod {
    public static final String MODID = "sharingan";

    public SharinganMod(IEventBus modBus) {
        ModRegistries.ATTACHMENTS.register(modBus);
        ModRegistries.EFFECTS.register(modBus);
        ModRegistries.ITEMS.register(modBus);
        ModRegistries.TABS.register(modBus);
    }
}
