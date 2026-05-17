package net.mistersecret312.rocketry_science.environment.modifiers;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.init.ModifierTypeInit;

public interface ModifierConfig {
	ModifierType<?> getType();
	Codec<ModifierConfig> CODEC = ModifierTypeInit.REGISTRY.byNameCodec()
														   .dispatch(ModifierConfig::getType, ModifierType::codec);

	void setup(CelestialBody body);

	void tick(Level level);
	void tick(Entity entity);
}