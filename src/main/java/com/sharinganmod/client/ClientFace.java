package com.sharinganmod.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Qual olho aparece no rosto de cada jogador (recebido do servidor). Codigos em FacePayload. */
public final class ClientFace {
    private ClientFace() {}

    private static final Map<UUID, Integer> FACES = new HashMap<>();

    public static void set(UUID id, int code) {
        if (code <= 0) FACES.remove(id);
        else FACES.put(id, code);
    }

    public static int get(UUID id) {
        return FACES.getOrDefault(id, 0);
    }

    public static void clear() {
        FACES.clear();
    }
}
