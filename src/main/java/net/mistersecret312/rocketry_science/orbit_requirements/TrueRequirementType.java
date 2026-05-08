package net.mistersecret312.rocketry_science.orbit_requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.mistersecret312.rocketry_science.data.orbiting_objects.SpaceCraft;
import net.mistersecret312.rocketry_science.init.OrbitRequirementInit;

public record TrueRequirementType(boolean value) implements OrbitRequirement
{
	public static final MapCodec<TrueRequirementType> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.BOOL.fieldOf("value").forGetter(TrueRequirementType::value)
	).apply(inst, TrueRequirementType::new));

	@Override
	public RequirementType<?> getType()
	{
		return OrbitRequirementInit.TRUE.get();
	}

	@Override
	public boolean test(SpaceCraft craft)
	{
		return value;
	}

	@Override
	public Component getFailureMessage() {
		return Component.literal("Said so!");
	}
}
