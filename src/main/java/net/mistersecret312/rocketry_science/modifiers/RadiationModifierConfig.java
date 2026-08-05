package net.mistersecret312.rocketry_science.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.environment.modifiers.ModifierConfig;
import net.mistersecret312.rocketry_science.environment.modifiers.ModifierType;
import net.mistersecret312.rocketry_science.init.ModifierTypeInit;

public record RadiationModifierConfig(double radiation) implements ModifierConfig
{
	public static MapCodec<RadiationModifierConfig> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.DOUBLE.fieldOf("radiation").forGetter(RadiationModifierConfig::radiation)
	).apply(inst, RadiationModifierConfig::new));

	@Override
	public ModifierType<?> getType()
	{
		return ModifierTypeInit.RADIATION.get();
	}

	@Override
	public void setup(CelestialBody body)
	{
		body.getEnvironment().radiation = this.radiation;
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