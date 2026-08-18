package net.mistersecret312.rocketry_science.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.environment.EnvironmentData;
import net.mistersecret312.rocketry_science.environment.modifiers.ModifierConfig;
import net.mistersecret312.rocketry_science.environment.modifiers.ModifierType;
import net.mistersecret312.rocketry_science.init.ModifierTypeInit;

public record TemperatureModifierConfig(double nightTimeTemperature, double dayTimeTemperature) implements ModifierConfig
{
	public static MapCodec<TemperatureModifierConfig> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.DOUBLE.fieldOf("night_temperature").forGetter(TemperatureModifierConfig::nightTimeTemperature),
			Codec.DOUBLE.fieldOf("day_temperature").forGetter(TemperatureModifierConfig::dayTimeTemperature)
	).apply(inst, TemperatureModifierConfig::new));

	@Override
	public ModifierType<?> getType()
	{
		return ModifierTypeInit.TEMPERATURE.get();
	}

	@Override
	public void setup(CelestialBody body)
	{
		body.getEnvironment().temperature = new EnvironmentData.TemperatureGradient(nightTimeTemperature, dayTimeTemperature);
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