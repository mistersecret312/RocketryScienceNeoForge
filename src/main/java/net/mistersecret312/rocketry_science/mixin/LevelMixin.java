package net.mistersecret312.rocketry_science.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelTimeAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelMixin
{
	@Shadow public abstract float getRainLevel(float pDelta);

	@Shadow public abstract float getThunderLevel(float pDelta);

	@Shadow private int skyDarken;

	@Inject(method = "updateSkyBrightness()V", at = @At("HEAD"), cancellable = true)
	public void skyBrightness(CallbackInfo ci)
	{
		double weatherFactor = 1.0D;
		Level level = ((Level) (Object) this);

		CelestialBody body = OrbitUtil.getCelestialBody(level);
		if (body != null && body.hasAtmosphere())
		{
			double rain = 1.0D - (double)(this.getRainLevel(1.0F) * 5.0F) / 16.0D;
			double thunder = 1.0D - (double)(this.getThunderLevel(1.0F) * 5.0F) / 16.0D;
			weatherFactor = rain * thunder;
		}

		double sunHeight = Mth.cos(((LevelTimeAccess) this).getTimeOfDay(1.0F) * ((float)Math.PI * 2F));
		double dayLightFactor = 0.5D + 2.0D * Mth.clamp(sunHeight, -0.25D, 0.25D);

		double maxDarkness;
		if (body == null || !body.hasAtmosphere())
		{
			maxDarkness = 11.0D;
		}
		else
		{
			maxDarkness = 15.0D;
		}

		this.skyDarken = (int)((1.0D - dayLightFactor * weatherFactor) * maxDarkness);

		ci.cancel();
	}
}