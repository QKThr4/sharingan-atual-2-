package com.sharinganmod.client;

import com.sharinganmod.SharinganMod;
import com.sharinganmod.data.SharinganData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Editor de layout: arraste os olhos no rosto (posicao), mude o tamanho com o controle ou a roda do mouse,
 * e arraste os blocos do HUD (status/chakra e habilidades) para onde quiser na tela.
 */
public class LayoutScreen extends Screen {
    /** Pixels de tela por pixel da pele (a cabeca tem 8x8). */
    private static final int S = 12;

    private final Screen parent;
    private int dragging;
    private double offX;
    private double offY;

    public LayoutScreen(Screen parent) {
        super(Component.literal("Layout do Sharingan"));
        this.parent = parent;
    }

    private int faceX() {
        return this.width / 2 - 4 * S;
    }

    private int faceY() {
        return this.height / 2 - 4 * S - 6;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        double v = (ClientLayout.eyeHalf - ClientLayout.HALF_MIN) / (ClientLayout.HALF_MAX - ClientLayout.HALF_MIN);
        addRenderableWidget(new AbstractSliderButton(cx - 100, this.height - 52, 200, 20, Component.empty(), v) {
            {
                updateMessage();
            }

            @Override
            protected void updateMessage() {
                setMessage(Component.literal("Tamanho dos olhos: " + Math.round(ClientLayout.eyeHalf / 1.5f * 100f) + "%"));
            }

            @Override
            protected void applyValue() {
                ClientLayout.eyeHalf = ClientLayout.HALF_MIN
                        + (float) this.value * (ClientLayout.HALF_MAX - ClientLayout.HALF_MIN);
                ClientLayout.clamp();
            }
        });
        addRenderableWidget(Button.builder(Component.literal("Restaurar padrão"), b -> {
            ClientLayout.reset();
            rebuildWidgets();
        }).bounds(cx - 100, this.height - 28, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Salvar e voltar"), b -> onClose())
                .bounds(cx + 2, this.height - 28, 98, 20).build());
    }

    private static String eyeKey() {
        SharinganData d = ClientData.DATA;
        if (d.stage >= 4 && d.level() >= 4) {
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

    private int statusBoxX() {
        return ClientLayout.place(ClientLayout.statusX, this.width, ClientLayout.STATUS_W);
    }

    private int statusBoxY() {
        return ClientLayout.place(ClientLayout.statusY, this.height, ClientLayout.STATUS_H);
    }

    private int skillsBoxX() {
        return ClientLayout.place(ClientLayout.skillsX, this.width, ClientLayout.SKILLS_W);
    }

    private int skillsBoxY() {
        return ClientLayout.place(ClientLayout.skillsY, this.height, ClientLayout.SKILLS_H);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void box(GuiGraphics gg, int x, int y, int w, int h, String label, int color) {
        gg.fill(x, y, x + w, y + h, 0x80000000 | (color & 0xFFFFFF));
        gg.fill(x, y, x + w, y + 1, 0xFFFFFFFF);
        gg.fill(x, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        gg.fill(x, y, x + 1, y + h, 0xFFFFFFFF);
        gg.fill(x + w - 1, y, x + w, y + h, 0xFFFFFFFF);
        gg.drawCenteredString(this.font, label, x + w / 2, y + h / 2 - 4, 0xFFFFFF);
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.render(gg, mouseX, mouseY, partialTick);
        int cx = this.width / 2;
        gg.drawCenteredString(this.font, "Layout do Sharingan", cx, 8, 0xFFFFFF);
        gg.drawCenteredString(this.font, "Arraste os olhos no rosto, role a roda sobre o rosto para mudar o tamanho",
                cx, 20, 0xCCCCCC);
        gg.drawCenteredString(this.font, "e arraste os blocos azul (status/chakra) e vermelho (habilidades) pela tela.",
                cx, 31, 0xCCCCCC);

        // rosto do jogador
        int fx = faceX();
        int fy = faceY();
        gg.fill(fx - 3, fy - 3, fx + 8 * S + 3, fy + 8 * S + 3, 0xFF555555);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ResourceLocation skin = mc.player.getSkin().texture();
            gg.blit(skin, fx, fy, 8 * S, 8 * S, 8, 8, 8, 8, 64, 64);
            gg.blit(skin, fx, fy, 8 * S, 8 * S, 40, 8, 8, 8, 64, 64);
        }
        ResourceLocation eye = ResourceLocation.fromNamespaceAndPath(SharinganMod.MODID,
                "textures/gui/eye_" + eyeKey() + ".png");
        float half = ClientLayout.eyeHalf * S;
        int size = Math.round(half * 2);
        float ey = fy + (8 + ClientLayout.eyeY) * S;
        for (int sign = -1; sign <= 1; sign += 2) {
            float ex = cx + sign * ClientLayout.eyeDx * S;
            gg.blit(eye, Math.round(ex - half), Math.round(ey - half), size, size, 0, 0, 128, 128, 128, 128);
        }

        box(gg, statusBoxX(), statusBoxY(), ClientLayout.STATUS_W, ClientLayout.STATUS_H, "Status e chakra", 0x2255DD);
        box(gg, skillsBoxX(), skillsBoxY(), ClientLayout.SKILLS_W, ClientLayout.SKILLS_H, "Habilidades", 0xDD2222);
    }

    private void dragEyes(double mx, double my) {
        ClientLayout.eyeDx = (float) (Math.abs(mx - this.width / 2.0) / S);
        ClientLayout.eyeY = (float) ((my - faceY()) / S - 8);
        ClientLayout.clamp();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        if (button != 0) return false;
        if (inside(mx, my, skillsBoxX(), skillsBoxY(), ClientLayout.SKILLS_W, ClientLayout.SKILLS_H)) {
            dragging = 2;
            offX = mx - skillsBoxX();
            offY = my - skillsBoxY();
            return true;
        }
        if (inside(mx, my, statusBoxX(), statusBoxY(), ClientLayout.STATUS_W, ClientLayout.STATUS_H)) {
            dragging = 1;
            offX = mx - statusBoxX();
            offY = my - statusBoxY();
            return true;
        }
        if (inside(mx, my, faceX(), faceY(), 8 * S, 8 * S)) {
            dragging = 3;
            dragEyes(mx, my);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging == 1) {
            ClientLayout.statusX = ClientLayout.frac(mx - offX, this.width, ClientLayout.STATUS_W);
            ClientLayout.statusY = ClientLayout.frac(my - offY, this.height, ClientLayout.STATUS_H);
            return true;
        }
        if (dragging == 2) {
            ClientLayout.skillsX = ClientLayout.frac(mx - offX, this.width, ClientLayout.SKILLS_W);
            ClientLayout.skillsY = ClientLayout.frac(my - offY, this.height, ClientLayout.SKILLS_H);
            return true;
        }
        if (dragging == 3) {
            dragEyes(mx, my);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = 0;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (inside(mx, my, faceX(), faceY(), 8 * S, 8 * S)) {
            ClientLayout.eyeHalf += (float) scrollY * 0.1f;
            ClientLayout.clamp();
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        ClientLayout.save();
        Minecraft.getInstance().setScreen(parent);
    }
}
