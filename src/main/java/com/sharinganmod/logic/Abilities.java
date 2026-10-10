package com.sharinganmod.logic;

import com.sharinganmod.data.SharinganData;
import com.sharinganmod.data.Skills;
import com.sharinganmod.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Habilidades dos Mangekyou. Cada uso gasta chakra, entra em recarga e desgasta os olhos. */
public final class Abilities {
    private Abilities() {}

    public static void use(ServerPlayer p, SharinganData d, int slot) {
        if (d.stage < 4) { SharinganLogic.msg(p, "Você precisa do Mangekyou Sharingan."); return; }
        if (!d.mangekyouOn) {
            SharinganLogic.msg(p, Component.literal("Ative o Mangekyou Sharingan (")
                    .append(SharinganLogic.key("mangekyou")).append(Component.literal(") primeiro.")));
            return;
        }
        if (slot == 3) { toggleSusanoo(p, d); return; }
        if (d.cooldowns[slot] > 0) {
            SharinganLogic.msg(p, Skills.NAMES[d.type][slot] + " em recarga: " + (d.cooldowns[slot] / 20 + 1) + "s");
            return;
        }
        float cost = Skills.CHAKRA[d.type][slot] * d.penalty();
        if (d.chakra < cost) {
            SharinganLogic.msg(p, "Chakra insuficiente (" + (int) d.chakra + "/" + (int) Math.ceil(cost) + ").");
            return;
        }
        boolean ok = switch (d.type) {
            case 1 -> switch (slot) {
                case 0 -> tsukuyomi(p);
                case 1 -> amaterasu(p);
                default -> karasu(p);
            };
            case 2 -> switch (slot) {
                case 0 -> chidori(p);
                case 1 -> kagutsuchi(p);
                default -> kirin(p);
            };
            case 3 -> switch (slot) {
                case 0 -> kamuiIntangible(p, d);
                case 1 -> kamuiTeleport(p);
                default -> kamuiSuction(p, d);
            };
            case 4 -> switch (slot) {
                case 0 -> kotoamatsukami(p);
                case 1 -> shunshin(p);
                default -> olhar(p);
            };
            case 5 -> switch (slot) {
                case 0 -> tsukuyomi(p);
                case 1 -> amaterasu(p);
                default -> kirin(p);
            };
            default -> false;
        };
        if (ok) {
            d.chakra -= cost;
            d.cooldowns[slot] = Math.round(Skills.COOLDOWN[d.type][slot] * d.penalty() * (d.halfEye ? 1.5f : 1f));
            SharinganLogic.addStrain(p, d, Skills.STRAIN[d.type][slot]);
        }
    }

    private static void toggleSusanoo(ServerPlayer p, SharinganData d) {
        if (d.susanooOn) {
            SharinganLogic.endSusanoo(p, d);
            return;
        }
        if (d.eyeSide != 0) {
            SharinganLogic.msg(p, "Só é possível usar o Susanoo com os dois olhos.");
            return;
        }
        if (d.cooldowns[3] > 0) {
            SharinganLogic.msg(p, "Susanoo em recarga: " + (d.cooldowns[3] / 20 + 1) + "s");
            return;
        }
        float cost = Skills.SUSANOO_COST * d.penalty();
        if (d.chakra < cost) {
            SharinganLogic.msg(p, "Chakra insuficiente (" + (int) d.chakra + "/" + (int) Math.ceil(cost) + ").");
            return;
        }
        d.chakra -= cost;
        SharinganLogic.startSusanoo(p, d);
        SharinganLogic.addStrain(p, d, Skills.SUSANOO_STRAIN_ON_START);
    }

