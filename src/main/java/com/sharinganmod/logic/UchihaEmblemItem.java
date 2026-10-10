package com.sharinganmod.logic;

import com.sharinganmod.data.SharinganData;
import com.sharinganmod.net.Net;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Emblema do Cla Uchiha: entregue no primeiro login do mundo. Clique direito para pertencer ao cla. */
public class UchihaEmblemItem extends Item {
    public UchihaEmblemItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.fail(stack);
        SharinganData d = SharinganData.get(sp);
        if (d.uchiha) {
            SharinganLogic.msg(sp, "Você já pertence ao Clã Uchiha.");
            return InteractionResultHolder.fail(stack);
        }
        d.uchiha = true;
        stack.shrink(1);
        SharinganLogic.chat(sp, "§c§lO sangue do Clã Uchiha desperta em você. §r§7"
                + "Agora você pode despertar o Sharingan (sobreviva a um golpe quase fatal).");
        level.playSound(null, sp.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1f, 0.6f);
        Net.sync(sp);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("Clique direito para pertencer ao Clã Uchiha.").withStyle(ChatFormatting.GRAY));
        tip.add(Component.literal("Só quem é do Clã Uchiha pode despertar o Sharingan.")
                .withStyle(ChatFormatting.DARK_RED));
    }
}
