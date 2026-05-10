package net.mistersecret312.rocketry_science.data.orbits;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import org.joml.Vector2d;

public class TransferOrbit extends Orbit<SpaceCraft>
{
	private final SpaceCraft craft;

	private final TravelPoint departure;
	private final TravelPoint arrival;

	private final long travelDuration;

	public TransferOrbit(SpaceCraft craft, TravelPoint departure, TravelPoint arrival, long travelDuration)
	{
		this.craft = craft;
		this.departure = departure;
		this.arrival = arrival;
		this.travelDuration = travelDuration;
	}

	public TransferOrbit(SpaceCraft craft)
	{
		this.craft = craft;
		this.departure = null;
		this.arrival = null;
		this.travelDuration = 0;
	}

	public TravelPoint getDeparture()
	{
		return departure;
	}

	public TravelPoint getArrival()
	{
		return arrival;
	}

	public long getTravelDuration()
	{
		return travelDuration;
	}

	@Override
	public double getOrbitalPeriod()
	{
		return 0;
	}

	@Override
	public double getOrbitalAltitude()
	{
		return 0;
	}

	@Override
	Vector2d getPosition(long tick, RegistryAccess registryAccess)
	{
		Registry<CelestialBody> registry = registryAccess.registryOrThrow(CelestialBody.REGISTRY_KEY);
		CelestialBody departureBody = registry.get(this.getDeparture().getBody());
		CelestialBody arrivalBody = registry.get(this.getArrival().getBody());

		if(departureBody != null && arrivalBody != null)
		{
			Vector2d departurePoint = departureBody.getOrbit().getPosition(getDeparture().getTick(), registryAccess);
			Vector2d arrivalPoint = arrivalBody.getOrbit().getPosition(getArrival().getTick(), registryAccess);

			return new Vector2d(
					Mth.lerp(getAngle(tick), departurePoint.x, arrivalPoint.x),
					Mth.lerp(getAngle(tick), departurePoint.y, arrivalPoint.y)
			);
		}

		return new Vector2d();
	}

	@Override
	double getAngle(long tick)
	{
		if(getTravelDuration() <= 0)
			return 1;

		double progress = (double) (tick - getDeparture().getTick()) / getTravelDuration();
		return Mth.clamp(progress, 0D, 1D);
	}

	public double getProgress(long tick)
	{
		if(getArrival().getTick()-getDeparture().getTick() == 0)
			return 1;

		double ratio = (double) (tick - getDeparture().getTick()) / (getArrival().getTick() - getDeparture().getTick());
		return Math.max(1, Math.min(0, ratio));
	}

	@Override
	public ResourceKey<CelestialBody> getParent()
	{
		return getDeparture().getBody();
	}

	@Override
	public SpaceCraft getOrbitingObject()
	{
		return craft;
	}

	@Override
	public TransferOrbit load(CompoundTag tag, HolderLookup.Provider registryAccess)
	{
		TravelPoint departure = TravelPoint.load(tag.getCompound("departure"));
		TravelPoint arrival = TravelPoint.load(tag.getCompound("arrival"));

		long travelDuration = tag.getLong("travel_duration");

		return new TransferOrbit(this.getOrbitingObject(), departure, arrival, travelDuration);
	}

	@Override
	public CompoundTag save(HolderLookup.Provider registryAccess)
	{
		CompoundTag tag = new CompoundTag();

		tag.put("departure", this.getDeparture().save());
		tag.put("arrival", this.getArrival().save());
		tag.putLong("travel_duration", this.getTravelDuration());

		return tag;
	}
}
