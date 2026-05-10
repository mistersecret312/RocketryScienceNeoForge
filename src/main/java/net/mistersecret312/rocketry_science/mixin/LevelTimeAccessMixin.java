package net.mistersecret312.rocketry_science.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelTimeAccess;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelTimeAccess.class)
public interface LevelTimeAccessMixin
{
	@Inject(method = "getTimeOfDay(F)F", at = @At("HEAD"), cancellable = true)
	default void timeOfDay(float partialTick, CallbackInfoReturnable<Float> cir)
	{
		LevelTimeAccess timeAccess = (LevelTimeAccess) (Object) this;
		if(!(timeAccess instanceof Level level))
			return;

		long dayLength = timeAccess.dayTime();
		CelestialBody body = OrbitUtil.getCelestialBody(level);
		if(body != null)
			dayLength = (long) body.getDayLength()*60*20;

		double d0 = Mth.frac((double)timeAccess.dimensionType().fixedTime().orElse(timeAccess.dayTime()) / (double)dayLength - (double)0.25F);
		double d1 = (double)0.5F - Math.cos(d0 * Math.PI) / (double)2.0F;
		cir.setReturnValue((float)(d0 * (double)2.0F + d1) / 3.0F);
	}
}
