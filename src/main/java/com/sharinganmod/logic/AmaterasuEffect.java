package com.sharinganmod.logic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Chamas negras: causam dano continuo, ignorando o tempo de invulnerabilidade. */
public class AmaterasuEffect extends MobEffect {
    public AmaterasuEffect() {
        super(MobEffectCategory.HARMFUL, 0x1A0A2A);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel sl) {
            entity.invulnerableTime = 0;
            entity.hurt(entity.damageSources().onFire(), 2.0f);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                    entity.getZ(), 8, 0.3, entity.getBbHeight() / 3, 0.3, 0.02);
        }
        return true;
    }
}
