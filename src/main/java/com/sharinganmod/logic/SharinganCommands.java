package com.sharinganmod.logic;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.sharinganmod.data.SharinganData;
import com.sharinganmod.net.Net;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/** Comandos de teste/admin (OP nivel 2). */
public final class SharinganCommands {
    private SharinganCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sharingan")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("stage")
                        .then(Commands.argument("n", IntegerArgumentType.integer(0, 3))
                                .executes(c -> stage(c.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(c, "n")))))
                .then(Commands.literal("mangekyou")
                        .then(Commands.literal("itachi").executes(c -> mk(c.getSource().getPlayerOrException(), 1)))
                        .then(Commands.literal("sasuke").executes(c -> mk(c.getSource().getPlayerOrException(), 2)))
                        .then(Commands.literal("obito").executes(c -> mk(c.getSource().getPlayerOrException(), 3)))
                        .then(Commands.literal("shisui").executes(c -> mk(c.getSource().getPlayerOrException(), 4)))
                        .then(Commands.literal("eterno").executes(c -> mk(c.getSource().getPlayerOrException(), 5))))
                .then(Commands.literal("uchiha")
                        .then(Commands.argument("valor", BoolArgumentType.bool())
                                .executes(c -> {
                                    ServerPlayer p = c.getSource().getPlayerOrException();
                                    SharinganData.get(p).uchiha = BoolArgumentType.getBool(c, "valor");
                                    Net.sync(p);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(Commands.literal("chakra").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    SharinganData d = SharinganData.get(p);
                    d.chakra = d.maxChakra();
                    Net.sync(p);
                    return Command.SINGLE_SUCCESS;
                }))
                // somente para testes de administrador: jogadores nao tem como desfazer a cegueira
                .then(Commands.literal("cure").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    SharinganData d = SharinganData.get(p);
                    d.blind = false;
                    d.strain = 0;
                    Net.sync(p);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("blind").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    SharinganData d = SharinganData.get(p);
                    d.blind = true;
                    d.strain = 100;
                    d.mangekyouOn = false;
                    Net.sync(p);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("reset").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    SharinganData d = SharinganData.get(p);
                    SharinganLogic.deactivateAll(p, d);
                    d.stage = 0;
                    d.type = 0;
                    d.xp = 0;
                    d.setBoth();
                    d.awakened = false;
                    Net.sync(p);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("xp")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(c -> {
                                    ServerPlayer p = c.getSource().getPlayerOrException();
                                    SharinganLogic.addXp(p, SharinganData.get(p), IntegerArgumentType.getInteger(c, "amount"));
                                    Net.sync(p);
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }

    private static int stage(ServerPlayer p, int n) {
        SharinganData d = SharinganData.get(p);
        if (n == 0) SharinganLogic.deactivateAll(p, d);
        if (d.susanooOn) SharinganLogic.endSusanoo(p, d);
        d.stage = n;
        d.type = 0;
        d.xp = 0;
        d.mangekyouOn = false;
        d.setBoth();
        if (d.stage > 0) d.awakened = true;
        d.equipped = true;
        d.chakra = d.maxChakra();
        SharinganLogic.refreshEffects(p, d);
        Net.sync(p);
        return Command.SINGLE_SUCCESS;
    }

    private static int mk(ServerPlayer p, int type) {
        SharinganData d = SharinganData.get(p);
        if (d.susanooOn) SharinganLogic.endSusanoo(p, d);
        d.stage = type == 5 ? 5 : 4;
        d.type = type;
        d.setBoth();
        if (d.stage > 0) d.awakened = true;
        d.mangekyouOn = false;
        d.equipped = true;
        d.chakra = d.maxChakra();
        SharinganLogic.refreshEffects(p, d);
        Net.sync(p);
        return Command.SINGLE_SUCCESS;
    }
}
