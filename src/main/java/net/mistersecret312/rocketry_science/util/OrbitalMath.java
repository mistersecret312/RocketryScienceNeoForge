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

		double part1 = Math.sqrt(EnvironmentUtil.getGravitationalParameter(body)/initialHeight)*(Math.sqrt((2*targetHeight)/(initialHeight+targetHeight))-1);
		double part2 = Math.sqrt(EnvironmentUtil.getGravitationalParameter(body)/targetHeight)*(1-Math.sqrt((2*initialHeight)/(initialHeight+targetHeight)));

		return part1+part2;
	}

	public static TransferOrbit.TransferData calculateTransfer(Level level, ArtificialOrbit origin, ArtificialOrbit destination, double currentTime) {
		CelestialBody parentA = OrbitUtil.getCelestialBody(origin.getParent(), level);
		CelestialBody parentB = OrbitUtil.getCelestialBody(destination.getParent(), level);

		if (parentA.equals(parentB))
			return getHohmann(origin.getOrbitalAltitude()+parentA.getRadius(), destination.getOrbitalAltitude()+parentB.getRadius(), parentA, 0); // No window penalty for same parent

		CelestialBody commonAncestor = OrbitUtil.findCommonAncestor(level, parentA, parentB);
		if (commonAncestor == null) {
			return new TransferOrbit.TransferData(100000,20*60*60*2);
		}

		double totalDv = 0;
		double totalTime = 0;

		double originVel = Math.sqrt(EnvironmentUtil.getGravitationalParameter(parentA) / origin.getOrbitalAltitude()+parentA.getRadius());
		double escapeVelA = originVel * Math.sqrt(2);
		totalDv += (escapeVelA - originVel);

		double r1 = OrbitUtil.getRadiusRelativeToAncestor(level, parentA, commonAncestor);
		double r2 = OrbitUtil.getRadiusRelativeToAncestor(level, parentB, commonAncestor);

		double error = calculateTransferWindowError(level.getGameTime(), parentA, parentB, commonAncestor);
		TransferOrbit.TransferData interplanetary = getHohmann(r1, r2, commonAncestor, error);

		totalDv += interplanetary.deltaV;
		totalTime += interplanetary.transferTime;

		double destVel = Math.sqrt(EnvironmentUtil.getGravitationalParameter(parentB) / destination.getOrbitalAltitude()+parentB.getRadius());
		double escapeVelB = destVel * Math.sqrt(2);
		totalDv += (escapeVelB - destVel);

		return new TransferOrbit.TransferData(totalTime, totalDv);
	}

	private static TransferOrbit.TransferData getHohmann(double r1, double r2, CelestialBody parent, double phaseErrorRads) {
		double mu = EnvironmentUtil.getGravitationalParameter(parent);

		double dv1 = Math.sqrt(mu / r1) * (Math.sqrt((2 * r2) / (r1 + r2)) - 1);
		double dv2 = Math.sqrt(mu / r2) * (1 - Math.sqrt((2 * r1) / (r1 + r2)));
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

	private static double calculateTransferWindowError(long time, CelestialBody startPlanet, CelestialBody endPlanet, CelestialBody commonParent) {
		double r1 = startPlanet.getOrbit().getOrbitalAltitude()+startPlanet.getRadius();
		double r2 = endPlanet.getOrbit().getOrbitalAltitude()+endPlanet.getRadius();

		double idealPhaseAngle = Math.PI * (1 - Math.sqrt(Math.pow(r1 + r2, 3) / (8 * Math.pow(r2, 3))));
		double currentPhaseAngle = Math.abs(startPlanet.getOrbitAngle(time) - endPlanet.getOrbitAngle(time));

		if (currentPhaseAngle > Math.PI)
			currentPhaseAngle = (2 * Math.PI) - currentPhaseAngle;

		return Math.abs(idealPhaseAngle - currentPhaseAngle);
	}

	public static int deltaVToFuelMass(Stage stage, double deltaV)
	{
		double stageMass = stage.getTotalMass();
		double massWithoutDeltaV = stage.getTotalMass()*Math.pow(2.718, -(deltaV/(stage.getAverageIsp()*9.81)));

		return (int) (stageMass-massWithoutDeltaV);
	}

	public static void gravityAffect(Entity entity)
	{
		if(!entity.isNoGravity() && !(entity instanceof LivingEntity living && living.isFallFlying())
				   && !entity.isInWater() && !entity.isInLava()
				   && !entity.isSwimming() && !entity.isDescending())
		{
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