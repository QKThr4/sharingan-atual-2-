package com.sharinganmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sharinganmod.SharinganMod;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Desenha os dois olhos (Sharingan) no rosto do jogador enquanto o Sharingan esta ativo. */
public class FaceLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final String[] NAMES = {"", "1", "2", "3", "itachi", "sasuke", "obito", "shisui", "eterno"};
    private static final ResourceLocation[] TEX = new ResourceLocation[NAMES.length];

    static {
        for (int i = 1; i < NAMES.length; i++) {
            TEX[i] = ResourceLocation.fromNamespaceAndPath(SharinganMod.MODID, "textures/gui/eye_" + NAMES[i] + ".png");
        }
    }

    /** Um pouco a frente da camada externa da cabeca (z = -4,5) para nao sumir no cabelo. */
    private static final float EYE_Z = -4.6f;

    public FaceLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        int packed = ClientFace.get(player.getUUID());
        int code = packed & 15;
        // 0 os dois olhos, 1 so o esquerdo, 2 so o direito (o esquerdo do jogador fica em +x no modelo)
        int side = packed >> 4;
        if (code <= 0 || code >= TEX.length || player.isInvisible() || player.isSpectator()) return;

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEX[code]));
        poseStack.pushPose();
        this.getParentModel().getHead().translateAndRotate(poseStack);
        PoseStack.Pose pose = poseStack.last();
        float dx = ClientLayout.eyeDx;
        float cy = ClientLayout.eyeY;
        float half = ClientLayout.eyeHalf;
        if (side == 0 || side == 1) quad(vc, pose, dx, cy, half);
        if (side == 0 || side == 2) quad(vc, pose, -dx, cy, half);
        poseStack.popPose();
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose, float cx, float cy, float half) {
        float s = 1.0f / 16.0f;
        float x0 = (cx - half) * s;
        float x1 = (cx + half) * s;
        float y0 = (cy - half) * s;
        float y1 = (cy + half) * s;
        float z = EYE_Z * s;
        vertex(vc, pose, x0, y1, z, 0f, 1f);
        vertex(vc, pose, x1, y1, z, 1f, 1f);
        vertex(vc, pose, x1, y0, z, 1f, 0f);
        vertex(vc, pose, x0, y0, z, 0f, 0f);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v) {
        vc.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0f, 0.0f, -1.0f);
    }
}
