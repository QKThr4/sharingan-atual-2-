package com.sharinganmod.data;

import com.sharinganmod.registry.ModRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** Estado do Sharingan de um jogador (salvo no jogador e copiado ao morrer). */
public class SharinganData implements INBTSerializable<CompoundTag> {
    /** "Ticks" que o Sharingan tira dos inimigos por nivel (indice = nivel 0..5). */
    public static final int[] SLOW_TICKS = {0, 2, 3, 4, 5, 6};
    /** XP para sair de 1 para 2 tomoe e de 2 para 3 tomoe. */
    public static final int XP_TO_2 = 150;
    public static final int XP_TO_3 = 250;
    /** Tempo para encher a barra do zero com o Sharingan desligado. */
    public static final float REGEN_SECONDS = 90f;
    /** Chance de despertar o Sharingan ao sobreviver a um golpe quase fatal. So quem e do Cla Uchiha desperta. */
    public static final float AWAKEN_CHANCE_UCHIHA = 0.30f;
    public static final float AWAKEN_CHANCE_OTHER = 0.0f;

    /** 0 nada, 1-3 tomoe, 4 mangekyou, 5 mangekyou eterno */
    public int stage;
    /** 0 nenhum, 1 Itachi, 2 Sasuke, 3 Obito, 4 Shisui, 5 Eterno */
    public int type;
    public int xp;
    public boolean equipped = true;
    public boolean sharinganOn;
    public boolean mangekyouOn;
    public boolean susanooOn;
    /** Cegueira permanente: nao ha como desfazer. */
    public boolean blind;
    /** Tem so um olho (partido): halfEye == (eyeSide != 0). */
    public boolean halfEye;
    public boolean gaveEye;
    /** Quais olhos o jogador tem: 0 os dois, 1 so o esquerdo, 2 so o direito. */
    public int eyeSide;
    /** Ja teve um Sharingan neste mundo: so pode conseguir outro por implante. */
    public boolean awakened;
    /** Ticks sem recuperar chakra (evita recuperar apertando a tecla sem parar). Nao e salvo. */
    public int regenDelay;
    /** Desgaste ocular 0..100 (permanente, nunca diminui). */
    public float strain;
    public float chakra;
    public boolean uchiha;
    /** Ja recebeu o Emblema do Cla Uchiha no primeiro login do mundo. */
    public boolean kit;
    public int susanooTicks;
    public int intangibleTicks;
    public int suctionTicks;
    public int[] cooldowns = new int[4];

    public static SharinganData get(Player p) {
        return p.getData(ModRegistries.SHARINGAN);
    }

    /** Nivel efetivo dos poderes (0 = desligado). */
    public int level() {
        if (!sharinganOn || !equipped || stage <= 0) return 0;
        if (stage <= 3) return stage;
        return mangekyouOn ? stage : 3;
    }

    public boolean eyesActive() {
        return sharinganOn && equipped && stage > 0;
    }

    public void setBoth() {
        eyeSide = 0;
        halfEye = false;
        gaveEye = false;
    }

    /** side: 1 = so o olho esquerdo, 2 = so o direito. */
    public void setOnly(int side) {
        eyeSide = side;
        halfEye = side != 0;
        gaveEye = halfEye;
    }

    /** Chakra gasto por segundo com o Sharingan ligado, por estagio (0..5): 1 tomoe 2, 2 tomoe 1,5, 3 tomoe 1, Mangekyou e Eterno 2. */
    public static final float[] DRAIN_PER_SECOND = {0f, 2f, 1.5f, 1f, 2f, 2f};
    /** Duracao com a barra cheia, em segundos, por estagio: 2, 4, 6, 8 e 10 minutos. */
    public static final int[] FULL_SECONDS = {0, 120, 240, 360, 480, 600};

    /** Chakra gasto por tick (sem compensar o tick rate do jogo). */
    public float drainPerTick() {
        return DRAIN_PER_SECOND[Math.max(0, Math.min(5, stage))] / 20f;
    }

    /** Segundos de Sharingan com a barra cheia. */
    public float fullSeconds() {
        return FULL_SECONDS[Math.max(0, Math.min(5, stage))];
    }

    public int xpNeeded() {
        return stage == 1 ? XP_TO_2 : stage == 2 ? XP_TO_3 : 0;
    }

    public float maxChakra() {
        if (stage <= 0) return 0f;
        return DRAIN_PER_SECOND[Math.min(5, stage)] * FULL_SECONDS[Math.min(5, stage)];
    }

    /** Multiplicador de recarga e de custo de chakra: quem nao e do cla Uchiha (ex.: Sharingan dado por admin) paga 50% a mais. */
    public float penalty() {
        return uchiha ? 1.0f : 1.5f;
    }

    /** Multiplicador do desgaste ocular: +25% sem o cla, -50% com meio olho (Obito). */
    public float strainFactor() {
        return (uchiha ? 1.0f : 1.25f) * (halfEye ? 0.5f : 1.0f);
    }

    /** Segundos de Sharingan restantes com o chakra atual. */
    public int secondsLeft() {
        return (int) (chakra / drainPerTick() / 20f);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag t = new CompoundTag();
        t.putInt("stage", stage);
        t.putInt("type", type);
        t.putInt("xp", xp);
        t.putBoolean("equipped", equipped);
        t.putBoolean("on", sharinganOn);
        t.putBoolean("mk", mangekyouOn);
        t.putBoolean("sus", susanooOn);
        t.putBoolean("blind", blind);
        t.putBoolean("half", halfEye);
        t.putInt("side", eyeSide);
        t.putBoolean("awk", awakened);
        t.putBoolean("gave", gaveEye);
        t.putFloat("strain", strain);
        t.putFloat("chakra", chakra);
        t.putBoolean("uchiha", uchiha);
        t.putBoolean("kit", kit);
        t.putInt("susT", susanooTicks);
        t.putInt("intT", intangibleTicks);
        t.putInt("sucT", suctionTicks);
        t.putIntArray("cd", cooldowns);
        return t;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag t) {
        stage = t.getInt("stage");
        type = t.getInt("type");
        xp = t.getInt("xp");
        equipped = !t.contains("equipped") || t.getBoolean("equipped");
        sharinganOn = t.getBoolean("on");
        mangekyouOn = t.getBoolean("mk");
        susanooOn = t.getBoolean("sus");
        blind = t.getBoolean("blind");
        halfEye = t.getBoolean("half");
        eyeSide = t.getInt("side");
        if (halfEye && eyeSide == 0) eyeSide = 1;
        halfEye = eyeSide != 0;
        awakened = t.getBoolean("awk") || stage > 0;
        gaveEye = t.getBoolean("gave");
        strain = t.getFloat("strain");
        chakra = t.getFloat("chakra");
        uchiha = t.getBoolean("uchiha");
        kit = t.getBoolean("kit");
        susanooTicks = t.getInt("susT");
        intangibleTicks = t.getInt("intT");
        suctionTicks = t.getInt("sucT");
        int[] a = t.getIntArray("cd");
        cooldowns = new int[4];
        for (int i = 0; i < 4 && i < a.length; i++) cooldowns[i] = a[i];
    }
}
