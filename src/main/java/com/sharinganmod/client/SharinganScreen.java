package com.sharinganmod.client;

import com.sharinganmod.SharinganMod;
import com.sharinganmod.data.SharinganData;
import com.sharinganmod.data.Skills;
import com.sharinganmod.net.ActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

/** Aba especial do Sharingan: olho em uso, clã, chakra e ações. */
public class SharinganScreen extends Screen {
    private Button equip;
    private Button split;

    public SharinganScreen() {
        super(Component.literal("Aba Sharingan"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 + 52;
        equip = addRenderableWidget(Button.builder(Component.literal("Retirar Sharingan"), b -> send(8))
                .bounds(cx - 110, y, 220, 20).build());
        split = addRenderableWidget(Button.builder(Component.literal("Partir o olho ao meio"), b -> send(7))
                .bounds(cx - 110, y + 22, 220, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Layout: posição e tamanho"),
                        b -> Minecraft.getInstance().setScreen(new LayoutScreen(this)))
                .bounds(cx - 110, y + 44, 220, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose())
                .bounds(cx - 110, y + 66, 220, 20).build());
        refreshButtons();
    }

    private void send(int action) {
        PacketDistributor.sendToServer(new ActionPayload(action));
    }

    private void refreshButtons() {
        SharinganData d = ClientData.DATA;
        if (equip == null) return;
        String what = d.stage >= 5 ? "Mangekyou Eterno (com o 3 Tomoe)"
                : d.stage == 4 ? "Mangekyou (com o 3 Tomoe)"
                : "Sharingan (" + d.stage + " Tomoe)";
        String label = d.stage <= 0 ? "Sem Sharingan"
                : "Retirar " + what + (d.eyeSide != 0 ? " - " + Skills.sideName(d.eyeSide) : "");
        equip.setMessage(Component.literal(label));
        equip.active = d.stage > 0;
        split.setMessage(Component.literal(d.eyeSide != 0 ? "Você já tem só um olho" : "Partir o olho ao meio (sai esquerdo ou direito)"));
        split.active = d.stage > 0 && d.eyeSide == 0;
    }

    @Override
    public void tick() {
        refreshButtons();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeys.SCREEN.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Nome da textura do olho mostrado: Mangekyou so aparece enquanto ele esta sendo usado. */
    private static String iconKey(SharinganData d) {
        if (d.eyesActive() && d.level() >= 4) {
            if (d.stage == 5) return "eterno";
            return switch (d.type) {
                case 1 -> "itachi";
                case 2 -> "sasuke";
                case 3 -> "obito";
                default -> "shisui";
            };
        }
        return String.valueOf(Math.max(1, Math.min(3, d.stage)));
    }

    private static String time(int seconds) {
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    @Override
    public void renderBackground(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gg, mouseX, mouseY, partialTick);
        SharinganData d = ClientData.DATA;
        int cx = this.width / 2;
        int cy = this.height / 2;

        gg.fill(cx - 140, cy - 130, cx + 140, cy + 144, 0xC0101010);
        gg.fill(cx - 140, cy - 130, cx + 140, cy - 128, 0xFFAA1111);
        gg.drawCenteredString(this.font, "Aba Sharingan", cx, cy - 124, 0xFFFFFF);

        // slot do olho em uso
        gg.fill(cx - 34, cy - 112, cx + 34, cy - 44, 0xFF555555);
        gg.fill(cx - 32, cy - 110, cx + 32, cy - 46, 0xFF1A1A1A);
        if (d.stage > 0) {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(SharinganMod.MODID,
                    "textures/gui/eye_" + iconKey(d) + ".png");
            // desligado = mais escuro
            if (!d.eyesActive()) gg.setColor(0.3f, 0.3f, 0.3f, 1f);
            gg.blit(tex, cx - 30, cy - 108, 60, 60, 0, 0, 128, 128, 128, 128);
            gg.setColor(1f, 1f, 1f, 1f);
        }

        int y = cy - 40;
        if (d.stage <= 0) {
            gg.drawCenteredString(this.font, "Sem Sharingan", cx, y, 0xAAAAAA);
            y += 11;
        } else if (d.eyesActive() && d.level() >= 4) {
            gg.drawCenteredString(this.font, "Usando: " + Skills.stageName(d) + " [ATIVO]", cx, y, 0xFF5555);
            y += 11;
        } else {
            String base = "Sharingan " + Math.min(3, d.stage) + " Tomoe";
            gg.drawCenteredString(this.font, base + (d.eyesActive() ? " [ATIVO]" : " (desativado)"), cx, y,
                    d.eyesActive() ? 0xFF5555 : 0x888888);
            y += 11;
            if (d.stage >= 4) {
                String mk = d.stage >= 5 ? "Mangekyou Eterno" : "Mangekyou de " + Skills.TYPE_NAMES[d.type];
                gg.drawCenteredString(this.font, mk + " desbloqueado (não está em uso)", cx, y, 0xDD8888);
                y += 11;
            }
        }
        if (d.stage > 0 && d.eyeSide != 0) {
            gg.drawCenteredString(this.font, "Só o " + Skills.sideName(d.eyeSide) + ": sem Susanoo, recargas +50%",
                    cx, y, 0xBB88FF);
            y += 11;
        }
        if (d.uchiha) {
            gg.drawCenteredString(this.font, "Clã Uchiha: recargas e custos normais", cx, y, 0x66DD66);
        } else {
            gg.drawCenteredString(this.font, "Sem o Clã Uchiha: recargas e custos maiores", cx, y, 0xDD6666);
        }
        y += 11;
        if (d.stage > 0) {
            gg.drawCenteredString(this.font, "Chakra: " + (int) d.chakra + "/" + (int) d.maxChakra()
                    + " (dura " + time((int) d.fullSeconds()) + " com a barra cheia)", cx, y, 0x66AAFF);
            y += 11;
        }

        if (d.stage == 0) {
            if (d.awakened) {
                gg.drawCenteredString(this.font, "Só se consegue outro Sharingan por implante.", cx, y, 0xAAAAAA);
            } else if (d.uchiha) {
                int chance = (int) (SharinganData.AWAKEN_CHANCE_UCHIHA * 100);
                gg.drawCenteredString(this.font, "Sobreviva a um golpe quase fatal (" + chance + "% de chance).", cx, y, 0xAAAAAA);
            } else {
                gg.drawCenteredString(this.font, "Use o Emblema do Clã Uchiha para poder despertar.", cx, y, 0xAAAAAA);
            }
        } else if (d.stage < 3) {
            gg.drawCenteredString(this.font, Component.literal("XP: " + d.xp + " / " + d.xpNeeded() + " (ative com ")
                    .append(Component.keybind("key.sharingan.sharingan")).append(Component.literal(" e ganhe XP)")), cx, y, 0xAAAAAA);
        } else if (d.stage == 3) {
            gg.drawCenteredString(this.font, "Mate um mob domesticado para despertar o Mangekyou.", cx, y, 0xAAAAAA);
        } else {
            gg.drawCenteredString(this.font, d.stage == 5 ? "Sem desgaste ocular." : "Desgaste ocular: " + (int) d.strain + "% (permanente)", cx, y, 0xAAAAAA);
        }
        y += 11;
        if (d.blind) {
            gg.drawCenteredString(this.font, d.stage == 5 ? "Cegueira permanente (o Eterno devolve a visão)"
                    : "CEGUEIRA PERMANENTE (o Sharingan mostra silhuetas)", cx, y, 0xFF2222);
        }
    }
}
