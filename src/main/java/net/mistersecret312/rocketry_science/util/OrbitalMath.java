package net.mistersecret312.rocketry_science.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.rocketry_science.data.orbits.ArtificialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.TransferOrbit;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.vessel.Stage;

public class OrbitalMath
{
	public static final double TIME_SCALE = 0.000008;
	public static final double KM_TO_AU = 1.0 / 149597870;

	public static double getOrbitDeltaV(CelestialBody body, double height)
	{
		double altitude = body.getRadius()+height;
		return Math.sqrt(EnvironmentUtil.getGravitationalParameter(body)/altitude);
	}

	public static double getLaunchDeltaV(CelestialBody body, double targetOrbitHeight)
	{
		double startHeight = 120000;

		double part1 = getOrbitDeltaV(body, startHeight);
		double part2 = getTransferDeltaV(body, startHeight, targetOrbitHeight);

		return part1+part2;
	}

	public static double getTransferDeltaV(CelestialBody body, double initialHeight, double targetHeight)
	{
		initialHeight += body.getRadius();
		targetHeight += body.getRadius();

		double mu = EnvironmentUtil.getGravitationalParameter(body);
		double part1 = Math.sqrt(mu / initialHeight) * (Math.sqrt((2.0 * targetHeight) / (initialHeight + targetHeight)) - 1.0);
		double part2 = Math.sqrt(mu / targetHeight) * (1.0 - Math.sqrt((2.0 * initialHeight) / (initialHeight + targetHeight)));

		return Math.abs(part1) + Math.abs(part2);
	}

	public static TransferOrbit.TransferData calculateTransfer(Level level, ArtificialOrbit origin, ArtificialOrbit destination, double currentTime) {
		CelestialBody parentA = OrbitUtil.getCelestialBody(origin.getParent(level.registryAccess()), level);
		CelestialBody parentB = OrbitUtil.getCelestialBody(destination.getParent(level.registryAccess()), level);

		if (parentA.equals(parentB)) {
			return getHohmann(origin.getOrbitalAltitude() + parentA.getRadius(), destination.getOrbitalAltitude() + parentA.getRadius(), parentA, 0);
		}

		CelestialBody commonAncestor = OrbitUtil.findCommonAncestor(level.registryAccess(), parentA, parentB);
		if (commonAncestor == null)
			return new TransferOrbit.TransferData(100000, 20 * 60 * 60 * 2);

		double totalDv = 0;
		double totalTime = 0;

		// FIXED: Added missing denominator parentheses
		double rOrigin = origin.getOrbitalAltitude() + parentA.getRadius();
		double originVel = Math.sqrt(EnvironmentUtil.getGravitationalParameter(parentA) / rOrigin);
		double escapeVelA = originVel * Math.sqrt(2.0);
		totalDv += (escapeVelA - originVel);

		double r1 = OrbitUtil.getRadiusRelativeToAncestor(level, parentA, commonAncestor);
		double r2 = OrbitUtil.getRadiusRelativeToAncestor(level, parentB, commonAncestor);

		double error = calculateTransferWindowError(level.getGameTime(), parentA, parentB);
		TransferOrbit.TransferData interplanetary = getHohmann(r1, r2, commonAncestor, error);

		totalDv += interplanetary.deltaV;
		totalTime += interplanetary.transferTime;

		// FIXED: Added missing denominator parentheses
		double rDest = destination.getOrbitalAltitude() + parentB.getRadius();
		double destVel = Math.sqrt(EnvironmentUtil.getGravitationalParameter(parentB) / rDest);
		double escapeVelB = destVel * Math.sqrt(2.0);
		totalDv += (escapeVelB - destVel);

		return new TransferOrbit.TransferData(totalTime, totalDv);
	}

	private static TransferOrbit.TransferData getHohmann(double r1, double r2, CelestialBody parent, double phaseErrorRads) {
		double mu = EnvironmentUtil.getGravitationalParameter(parent);

		double dv1 = Math.sqrt(mu / r1) * (Math.sqrt((2.0 * r2) / (r1 + r2)) - 1.0);
		double dv2 = Math.sqrt(mu / r2) * (1.0 - Math.sqrt((2.0 * r1) / (r1 + r2)));
		double baseDv = Math.abs(dv1) + Math.abs(dv2);

		double a = (r1 + r2) / 2.0;
		double baseTime = Math.PI * Math.sqrt(Math.pow(a, 3) / mu);

		double maxPenaltyMultiplier = 3.0;
		double penalty = 1.0 + ((Math.abs(phaseErrorRads) / Math.PI) * (maxPenaltyMultiplier - 1.0));

		double finalDv = baseDv * penalty;
		double finalTimeReal = baseTime * penalty;
		double finalTimeGameplay = finalTimeReal * TIME_SCALE;

		return new TransferOrbit.TransferData(finalTimeGameplay, finalDv);
	}

	private static double calculateTransferWindowError(long time, CelestialBody startPlanet, CelestialBody endPlanet)
	{
		double r1 = startPlanet.getOrbit().getOrbitalAltitude() + startPlanet.getRadius();
		double r2 = endPlanet.getOrbit().getOrbitalAltitude() + endPlanet.getRadius();

		double startAngle = Math.toRadians(startPlanet.getOrbit().getAngle(time));
		double endAngle = Math.toRadians(endPlanet.getOrbit().getAngle(time));

		double idealPhaseAngle = Math.PI * (1.0 - Math.sqrt(Math.pow(r1 + r2, 3) / (8.0 * Math.pow(r2, 3))));
		double currentPhaseAngle = Math.abs(startAngle - endAngle);

		if (currentPhaseAngle > Math.PI)
			currentPhaseAngle = (2.0 * Math.PI) - currentPhaseAngle;

		return Math.abs(idealPhaseAngle - currentPhaseAngle);
	}

	public static int deltaVToFuelMass(Stage stage, double deltaV)
	{
		double payloadMass = 0;
		boolean foundSelf = false;

		for (Stage s : stage.getVessel().getStages())
		{
			if (foundSelf)
				payloadMass += s.getTotalMass();
			else if (s == stage)
				foundSelf = true;
		}

		double initialMass = payloadMass + stage.getTotalMass();
		double massWithoutDeltaV = initialMass * Math.exp(-(deltaV / (stage.getAverageIsp() * 9.81)));
		return (int) (initialMass - massWithoutDeltaV);
	}

	public static int deltaVToFuelMass(double totalMass, double Isp, double deltaV)
	{
		double massWithoutDeltaV = totalMass*Math.pow(2.718, -(deltaV/(Isp*9.81)));
		return (int) (totalMass-massWithoutDeltaV);
	}

	public static void gravityAffect(Entity entity)
	{
		if(!entity.isNoGravity() && !(entity instanceof LivingEntity living && living.isFallFlying())
				   && !entity.isInWater() && !entity.isInLava()
				   && !entity.isSwimming() && !entity.isDescending())
		{
			if(entity.level().dimension().equals(Level.OVERWORLD))
				return;

			if(entity instanceof Player player)
			{
				if(player.getAbilities().flying)
					return;
			}
			double gravity = entity.getGravity();

			CelestialBody body = OrbitUtil.getCelestialBody(entity.level());
			if(body != null)
			{
				double localGravity = EnvironmentUtil.getGravity(body)*gravity;
				double reverseAccell = gravity-localGravity;
				entity.addDeltaMovement(new Vec3(0, reverseAccell, 0));

				entity.fallDistance = (float) (entity.fallDistance * EnvironmentUtil.getGravity(body));
			}
		}
	}
}