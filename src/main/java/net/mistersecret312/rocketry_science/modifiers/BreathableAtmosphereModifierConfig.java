package net.mistersecret312.rocketry_science.modifiers;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.environment.modifiers.ModifierConfig;
import net.mistersecret312.rocketry_science.environment.modifiers.ModifierType;
import net.mistersecret312.rocketry_science.init.ModifierTypeInit;

public record BreathableAtmosphereModifierConfig() implements ModifierConfig
{
	public static final MapCodec<BreathableAtmosphereModifierConfig> CODEC = MapCodec.unit(new BreathableAtmosphereModifierConfig());

	@Override
	public ModifierType<?> getType()
	{
		return ModifierTypeInit.BREATHABLE.get();
	}

	@Override
	public void setup(CelestialBody body)
	{

	}

	@Override
	public void tick(Level level)
	{

	}

	@Override
	public void tick(Entity entity)
	{

	}
}
