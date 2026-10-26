package net.mistersecret312.rocketry_science.client.screen.spacemap;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class MapCelestialBody
{
	public final CelestialBody body;
	public final List<MapCelestialBody> children = new ArrayList<>();

	public double cullRadius;

	public MapCelestialBody(CelestialBody body, Level level)
	{
		this.body = body;

		double maxChildDistance = 0;
		for(CelestialBody child : OrbitUtil.getChildren(body, level))
		{
			MapCelestialBody childBody = new MapCelestialBody(child, level);
			children.add(childBody);

			Vector2d childPos = child.getOrbit().getVisualPosition(level.getGameTime());
			double dist = childPos.length();

			if (dist > maxChildDistance)
				maxChildDistance = dist;
		}

		if(body.getDisplayData().isPresent() && body.getDisplayData().get().collapseData().isPresent())
		{
			CelestialBody.DisplayData.CollapseData collapseData = body.getDisplayData().get().collapseData().get();
			maxChildDistance = Math.max(maxChildDistance, collapseData.systemSize());
		}

		this.cullRadius = maxChildDistance;
	}
}
