package com.sharinganmod.client;

import com.sharinganmod.data.SharinganData;
import com.sharinganmod.data.Skills;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ClientOverlay {
    private ClientOverlay() {}

    /** Tela de visao: tinta do Sharingan, desgaste e cegueira. */
    public static void renderVision(GuiGraphics gg, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        SharinganData d = ClientData.DATA;
        int w = gg.guiWidth();
        int h = gg.guiHeight();

        if (d.eyesActive()) gg.fill(0, 0, w, h, 0x18FF0000);
        if (d.susanooOn) gg.fill(0, 0, w, h, 0x206040FF);

        if (d.blind && d.stage < 5) {
            // Cego para sempre: escuridao total. Com o Sharingan ativo, restam so silhuetas escuras do mundo.
            int alpha = d.eyesActive() ? 0xD2 : 0xFF;
            gg.fill(0, 0, w, h, alpha << 24);
        } else if (d.strain > 60) {
            int a = (int) ((d.strain - 60) / 40f * 0x90);
            gg.fill(0, 0, w, h, (a << 24) | 0x300000);
        }
    }

    private static String time(int seconds) {
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    private static void bar(GuiGraphics gg, int x, int y, int w, int h, float frac, int color) {
        gg.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        gg.fill(x, y, x + w, y + h, 0xFF202028);
        int fw = (int) (w * Math.max(0f, Math.min(1f, frac)));
        gg.fill(x, y, x + fw, y + h, color);
    }

    /** HUD: bloco de status (chakra etc.) e bloco de habilidades, cada um na posicao escolhida no layout (aba J). */
    public static void renderHud(GuiGraphics gg, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        SharinganData d = ClientData.DATA;
        if (d.stage <= 0) return;
        int sw = gg.guiWidth();
        int sh = gg.guiHeight();
        drawStatus(gg, mc.font, d,
                ClientLayout.place(ClientLayout.statusX, sw, ClientLayout.STATUS_W),
                ClientLayout.place(ClientLayout.statusY, sh, ClientLayout.STATUS_H));
        if (d.stage >= 4) {
            drawSkills(gg, mc.font, d,
                    ClientLayout.place(ClientLayout.skillsX, sw, ClientLayout.SKILLS_W),
                    ClientLayout.place(ClientLayout.skillsY, sh, ClientLayout.SKILLS_H));
        }
    }

    private static void drawStatus(GuiGraphics gg, Font f, SharinganData d, int x, int y) {
        int barW = 110;

        String title = Skills.stageName(d) + (d.eyesActive() ? " [ATIVO]" : " [desligado]")
                + (d.blind && d.stage < 5 ? " - CEGO" : "");
        gg.drawString(f, title, x, y, d.eyesActive() ? 0xFF4444 : 0x999999);
        y += 11;

        // chakra
        float max = Math.max(1f, d.maxChakra());
        bar(gg, x, y, barW, 7, d.chakra / max, d.eyesActive() ? 0xFF3A7BFF : 0xFF2A5FCC);
        String chakraText = "Chakra " + (int) d.chakra + "/" + (int) max
                + (d.eyesActive() ? "  (" + time(d.secondsLeft()) + ")" : "");
        gg.drawString(f, chakraText, x + barW + 6, y, d.chakra <= 40f ? 0xFF8844 : 0x66AAFF);
        y += 11;

        if (d.eyesActive()) {
            int n = SharinganData.SLOW_TICKS[d.level()];
            gg.drawString(f, "Ticks do jogo: " + (20 - n) + "/s (-" + n + ")", x, y, 0x88DDFF);
            y += 10;
        }

        if (d.stage < 3) {
            bar(gg, x, y, barW, 4, d.xp / (float) Math.max(1, d.xpNeeded()), 0xFFDD2222);
            gg.drawString(f, "XP " + d.xp + "/" + d.xpNeeded(), x + barW + 6, y - 2, 0xFF8888);
        } else if (d.stage == 4) {
            bar(gg, x, y, barW, 5, d.strain / 100f, 0xFFCC1111);
            gg.drawString(f, "Desgaste ocular " + (int) d.strain + "% (permanente)", x + barW + 6, y - 2, 0xFF8888);
        }
    }

    private static void drawSkills(GuiGraphics gg, Font f, SharinganData d, int x, int y) {
        for (int i = 0; i < 4; i++) {
            String name = Skills.NAMES[d.type][i];
            float base = i == 3 ? Skills.SUSANOO_COST : Skills.CHAKRA[d.type][i];
            int cost = Math.round(base * d.penalty());
            String state = d.cooldowns[i] > 0 ? (d.cooldowns[i] / 20 + 1) + "s" : "pronto";
            int col = d.cooldowns[i] > 0 ? 0xAAAAAA : (d.chakra >= cost ? 0x55FF55 : 0xFFAA33);
            if (i == 3 && d.eyeSide != 0) {
                state = "precisa dos 2 olhos";
                col = 0xFF5555;
            } else if (i == 3 && d.susanooOn) {
                state = "ATIVO";
                col = 0x8888FF;
            }
            // mostra a tecla que o jogador realmente configurou nos controles
            MutableComponent line = Component.literal("[")
                    .append(ModKeys.SKILLS[i].getTranslatedKeyMessage())
                    .append(Component.literal("] " + name + " (" + cost + "): " + state));
            gg.drawString(f, line, x, y, col);
            y += 10;
        }
    }
}