    // ---------- helpers ----------
    private static LivingEntity rayEntity(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        BlockHitResult br = p.level().clip(new ClipContext(eye, eye.add(look.scale(range)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        double max = br.getType() == HitResult.Type.MISS ? range : br.getLocation().distanceTo(eye);
        Vec3 end = eye.add(look.scale(max));
        AABB box = p.getBoundingBox().expandTowards(look.scale(max)).inflate(1.0);
        EntityHitResult r = ProjectileUtil.getEntityHitResult(p, eye, end, box,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e != p, max * max);
        return r != null && r.getEntity() instanceof LivingEntity le ? le : null;
    }

    private static Vec3 rayPoint(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 end = eye.add(p.getLookAngle().scale(range));
        BlockHitResult br = p.level().clip(new ClipContext(eye, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        return br.getType() == HitResult.Type.MISS ? end : br.getLocation();
    }

    /** Teletransporta para perto de 'dest' (ponto na altura dos olhos) se houver espaco livre. */
    private static boolean teleportSafe(ServerPlayer p, Vec3 dest) {
        for (double dy : new double[]{-1.62, -1.0, 0, 0.5, 1.0, 2.0}) {
            AABB bb = p.getBoundingBox().move(dest.x - p.getX(), dest.y + dy - p.getY(), dest.z - p.getZ());
            if (p.level().noCollision(p, bb)) {
                p.connection.teleport(dest.x, dest.y + dy, dest.z, p.getYRot(), p.getXRot());
                p.fallDistance = 0;
                return true;
            }
        }
        SharinganLogic.msg(p, "Sem espaço para chegar lá.");
        return false;
    }

    private static void snd(ServerPlayer p, SoundEvent s, float pitch) {
        p.level().playSound(null, p.getX(), p.getY(), p.getZ(), s, SoundSource.PLAYERS, 1f, pitch);
    }

    private static void part(ServerLevel l, ParticleOptions o, Vec3 c, int n, double spread) {
        l.sendParticles(o, c.x, c.y, c.z, n, spread, spread, spread, 0.02);
    }

    private static boolean notAlly(LivingEntity e, ServerPlayer p) {
        return e != p && e.isAlive() && !SharinganLogic.isPet(e, p);
    }

    // ---------- Itachi / Eterno ----------
    private static boolean tsukuyomi(ServerPlayer p) {
        LivingEntity t = rayEntity(p, 32);
        if (t == null) { SharinganLogic.msg(p, "Nenhum alvo no seu olhar."); return false; }
        t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 5));
        t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
        t.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
        t.hurt(p.damageSources().indirectMagic(p, p), 8f);
        if (t instanceof Mob m) m.setTarget(null);
        part(p.serverLevel(), ParticleTypes.SMOKE, t.position().add(0, t.getBbHeight() / 2, 0), 40, 0.5);
        snd(p, SoundEvents.ENDERMAN_STARE, 0.5f);
        return true;
    }

    private static boolean amaterasu(ServerPlayer p) {
        LivingEntity t = rayEntity(p, 32);
        Vec3 c = t != null ? t.position() : rayPoint(p, 32);
        ServerLevel l = p.serverLevel();
        for (LivingEntity e : l.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(3.5), x -> notAlly(x, p))) {
            e.addEffect(new MobEffectInstance(ModRegistries.AMATERASU, 240, 0));
        }
        for (int i = 0; i < 24; i++) {
            double a = i * Math.PI * 2 / 24;
            l.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.x + Math.cos(a) * 3, c.y + 0.2, c.z + Math.sin(a) * 3, 3, 0.1, 0.4, 0.1, 0.02);
        }
        part(l, ParticleTypes.SOUL_FIRE_FLAME, c.add(0, 0.5, 0), 40, 1.2);
        snd(p, SoundEvents.FIRECHARGE_USE, 0.5f);
        return true;
    }

    private static boolean karasu(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 120, 0));
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 2));
        part(l, ParticleTypes.LARGE_SMOKE, p.position().add(0, 1, 0), 80, 1.0);
        for (LivingEntity e : l.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(6), x -> notAlly(x, p))) {
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            if (e instanceof Mob m) m.setTarget(null);
        }
        snd(p, SoundEvents.ENDERMAN_TELEPORT, 1.6f);
        return true;
    }

    // ---------- Sasuke ----------
    private static boolean chidori(ServerPlayer p) {
        Vec3 look = p.getLookAngle();
        Vec3 start = p.position();
        AABB path = new AABB(start, start.add(look.scale(10))).inflate(1.5, 1.5, 1.5).move(0, 1, 0);
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, path, x -> notAlly(x, p))) {
            e.hurt(p.damageSources().playerAttack(p), 12f);
        }
        p.setDeltaMovement(look.x * 2.6, look.y * 2.0 + 0.15, look.z * 2.6);
        p.hurtMarked = true;
        p.fallDistance = 0;
        ServerLevel l = p.serverLevel();
        for (int i = 0; i < 12; i++) {
            part(l, ParticleTypes.ELECTRIC_SPARK, start.add(look.scale(i * 0.9)).add(0, 1, 0), 6, 0.3);
        }
        snd(p, SoundEvents.LIGHTNING_BOLT_THUNDER, 2f);
        return true;
    }

    private static boolean kagutsuchi(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        for (int i = 1; i <= 26; i++) {
            Vec3 pt = eye.add(look.scale(i));
            BlockPos bp = BlockPos.containing(pt);
            if (!l.getBlockState(bp).getCollisionShape(l, bp).isEmpty()) break;
            part(l, ParticleTypes.SOUL_FIRE_FLAME, pt, 4, 0.25);
            for (LivingEntity e : l.getEntitiesOfClass(LivingEntity.class, new AABB(pt, pt).inflate(1.2), x -> notAlly(x, p))) {
                e.addEffect(new MobEffectInstance(ModRegistries.AMATERASU, 160, 0));
            }
        }
        snd(p, SoundEvents.BLAZE_SHOOT, 0.5f);
        return true;
    }

    private static boolean kirin(ServerPlayer p) {
        LivingEntity t = rayEntity(p, 48);
        Vec3 target = t != null ? t.position() : rayPoint(p, 48);
        if (target.distanceTo(p.position()) < 6) {
            SharinganLogic.msg(p, "Alvo muito perto para o Kirin.");
            return false;
        }
        ServerLevel l = p.serverLevel();
        for (int i = 0; i < 3; i++) {
            LightningBolt b = EntityType.LIGHTNING_BOLT.create(l);
            if (b != null) {
                b.moveTo(target.x + (i == 0 ? 0 : (l.random.nextDouble() - 0.5) * 4), target.y,
                        target.z + (i == 0 ? 0 : (l.random.nextDouble() - 0.5) * 4));
                b.setCause(p);
                l.addFreshEntity(b);
            }
        }
        for (LivingEntity e : l.getEntitiesOfClass(LivingEntity.class, new AABB(target, target).inflate(5), x -> notAlly(x, p))) {
            e.hurt(p.damageSources().indirectMagic(p, p), 25f);
        }
        return true;
    }

    // ---------- Obito ----------
    private static boolean kamuiIntangible(ServerPlayer p, SharinganData d) {
        d.intangibleTicks = 100;
        snd(p, SoundEvents.ENDERMAN_TELEPORT, 0.5f);
        return true;
    }

    private static boolean kamuiTeleport(ServerPlayer p) {
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        Vec3 end = eye.add(look.scale(64));
        BlockHitResult br = p.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        Vec3 dest = br.getType() == HitResult.Type.MISS ? end : br.getLocation().subtract(look.scale(0.6));
        ServerLevel l = p.serverLevel();
        part(l, ParticleTypes.PORTAL, p.position().add(0, 1, 0), 60, 0.6);
        if (!teleportSafe(p, dest)) return false;
        part(l, ParticleTypes.PORTAL, p.position().add(0, 1, 0), 60, 0.6);
        snd(p, SoundEvents.ENDERMAN_TELEPORT, 0.6f);
        return true;
    }

    private static boolean kamuiSuction(ServerPlayer p, SharinganData d) {
        d.suctionTicks = 80;
        snd(p, SoundEvents.ENDER_DRAGON_GROWL, 1.8f);
        return true;
    }

    // ---------- Shisui ----------
    private static boolean kotoamatsukami(ServerPlayer p) {
        LivingEntity t = rayEntity(p, 32);
        if (t == null) { SharinganLogic.msg(p, "Nenhum alvo no seu olhar."); return false; }
        if (t instanceof Mob m) {
            SharinganLogic.CHARM.put(t.getUUID(), 600);
            m.setTarget(null);
            t.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        } else {
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 6));
            t.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 3));
            t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 3));
            t.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            if (t instanceof ServerPlayer tp) SharinganLogic.msg(tp, "Você foi dominado pelo Kotoamatsukami!");
        }
        part(p.serverLevel(), ParticleTypes.ENCHANT, t.position().add(0, t.getBbHeight(), 0), 40, 0.5);
        snd(p, SoundEvents.ILLUSIONER_CAST_SPELL, 1f);
        return true;
    }

    private static boolean shunshin(ServerPlayer p) {
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        Vec3 end = eye.add(look.scale(12));
        BlockHitResult br = p.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        Vec3 dest = br.getType() == HitResult.Type.MISS ? end : br.getLocation().subtract(look.scale(0.6));
        ServerLevel l = p.serverLevel();
        part(l, ParticleTypes.CLOUD, p.position().add(0, 1, 0), 30, 0.5);
        if (!teleportSafe(p, dest)) return false;
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 2));
        part(l, ParticleTypes.CLOUD, p.position().add(0, 1, 0), 30, 0.5);
        snd(p, SoundEvents.ENDERMAN_TELEPORT, 1.8f);
        return true;
    }

    private static boolean olhar(ServerPlayer p) {
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(32), x -> x != p && x.isAlive())) {
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        }
        snd(p, SoundEvents.BEACON_ACTIVATE, 1.8f);
        return true;
    }
}
