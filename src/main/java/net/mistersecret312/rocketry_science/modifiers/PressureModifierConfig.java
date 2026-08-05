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

public record PressureModifierConfig(double pressure) implements ModifierConfig
{
	public static MapCodec<PressureModifierConfig> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.DOUBLE.fieldOf("pressure").forGetter(PressureModifierConfig::pressure)
	).apply(inst, PressureModifierConfig::new));

	@Override
	public ModifierType<?> getType()
	{
		return ModifierTypeInit.PRESSURE.get();
	}

	@Override
	public void setup(CelestialBody body)
	{
		body.getEnvironment().pressure = this.pressure;
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