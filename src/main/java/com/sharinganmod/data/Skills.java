package com.sharinganmod.data;

/** Tabelas de nomes, recargas (ticks), custo de chakra e desgaste dos olhos por habilidade. */
public final class Skills {
    private Skills() {}

    public static final String[] TYPE_NAMES = {"Nenhum", "Itachi", "Sasuke", "Obito", "Shisui", "Eterno"};

    public static final String[][] NAMES = {
            {"", "", "", ""},
            {"Tsukuyomi", "Amaterasu", "Karasu Bunshin", "Susanoo"},
            {"Chidori Corrente", "Enton: Kagutsuchi", "Kirin", "Susanoo"},
            {"Kamui: Intangibilidade", "Kamui: Teletransporte", "Kamui: Sucção", "Susanoo"},
            {"Kotoamatsukami", "Shunshin", "Olhar Revelador", "Susanoo"},
            {"Tsukuyomi", "Amaterasu", "Kirin", "Susanoo Perfeito"}
    };

    /** Recarga base em ticks (20 ticks = 1 s). */
    public static final int[][] COOLDOWN = {
            {0, 0, 0, 0},
            {600, 400, 500, 0},
            {200, 450, 900, 0},
            {600, 300, 700, 0},
            {1200, 100, 500, 0},
            {600, 400, 900, 0}
    };

    /** Chakra gasto por uso (a barra vai de 0 a 400 no Mangekyou, 500 no Eterno). */
    public static final float[][] CHAKRA = {
            {0, 0, 0, 0},
            {80, 70, 50, 0},
            {40, 80, 120, 0},
            {60, 50, 90, 0},
            {100, 25, 50, 0},
            {80, 70, 120, 0}
    };

    /**
     * Desgaste ocular por uso (de 100 ate cegar, sem recuperacao).
     * Media proxima de 1,0: cerca de 100 habilidades ate a cegueira.
     */
    public static final float[][] STRAIN = {
            {0, 0, 0, 0},
            {1.5f, 1.2f, 0.6f, 0},
            {0.6f, 1.2f, 2.0f, 0},
            {0.8f, 0.5f, 1.5f, 0},
            {2.0f, 0.3f, 0.6f, 0},
            {0, 0, 0, 0}
    };

    public static final float SUSANOO_COST = 100f;
    /** Chakra extra gasto por tick enquanto o Susanoo esta ativo. */
    public static final float SUSANOO_DRAIN_PER_TICK = 0.12f;
    public static final float SUSANOO_STRAIN_ON_START = 0.5f;
    /** Desgaste passivo por tick com o Mangekyou ligado e com o Susanoo ativo. */
    public static final float MANGEKYOU_STRAIN_PER_TICK = 0.0003f;
    public static final float SUSANOO_STRAIN_PER_TICK = 0.003f;

    /** 0 os dois olhos, 1 olho esquerdo, 2 olho direito. */
    public static String sideName(int side) {
        return side == 1 ? "olho esquerdo" : side == 2 ? "olho direito" : "os dois olhos";
    }

    public static String stageName(SharinganData d) {
        if (d.stage <= 0) return "Sem Sharingan";
        String base;
        if (d.stage <= 3) base = "Sharingan " + d.stage + " Tomoe";
        else if (d.stage == 4) base = "Mangekyou Sharingan (" + TYPE_NAMES[d.type] + ")";
        else base = "Mangekyou Sharingan Eterno";
        return d.eyeSide != 0 ? base + " - só o " + sideName(d.eyeSide) : base;
    }
}
