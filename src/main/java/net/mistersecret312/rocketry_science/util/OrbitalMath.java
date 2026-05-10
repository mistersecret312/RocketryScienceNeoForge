package net.mistersecret312.rocketry_science.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.vessel.Stage;

public class OrbitalMath
{
	public static double getOrbitDeltaV(CelestialBody body, double height)
	{
		double altitude = body.getRadius()+height;
		return Math.sqrt(body.getGravitationalParameter()/altitude);
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

		double part1 = Math.sqrt(body.getGravitationalParameter()/initialHeight)*(Math.sqrt((2*targetHeight)/(initialHeight+targetHeight))-1);
		double part2 = Math.sqrt(body.getGravitationalParameter()/targetHeight)*(1-Math.sqrt((2*initialHeight)/(initialHeight+targetHeight)));

		return part1+part2;
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
				double localGravity = body.getGravity()*gravity;
				double reverseAccell = gravity-localGravity;
				entity.addDeltaMovement(new Vec3(0, reverseAccell, 0));

				entity.fallDistance = (float) (entity.fallDistance * body.getGravity());
			}
		}
	}
}