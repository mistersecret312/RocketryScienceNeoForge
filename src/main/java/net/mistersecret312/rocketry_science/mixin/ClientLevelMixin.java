package net.mistersecret312.rocketry_science.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelTimeAccess;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin
{
	@Inject(method = "getSkyDarken(F)F", at = @At("HEAD"), cancellable = true)
	public void skyLight(float pPartialTick, CallbackInfoReturnable<Float> cir)
	{
		ClientLevel level = (ClientLevel) (Object) this;

		float f = level.getTimeOfDay(pPartialTick);
		float f1 = 1.0F - (Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
		f1 = Mth.clamp(f1, 0.0F, 1.0F);
		f1 = 1.0F - f1;

		CelestialBody body = OrbitUtil.getCelestialBody(level);
		if (body == null || body.hasAtmosphere())
		{
			f1 *= 1.0F - level.getRainLevel(pPartialTick) * 5.0F / 16.0F;
			f1 *= 1.0F - level.getThunderLevel(pPartialTick) * 5.0F / 16.0F;
		}
		
		float minAmbient;
		if (body != null && !body.hasAtmosphere())
		{
			minAmbient = 0.0F;
		}
		else
		{
			minAmbient = 0.2F * 1;
		}

		cir.setReturnValue(f1 * (1.0F - minAmbient) + minAmbient);
	}
}