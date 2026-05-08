package net.mistersecret312.rocketry_science.data.orbits;

import com.mojang.serialization.Codec;
import net.mistersecret312.rocketry_science.init.OrbitTypeInit;

public interface OrbitConfig {
	OrbitType<?> getType();
	Codec<OrbitConfig> CODEC = OrbitTypeInit.REGISTRY.byNameCodec()
														.dispatch(OrbitConfig::getType, OrbitType::codec);

	double getAltitude();
	double getPeriod();
}