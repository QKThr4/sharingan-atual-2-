package com.sharinganmod.logic;

import com.sharinganmod.data.SharinganData;
import com.sharinganmod.data.Skills;
import com.sharinganmod.net.FacePayload;
import com.sharinganmod.net.Net;
import com.sharinganmod.registry.ModRegistries;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

/** Toda a logica de estado do Sharingan (servidor). */
public final class SharinganLogic {
    private SharinganLogic() {}

    /** Tick rate do jogo aplicado por nos (20 = normal). */
    private static float appliedRate = 20f;

    /** Mobs dominados pelo Kotoamatsukami: UUID -> ticks restantes. */
    public static final Map<UUID, Integer> CHARM = new HashMap<>();
    /** Ultimo olho enviado a todos os clientes para o rosto de cada jogador. */
    private static final Map<UUID, Integer> LAST_FACE = new HashMap<>();

    // ---------- utilitarios ----------
    public static void msg(ServerPlayer p, String text) {
        p.displayClientMessage(Component.literal(text), true);
    }

    public static void msg(ServerPlayer p, Component text) {
        p.displayClientMessage(text, true);
    }

    public static void chat(ServerPlayer p, String text) {
        p.sendSystemMessage(Component.literal(text));
    }

    public static void chat(ServerPlayer p, Component text) {
        p.sendSystemMessage(text);
    }

    /** Nome da tecla configurada pelo jogador (o cliente resolve o nome real). */
    public static MutableComponent key(String name) {
        return Component.keybind("key.sharingan." + name);
    }

    public static boolean isPet(Entity e, Entity owner) {
        return e instanceof TamableAnimal t && owner instanceof LivingEntity o && t.isOwnedBy(o);
    }

    public static void give(ServerPlayer p, ItemStack s) {
        if (!p.getInventory().add(s)) p.drop(s, false);
    }

    /** Coloca o item na mao principal se estiver vazia; senao no inventario (ou no chao se cheio). */
    public static void giveInHand(ServerPlayer p, ItemStack s) {
        if (p.getMainHandItem().isEmpty()) p.setItemInHand(InteractionHand.MAIN_HAND, s);
        else give(p, s);
    }

    static void glass(ServerPlayer p, float pitch) {
        p.level().playSound(null, p.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1f, pitch);
    }

