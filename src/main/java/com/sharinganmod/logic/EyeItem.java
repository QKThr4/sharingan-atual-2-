package com.sharinganmod.logic;

import com.sharinganmod.data.Skills;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * Olho de Mangekyou (ja inclui o Sharingan de 3 tomoe). kind: 1 Itachi, 2 Sasuke, 3 Obito, 4 Shisui,
 * 5 Mangekyou Eterno. Clique direito para implantar; substitui o Sharingan que o jogador tiver.
 * O item pode conter os dois olhos (padrao) ou so um (olho esquerdo/direito, depois de partido).
 */
public class EyeItem extends Item {
    public final int kind;

    public EyeItem(Properties props, int kind) {
        super(props);
        this.kind = kind;
    }

    /** Qual olho o item contem: 0 os dois, 1 esquerdo, 2 direito. */
    public static int side(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd == null ? 0 : cd.copyTag().getInt("side");
    }

    public static void setSide(ItemStack stack, int side) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag t = cd == null ? new CompoundTag() : cd.copyTag();
        t.putInt("side", side);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    @Override
    public Component getName(ItemStack stack) {
        String base = kind == 5 ? "Mangekyou Sharingan Eterno" : "Mangekyou Sharingan de " + Skills.TYPE_NAMES[kind];
        return Component.literal(base + " (" + Skills.sideName(side(stack)) + ")");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.fail(stack);
        ItemStack old = SharinganLogic.transplant(sp, kind, stack);
        return SharinganLogic.finishUse(sp, stack, old);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        int side = side(stack);
        tip.add(Component.literal("Contém: " + Skills.sideName(side) + " (já inclui o 3 Tomoe).")
                .withStyle(kind == 3 ? ChatFormatting.DARK_PURPLE : ChatFormatting.DARK_RED));
        if (side != 0) {
            tip.add(Component.literal("Quem tiver o outro olho (do mesmo tipo) une os dois.")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tip.add(Component.literal("Com um olho só não existe Susanoo.").withStyle(ChatFormatting.DARK_PURPLE));
        } else if (kind == 1 || kind == 2) {
            tip.add(Component.literal("Junto do Mangekyou de " + Skills.TYPE_NAMES[kind == 1 ? 2 : 1]
                    + " forma o Mangekyou Eterno.").withStyle(ChatFormatting.DARK_RED));
        }
        tip.add(Component.literal("Clique direito para implantar: substitui o Sharingan que você tiver")
                .withStyle(ChatFormatting.GRAY));
        tip.add(Component.literal("(o antigo volta como item). Sem o Clã Uchiha: recargas e custos maiores.")
                .withStyle(ChatFormatting.GRAY));
    }
}
