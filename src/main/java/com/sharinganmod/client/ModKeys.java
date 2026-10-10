package com.sharinganmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class ModKeys {
    private ModKeys() {}

    public static final String CATEGORY = "key.categories.sharingan";

    public static final KeyMapping SCREEN = key("key.sharingan.screen", GLFW.GLFW_KEY_J);
    public static final KeyMapping SHARINGAN = key("key.sharingan.sharingan", GLFW.GLFW_KEY_V);
    public static final KeyMapping MANGEKYOU = key("key.sharingan.mangekyou", GLFW.GLFW_KEY_B);
    public static final KeyMapping SKILL1 = key("key.sharingan.skill1", GLFW.GLFW_KEY_BACKSLASH);
    public static final KeyMapping SKILL2 = key("key.sharingan.skill2", GLFW.GLFW_KEY_Z);
    public static final KeyMapping SKILL3 = key("key.sharingan.skill3", GLFW.GLFW_KEY_X);
    public static final KeyMapping SKILL4 = key("key.sharingan.skill4", GLFW.GLFW_KEY_C);

    /** Habilidades 1 a 4, na ordem. O HUD mostra o nome real da tecla configurada nos controles. */
    public static final KeyMapping[] SKILLS = {SKILL1, SKILL2, SKILL3, SKILL4};

    private static KeyMapping key(String name, int code) {
        return new KeyMapping(name, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, code, CATEGORY);
    }
}
