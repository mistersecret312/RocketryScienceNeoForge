package net.mistersecret312.rocketry_science.orbit_requirements;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.init.OrbitRequirementInit;

public interface OrbitRequirement {
    RequirementType<?> getType();
    boolean test(SpaceCraft craft); // Add your SpaceCraft entity parameter here later
    Component getFailureMessage();

    Codec<OrbitRequirement> CODEC = OrbitRequirementInit.REGISTRY.byNameCodec()
                                                                   .dispatch(OrbitRequirement::getType, RequirementType::codec);
}