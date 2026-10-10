package com.sharinganmod.logic;

import com.sharinganmod.SharinganMod;
import com.sharinganmod.data.SharinganData;
import com.sharinganmod.net.Net;
import com.sharinganmod.registry.ModRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = SharinganMod.MODID)
public final class ModEvents {
    private ModEvents() {}

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post e) {
        if (e.getEntity() instanceof ServerPlayer sp) SharinganLogic.tick(sp);
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post e) {
        SharinganLogic.charmTick(e.getServer());
        SharinganLogic.updateTickRate(e.getServer());
    }

    /** Kamui: intangibilidade bloqueia todo o dano. */
    @SubscribeEvent
    public static void incoming(LivingIncomingDamageEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp && SharinganData.get(sp).intangibleTicks > 0) {
            e.setCanceled(true);
        }
    }

    /** Despertar do Sharingan: sobreviver a um golpe quase fatal. */
    @SubscribeEvent
    public static void damaged(LivingDamageEvent.Post e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            SharinganData d = SharinganData.get(sp);
            float chance = d.uchiha ? SharinganData.AWAKEN_CHANCE_UCHIHA : SharinganData.AWAKEN_CHANCE_OTHER;
            // so quem pertence ao Cla Uchiha pode despertar o Sharingan
            if (d.uchiha && d.stage == 0 && !d.awakened && sp.getHealth() > 0 && sp.getHealth() <= 4f
                    && sp.getRandom().nextFloat() < chance) {
                SharinganLogic.awaken(sp, d);
            }
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayer sp)) return;
        SharinganData d = SharinganData.get(sp);
        LivingEntity victim = e.getEntity();
        if (d.stage == 3) {
            int type = SharinganLogic.mangekyouTypeFor(victim);
            if (type > 0) {
                SharinganLogic.awakenMangekyou(sp, d, type);
                return;
            }
        }
        if (d.eyesActive() && victim instanceof Enemy) SharinganLogic.addXp(sp, d, 10);
    }

    @SubscribeEvent
    public static void xp(PlayerXpEvent.PickupXp e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            SharinganData d = SharinganData.get(sp);
            if (d.eyesActive()) SharinganLogic.addXp(sp, d, e.getOrb().getValue());
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        SharinganData d = SharinganData.get(sp);
        if (!d.kit) {
            d.kit = true;
            SharinganLogic.give(sp, new ItemStack(ModRegistries.UCHIHA_EMBLEM.get()));
            SharinganLogic.chat(sp, "§cVocê recebeu o Emblema do Clã Uchiha. §7Clique direito com ele para "
                    + "pertencer ao clã: sem o clã, recargas, custo de chakra e desgaste dos olhos são maiores.");
        }
        SharinganLogic.forgetFace(sp);
        SharinganLogic.sendFacesTo(sp);
        Net.sync(sp);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) SharinganLogic.forgetFace(sp);
    }

    @SubscribeEvent
    public static void serverStopped(ServerStoppedEvent e) {
        SharinganLogic.clearAll();
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) SharinganLogic.onRespawn(sp);
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Net.sync(sp);
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        SharinganCommands.register(e.getDispatcher());
    }
}