    // ---------- progressao ----------
    public static void awaken(ServerPlayer p, SharinganData d) {
        d.stage = 1;
        d.xp = 0;
        d.awakened = true;
        d.setBoth();
        d.equipped = true;
        d.chakra = d.maxChakra();
        chat(p, Component.literal("§c§lSeu Sharingan despertou! §r§7(1 Tomoe) ")
                .append(key("sharingan")).append(Component.literal(" = ativar | "))
                .append(key("screen")).append(Component.literal(" = Aba Sharingan")));
        p.level().playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_STARE, SoundSource.PLAYERS, 1f, 0.7f);
        Net.sync(p);
    }

    public static void addXp(ServerPlayer p, SharinganData d, int amount) {
        if (d.stage < 1 || d.stage >= 3) return;
        d.xp += amount;
        while (d.stage < 3 && d.xp >= d.xpNeeded()) {
            d.xp -= d.xpNeeded();
            d.stage++;
            d.chakra = Math.min(d.maxChakra(), d.chakra + 100f);
            chat(p, "§c§lSeu Sharingan evoluiu para " + d.stage + " Tomoe!");
            p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 0.6f);
            if (d.sharinganOn) refreshEffects(p, d);
        }
        if (d.stage >= 3) d.xp = 0;
    }

    /** Retorna o tipo de Mangekyou (1-4) para o mob domesticado morto, ou 0. */
    /** Qualquer animal domesticado dá um Mangekyou aleatório (1 Itachi, 2 Sasuke, 3 Obito, 4 Shisui). */
    public static int mangekyouTypeFor(LivingEntity victim) {
        boolean tamed = (victim instanceof TamableAnimal t && t.isTame())
                || (victim instanceof net.minecraft.world.entity.animal.horse.AbstractHorse h && h.isTamed());
        return tamed ? 1 + victim.getRandom().nextInt(4) : 0;
    }

    public static void awakenMangekyou(ServerPlayer p, SharinganData d, int type) {
        d.stage = 4;
        d.type = type;
        d.mangekyouOn = false;
        d.chakra = Math.min(d.maxChakra(), d.chakra + 100f);
        chat(p, "§4§lA dor de perder quem você ama despertou o Mangekyou Sharingan de "
                + Skills.TYPE_NAMES[type] + "!");
        chat(p, Component.literal("§7")
                .append(key("mangekyou")).append(Component.literal(" = ativar Mangekyou | "))
                .append(key("skill1")).append(Component.literal(" "))
                .append(key("skill2")).append(Component.literal(" "))
                .append(key("skill3")).append(Component.literal(" "))
                .append(key("skill4")).append(Component.literal(" = habilidades | cuidado: cada uso desgasta "
                        + "seus olhos para sempre e a cegueira não tem cura.")));
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        p.level().playSound(null, p.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.8f, 1.4f);
        Net.sync(p);
    }

    /** Item que representa o Sharingan atual do jogador (vazio se ele nao tem nenhum). */
    private static ItemStack currentAsItem(SharinganData d) {
        if (d.stage <= 0) return ItemStack.EMPTY;
        ItemStack s;
        if (d.stage >= 5) s = new ItemStack(ModRegistries.EYE_ETERNO.get());
        else if (d.stage == 4) s = new ItemStack(ModRegistries.eyeFor(d.type));
        else return SharinganItem.make(d.stage, d.xp, d.eyeSide);
        EyeItem.setSide(s, d.eyeSide);
        return s;
    }

    /** Tira todo o Sharingan do jogador (volta a nao ter nenhum). */
    private static void clearCurrent(ServerPlayer p, SharinganData d) {
        deactivateAll(p, d);
        d.stage = 0;
        d.type = 0;
        d.xp = 0;
        d.setBoth();
        d.mangekyouOn = false;
    }

    /**
     * Fim do uso de um item de Sharingan: consome o item usado e devolve ao jogador o Sharingan que ele
     * tinha antes (na mao se o item acabou, senao no inventario). 'old' null = nao foi possivel usar.
     */
    public static InteractionResultHolder<ItemStack> finishUse(ServerPlayer sp, ItemStack stack, ItemStack old) {
        if (old == null) return InteractionResultHolder.fail(stack);
        stack.shrink(1);
        if (!old.isEmpty()) {
            if (stack.isEmpty()) return InteractionResultHolder.success(old);
            give(sp, old);
        }
        return InteractionResultHolder.success(stack);
    }

    /** Equipa um Sharingan de 1-3 tomoe com o XP guardado no item, substituindo o atual. side: 0 os dois olhos. */
    public static ItemStack equipTier(ServerPlayer p, int tier, int storedXp, int side) {
        return implant(p, tier, 0, storedXp, side);
    }

    /**
     * Implanta um olho (kind 1-4 = Mangekyou de Itachi/Sasuke/Obito/Shisui, 5 = Mangekyou Eterno).
     * O Mangekyou e o Eterno ja incluem o Sharingan de 3 tomoe, entao implantar um implanta o outro.
     * Substitui o Sharingan que o jogador tiver (o antigo volta como item).
     * Retorna null se nao foi possivel; senao o item que o jogador perdeu na troca (vazio se nao tinha).
     */
    public static ItemStack transplant(ServerPlayer p, int kind, ItemStack stack) {
        return implant(p, kind == 5 ? 5 : 4, kind, 0, EyeItem.side(stack));
    }

    /**
     * Implante generico. stage: 1-3 tomoe, 4 Mangekyou, 5 Eterno. type: tipo do Mangekyou (1-5) ou 0.
     * side: qual olho o item contem (0 os dois, 1 esquerdo, 2 direito).
     */
    private static ItemStack implant(ServerPlayer p, int stage, int type, int xp, int side) {
        SharinganData d = SharinganData.get(p);
        int itemType = stage >= 5 ? 5 : type;
        int curType = d.stage >= 5 ? 5 : d.type;

        if (d.stage == stage && curType == itemType) {
            // mesmo Sharingan: se o jogador tem so um olho e o item traz o outro, os dois se unem
            if (d.eyeSide != 0 && side != 0 && side == 3 - d.eyeSide) {
                d.setBoth();
                d.xp = Math.max(d.xp, stage >= 3 ? 0 : Math.min(xp, Math.max(0, d.xpNeeded() - 1)));
                chat(p, "\u00a75As duas metades se uniram: você tem os dois olhos de novo.");
                p.level().playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.6f);
                Net.sync(p);
                return ItemStack.EMPTY;
            }
            if (d.eyeSide == side) {
                msg(p, "Você já tem esse olho.");
                return null;
            }
        }

        // Itachi + Sasuke (os dois olhos inteiros) = Mangekyou Eterno
        if (d.stage == 4 && d.eyeSide == 0 && side == 0 && stage == 4
                && ((d.type == 1 && type == 2) || (d.type == 2 && type == 1))) {
            d.stage = 5;
            d.type = 5;
            d.mangekyouOn = false;
            d.chakra = Math.min(d.maxChakra(), d.chakra + 100f);
            if (d.susanooOn) endSusanoo(p, d);
            chat(p, "\u00a74\u00a7lOs olhos de Itachi e Sasuke se uniram: MANGEKYOU SHARINGAN ETERNO!");
            chat(p, d.blind
                    ? "\u00a77Com o Eterno a visão volta enquanto ele estiver nos seus olhos, e eles não sofrem mais desgaste."
                    : "\u00a77Seus olhos não sofrem mais desgaste.");
            p.level().playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.8f, 1.2f);
            Net.sync(p);
            return ItemStack.EMPTY;
        }

        ItemStack old = currentAsItem(d);
        clearCurrent(p, d);
        d.equipped = true;
        d.awakened = true;
        d.stage = stage;
        d.type = stage >= 5 ? 5 : (stage == 4 ? type : 0);
        d.xp = stage >= 3 ? 0 : Math.min(xp, Math.max(0, d.xpNeeded() - 1));
        d.setOnly(side);
        d.chakra = Math.min(d.chakra, d.maxChakra());
        String name = stage == 5 ? "Mangekyou Sharingan Eterno"
                : stage == 4 ? "Mangekyou Sharingan de " + Skills.TYPE_NAMES[type]
                : "Sharingan de " + stage + " Tomoe";
        chat(p, "\u00a7cVocê implantou o " + name + " (" + Skills.sideName(side) + ")"
                + (stage >= 4 ? ", que já inclui o Sharingan de 3 Tomoe." : ".")
                + (d.uchiha ? "" : " \u00a77Sem o Clã Uchiha: recargas e custo de chakra maiores.")
                + (old.isEmpty() ? "" : " \u00a77O Sharingan que você tinha voltou como item."));
        p.level().playSound(null, p.blockPosition(), stage >= 4 ? SoundEvents.WITHER_SPAWN : SoundEvents.ENDERMAN_STARE,
                SoundSource.PLAYERS, 0.6f, 1.4f);
        Net.sync(p);
        return old;
    }

    // ---------- acoes vindas do cliente ----------
    public static void handleAction(ServerPlayer p, int id) {
        SharinganData d = SharinganData.get(p);
        switch (id) {
            case 0 -> toggleSharingan(p, d);
            case 1 -> toggleMangekyou(p, d);
            case 2, 3, 4, 5 -> Abilities.use(p, d, id - 2);
            case 7 -> splitEye(p, d);
            case 8, 9 -> removeSharingan(p, d);
            default -> { }
        }
        Net.sync(p);
    }

    private static boolean enoughChakraToStart(ServerPlayer p, SharinganData d) {
        if (d.chakra < d.maxChakra() * 0.1f) {
            msg(p, "Chakra insuficiente (" + (int) d.chakra + "/" + (int) d.maxChakra() + "). Aguarde recuperar.");
            // apertar a tecla sem parar nao acelera a recuperacao: cada tentativa pausa a recuperacao por 3 s
            d.regenDelay = 60;
            return false;
        }
        return true;
    }

    private static void toggleSharingan(ServerPlayer p, SharinganData d) {
        if (d.stage < 1) { msg(p, "Você ainda não despertou o Sharingan."); return; }
        if (!d.equipped) {
            msg(p, Component.literal("O Sharingan não está equipado (abra a aba com ")
                    .append(key("screen")).append(Component.literal(").")));
            return;
        }
        if (d.sharinganOn) {
            deactivateAll(p, d);
            msg(p, "Sharingan desativado.");
            glass(p, 0.8f);
        } else {
            if (!enoughChakraToStart(p, d)) return;
            d.sharinganOn = true;
            refreshEffects(p, d);
            msg(p, "Sharingan ativado.");
            glass(p, 1.2f);
        }
    }

    private static void toggleMangekyou(ServerPlayer p, SharinganData d) {
        if (d.stage < 4) { msg(p, "Você precisa do Mangekyou Sharingan."); return; }
        if (!d.equipped) {
            msg(p, Component.literal("O Sharingan não está equipado (abra a aba com ")
                    .append(key("screen")).append(Component.literal(").")));
            return;
        }
        if (d.blind && d.stage < 5) { msg(p, "Seus olhos estão destruídos... só o Sharingan comum responde."); return; }
        if (d.mangekyouOn) {
            d.mangekyouOn = false;
            if (d.susanooOn) endSusanoo(p, d);
            refreshEffects(p, d);
            msg(p, "Mangekyou desativado.");
        } else {
            if (!enoughChakraToStart(p, d)) return;
            d.sharinganOn = true;
            d.mangekyouOn = true;
            refreshEffects(p, d);
            msg(p, "Mangekyou Sharingan ativado.");
            p.level().playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 0.5f);
        }
    }

    /**
     * Retira o Sharingan e entrega o item na mao, em qualquer estagio. O Mangekyou e o Eterno sao o 3 tomoe
     * evoluido: sai tudo junto num item so (o jogador fica sem Sharingan). 1/2/3 tomoe viram o item do
     * estagio, com o XP guardado.
     */
    private static void removeSharingan(ServerPlayer p, SharinganData d) {
        if (d.stage <= 0) { msg(p, "Você não tem Sharingan para retirar."); return; }
        int stage = d.stage;
        ItemStack out = currentAsItem(d);
        clearCurrent(p, d);
        giveInHand(p, out);
        glass(p, 0.8f);
        String what = stage <= 3 ? "Sharingan de " + stage + " Tomoe"
                : stage == 4 ? "Mangekyou Sharingan (junto com o 3 Tomoe)"
                : "Mangekyou Sharingan Eterno (junto com o 3 Tomoe)";
        chat(p, "\u00a7cVocê retirou o " + what + " e ele está na sua mão."
                + (stage >= 4 ? " \u00a77O desgaste dos seus olhos continua." : ""));
    }

    /**
     * Parte o olho ao meio, com qualquer Sharingan (1/2/3 tomoe, Mangekyou ou Eterno). O olho que sai e
     * sorteado (esquerdo ou direito) e vira um item na mao; o jogador fica so com o outro. Mangekyou/Eterno
     * levam o 3 tomoe junto. Com um olho so nao existe Susanoo.
     */
    private static void splitEye(ServerPlayer p, SharinganData d) {
        if (d.stage <= 0) { msg(p, "Você não tem Sharingan para partir."); return; }
        if (d.eyeSide != 0) { msg(p, "Você já tem só um olho."); return; }
        int removed = 1 + p.getRandom().nextInt(2);
        ItemStack out = currentAsItem(d);
        EyeItem.setSide(out, removed);
        d.setOnly(3 - removed);
        if (d.susanooOn) endSusanoo(p, d);
        giveInHand(p, out);
        glass(p, 1.0f);
        chat(p, "\u00a75Você partiu seu Sharingan: o " + Skills.sideName(removed) + " saiu como item (na sua mão) e "
                + "você ficou só com o " + Skills.sideName(d.eyeSide) + ". \u00a77Sem os dois olhos não há Susanoo. "
                + "Quem tiver o outro olho (do mesmo tipo) une os dois ao usar o item.");
    }

    // ---------- efeitos ----------
    public static void deactivateAll(ServerPlayer p, SharinganData d) {
        d.regenDelay = 60;
        d.sharinganOn = false;
        d.mangekyouOn = false;
        if (d.susanooOn) endSusanoo(p, d);
        removeEffects(p);
    }

    public static void refreshEffects(ServerPlayer p, SharinganData d) {
        removeEffects(p);
        int lvl = d.level();
        if (lvl <= 0) return;
        applyEffects(p, lvl);
    }

    /**
     * 1 tomoe: visao noturna. 2: + velocidade. 3: + forca. Mangekyou: + resistencia e forca II.
     * Eterno: resistencia II.
     */
    private static void applyEffects(ServerPlayer p, int lvl) {
        effect(p, MobEffects.NIGHT_VISION, 260, 0);
        if (lvl >= 2) effect(p, MobEffects.MOVEMENT_SPEED, 60, 0);
        if (lvl >= 3) effect(p, MobEffects.DAMAGE_BOOST, 60, lvl >= 4 ? 1 : 0);
        if (lvl >= 4) effect(p, MobEffects.DAMAGE_RESISTANCE, 60, lvl >= 5 ? 1 : 0);
    }

    static void effect(ServerPlayer p, Holder<MobEffect> eff, int ticks, int amp) {
        p.addEffect(new MobEffectInstance(eff, ticks, amp, true, false, false));
    }

    private static void removeOurs(ServerPlayer p, Holder<MobEffect> eff) {
        MobEffectInstance i = p.getEffect(eff);
        if (i != null && i.isAmbient() && !i.isVisible()) p.removeEffect(eff);
    }

    private static void removeEffects(ServerPlayer p) {
        removeOurs(p, MobEffects.NIGHT_VISION);
        removeOurs(p, MobEffects.MOVEMENT_SPEED);
        removeOurs(p, MobEffects.DAMAGE_BOOST);
        removeOurs(p, MobEffects.DAMAGE_RESISTANCE);
        removeOurs(p, MobEffects.ABSORPTION);
    }

    // ---------- desgaste e cegueira (permanentes) ----------
    public static void addStrain(ServerPlayer p, SharinganData d, float amount) {
        if (d.stage >= 5 || d.blind || amount <= 0) return;
        float old = d.strain;
        d.strain += amount * d.strainFactor();
        if (old < 60 && d.strain >= 60) msg(p, "§cSeus olhos começam a arder...");
        if (old < 85 && d.strain >= 85) msg(p, "§4§lVocê está prestes a perder a visão!");
        if (d.strain >= 100) {
            d.strain = 100;
            d.blind = true;
            d.mangekyouOn = false;
            if (d.susanooOn) endSusanoo(p, d);
            refreshEffects(p, d);
            chat(p, "§4§lO uso prolongado do Mangekyou destruiu sua visão. Você está cego para sempre.");
            chat(p, Component.literal("§7Com o Sharingan ativo (")
                    .append(key("sharingan")).append(Component.literal(") você ainda percebe silhuetas do mundo.")));
            p.level().playSound(null, p.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.8f, 0.8f);
        }
    }

    // ---------- Susanoo ----------
    public static void startSusanoo(ServerPlayer p, SharinganData d) {
        boolean perfect = d.stage >= 5;
        d.susanooOn = true;
        d.susanooTicks = perfect ? 1800 : 900;
        effect(p, MobEffects.ABSORPTION, d.susanooTicks, perfect ? 9 : 4);
        p.level().playSound(null, p.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.9f, 0.6f);
        msg(p, perfect ? "Susanoo Perfeito!" : "Susanoo!");
    }

    public static void endSusanoo(ServerPlayer p, SharinganData d) {
        d.susanooOn = false;
        d.susanooTicks = 0;
        d.cooldowns[3] = Math.max(d.cooldowns[3], 300);
        removeOurs(p, MobEffects.ABSORPTION);
        msg(p, "Susanoo dissipado.");
    }

    private static void susanooTick(ServerPlayer p, SharinganData d) {
        boolean perfect = d.stage >= 5;
        d.susanooTicks--;
        d.chakra -= Skills.SUSANOO_DRAIN_PER_TICK;
        addStrain(p, d, Skills.SUSANOO_STRAIN_PER_TICK);
        if (!d.susanooOn) return;
        if (p.tickCount % 20 == 0) {
            effect(p, MobEffects.DAMAGE_RESISTANCE, 60, 3);
            effect(p, MobEffects.DAMAGE_BOOST, 60, perfect ? 2 : 1);
        }
        if (p.tickCount % 10 == 0) {
            double rad = perfect ? 6 : 4;
            float dmg = perfect ? 10f : 6f;
            for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(rad),
                    x -> x != p && x.isAlive() && !isPet(x, p)
                            && (x instanceof Enemy || (x instanceof Mob m && m.getTarget() == p)))) {
                e.hurt(p.damageSources().playerAttack(p), dmg);
                e.knockback(0.8, p.getX() - e.getX(), p.getZ() - e.getZ());
            }
        }
        if (p.tickCount % 4 == 0) susanooShell(p, perfect);
        if (d.chakra <= 0f || d.susanooTicks <= 0 || !d.mangekyouOn) endSusanoo(p, d);
    }

    /** Armadura de chakra azul/roxa ao redor do jogador (visual simplificado do Susanoo). */
    private static void susanooShell(ServerPlayer p, boolean perfect) {
        ServerLevel l = p.serverLevel();
        double radius = perfect ? 2.6 : 1.8;
        double height = perfect ? 5.0 : 3.4;
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(perfect ? 0.35f : 0.45f, 0.4f, 1.0f), 1.6f);
        double[] hs = {0.1, height * 0.3, height * 0.55, height * 0.8};
        for (int i = 0; i < 10; i++) {
            double a = i * (Math.PI * 2 / 10) + p.tickCount * 0.1;
            for (double h : hs) {
                double r = radius * (0.6 + 0.4 * Math.sin(Math.PI * h / height));
                l.sendParticles(dust, p.getX() + Math.cos(a) * r, p.getY() + h, p.getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
            }
        }
    }

    // ---------- rosto (olhos visiveis em todos os clientes) ----------
    /** Baixo 4 bits: 0 nenhum, 1-3 tomoe, 4 Itachi, 5 Sasuke, 6 Obito, 7 Shisui, 8 Eterno. Bits altos: olho(s). */
    public static int faceCode(SharinganData d) {
        if (d.stage <= 0 || !d.eyesActive()) return 0;
        int base;
        if (d.level() >= 4) base = d.stage >= 5 ? 8 : 3 + Math.max(1, Math.min(4, d.type));
        else base = Math.min(3, d.stage);
        // bits altos: quais olhos aparecem (0 os dois, 1 so o esquerdo, 2 so o direito)
        return base | (d.eyeSide << 4);
    }

    private static void updateFace(ServerPlayer p, SharinganData d) {
        int code = faceCode(d);
        int last = LAST_FACE.getOrDefault(p.getUUID(), 0);
        if (code != last) {
            LAST_FACE.put(p.getUUID(), code);
            PacketDistributor.sendToAllPlayers(new FacePayload(p.getUUID(), code));
        }
    }

    /** Ao entrar, o jogador recebe o rosto de quem ja esta com o Sharingan ativo. */
    public static void sendFacesTo(ServerPlayer joiner) {
        for (Map.Entry<UUID, Integer> en : LAST_FACE.entrySet()) {
            if (en.getValue() > 0 && !en.getKey().equals(joiner.getUUID())) {
                PacketDistributor.sendToPlayer(joiner, new FacePayload(en.getKey(), en.getValue()));
            }
        }
    }

    public static void forgetFace(ServerPlayer p) {
        LAST_FACE.remove(p.getUUID());
    }

    public static void clearAll() {
        LAST_FACE.clear();
        CHARM.clear();
        appliedRate = 20f;
    }

    // ---------- tick do jogador ----------
    public static void tick(ServerPlayer p) {
        SharinganData d = SharinganData.get(p);
        updateFace(p, d);
        if (d.stage > 0) d.awakened = true;
        blindEffect(p, d);
        if (d.stage <= 0) return;

        for (int i = 0; i < 4; i++) if (d.cooldowns[i] > 0) d.cooldowns[i]--;

        // chakra: gasta com o Sharingan ativo, recupera com ele desligado
        float max = d.maxChakra();
        if (d.chakra > max) d.chakra = max;
        if (d.eyesActive()) {
            // com o jogo mais lento cada tick dura mais: compensa para o tempo real continuar o mesmo
            d.chakra -= d.drainPerTick() * (20f / appliedRate);
            if (d.chakra <= 0f) {
                d.chakra = 0f;
                deactivateAll(p, d);
                msg(p, "§cSeu chakra acabou: o Sharingan se desativou.");
                glass(p, 0.7f);
            }
        } else if (d.regenDelay > 0) {
            d.regenDelay--;
        } else if (d.chakra < max) {
            d.chakra = Math.min(max, d.chakra + max / (SharinganData.REGEN_SECONDS * 20f));
        }
        lowChakraEffects(p, d);
        if (d.eyesActive() && p.tickCount % 5 == 0) purgeBadEffects(p, d);

        int lvl = d.level();
        if (lvl > 0) {
            if (p.tickCount % 20 == 0) {
                applyEffects(p, lvl);
                addXp(p, d, 1);
            }
        }

        if (d.mangekyouOn) addStrain(p, d, Skills.MANGEKYOU_STRAIN_PER_TICK);
        if (d.susanooOn && d.eyeSide != 0) endSusanoo(p, d);
        if (d.susanooOn) susanooTick(p, d);

        if (d.intangibleTicks > 0) {
            d.intangibleTicks--;
            if (p.tickCount % 2 == 0) {
                p.serverLevel().sendParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 6, 0.4, 0.8, 0.4, 0.1);
            }
        }

        if (d.suctionTicks > 0) suctionTick(p, d);

        if (p.tickCount % 10 == 0) Net.sync(p);
    }

    /** Cegueira permanente: efeito de cegueira constante, exceto com o Eterno nos olhos ou com o Sharingan ativo. */
    private static void blindEffect(ServerPlayer p, SharinganData d) {
        if (!d.blind || p.tickCount % 20 != 0) return;
        if (d.stage >= 5 || d.eyesActive()) {
            removeOurs(p, MobEffects.BLINDNESS);
        } else {
            p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, false, true));
        }
    }

    /**
     * Efeitos colaterais de chakra baixo, cumulativos: ate 40 fome, ate 30 lentidao, ate 20 nausea,
     * ate 10 fraqueza.
     */
    private static void lowChakraEffects(ServerPlayer p, SharinganData d) {
        if (p.tickCount % 20 != 0) return;
        float c = d.chakra;
        if (c <= 40f) lowEffect(p, MobEffects.HUNGER);
        if (c <= 30f) lowEffect(p, MobEffects.MOVEMENT_SLOWDOWN);
        if (c <= 20f) lowEffect(p, MobEffects.CONFUSION);
        if (c <= 10f) lowEffect(p, MobEffects.WEAKNESS);
    }

    private static void lowEffect(ServerPlayer p, Holder<MobEffect> eff) {
        p.addEffect(new MobEffectInstance(eff, 100, 0, true, false, true));
    }

    /** Com o Sharingan ativo: remove nausea, escuridao e cegueira (a nausea do chakra baixo continua). */
    private static void purgeBadEffects(ServerPlayer p, SharinganData d) {
        if (p.hasEffect(MobEffects.DARKNESS)) p.removeEffect(MobEffects.DARKNESS);
        if (p.hasEffect(MobEffects.BLINDNESS)) p.removeEffect(MobEffects.BLINDNESS);
        if (d.chakra > 20f && p.hasEffect(MobEffects.CONFUSION)) p.removeEffect(MobEffects.CONFUSION);
    }

    /**
     * Diminuir os ticks: enquanto algum jogador estiver com o Sharingan ativo, o jogo inteiro roda a
     * (20 - N) ticks por segundo, exatamente como "/tick rate". Com varios jogadores vale o maior N.
     * Ao desligar, volta a 20.
     */
    public static void updateTickRate(MinecraftServer server) {
        int slow = 0;
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            if (!sp.isAlive()) continue;
            int lvl = SharinganData.get(sp).level();
            if (lvl > 0) slow = Math.max(slow, SharinganData.SLOW_TICKS[lvl]);
        }
        float want = 20f - slow;
        if (want != appliedRate) {
            appliedRate = want;
            server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack().withSuppressedOutput(), "tick rate " + (int) want);
        }
    }

    /** Tick rate atual aplicado pelo mod (usado pelo HUD no servidor integrado). */
    public static float appliedRate() {
        return appliedRate;
    }

    private static void suctionTick(ServerPlayer p, SharinganData d) {
        d.suctionTicks--;
        // ponto de sucção bem a frente do jogador: um creeper puxado nao chega perto o bastante para explodir
        Vec3 c = p.getEyePosition().add(p.getLookAngle().scale(6.5));
        for (Entity e : p.level().getEntities(p, p.getBoundingBox().inflate(14))) {
            if (!(e instanceof LivingEntity le) || isPet(e, p)) continue;
            Vec3 pull = c.subtract(e.position().add(0, e.getBbHeight() / 2, 0));
            double dist = pull.length();
            if (dist < 0.5) continue;
            e.setDeltaMovement(e.getDeltaMovement().add(pull.normalize().scale(0.35)));
            e.hurtMarked = true;
            if (dist < 3.5 && p.tickCount % 10 == 0) le.hurt(p.damageSources().indirectMagic(p, p), 3f);
        }
        p.serverLevel().sendParticles(ParticleTypes.PORTAL, c.x, c.y, c.z, 25, 1.2, 1.2, 1.2, 0.3);
    }

    // ---------- Kotoamatsukami (controle de mobs) ----------
    public static void charmTick(MinecraftServer server) {
        if (CHARM.isEmpty() || server.getTickCount() % 10 != 0) return;
        Iterator<Map.Entry<UUID, Integer>> it = CHARM.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> en = it.next();
            en.setValue(en.getValue() - 10);
            Entity ent = null;
            for (ServerLevel l : server.getAllLevels()) {
                ent = l.getEntity(en.getKey());
                if (ent != null) break;
            }
            if (!(ent instanceof Mob m) || !m.isAlive() || en.getValue() <= 0) {
                it.remove();
                continue;
            }
            Monster target = m.level().getEntitiesOfClass(Monster.class, m.getBoundingBox().inflate(16),
                            x -> x != m && x.isAlive() && !CHARM.containsKey(x.getUUID()))
                    .stream().min(Comparator.comparingDouble((Monster x) -> m.distanceToSqr(x))).orElse(null);
            m.setTarget(target);
        }
    }

    public static void onRespawn(ServerPlayer p) {
        SharinganData d = SharinganData.get(p);
        d.susanooOn = false;
        d.susanooTicks = 0;
        d.intangibleTicks = 0;
        d.suctionTicks = 0;
        Net.sync(p);
    }
}
