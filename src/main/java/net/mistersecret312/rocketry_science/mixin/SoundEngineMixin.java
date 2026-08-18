package net.mistersecret312.rocketry_science.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.data.room.Room;
import net.mistersecret312.rocketry_science.data.room.RoomManager;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.init.AttachmentTypeInit;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin
{
    @Shadow @Final private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;
    @Shadow protected abstract float calculateVolume(SoundInstance instance);

    private boolean wasInVacuum = false;

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void rocketry$abortVacuumSounds(SoundInstance instance, CallbackInfo ci)
    {
        if (instance.getAttenuation() == SoundInstance.Attenuation.LINEAR && isCurrentlyInVacuum())
        {
            if (!(instance instanceof TickableSoundInstance))
                ci.cancel();
        }
    }

    @Inject(method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F", at = @At("HEAD"), cancellable = true)
    private void rocketry$silenceTickableSounds(SoundInstance instance, CallbackInfoReturnable<Float> cir)
    {
        if (instance.getAttenuation() == SoundInstance.Attenuation.LINEAR && isCurrentlyInVacuum())
            cir.setReturnValue(0.0F);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void rocketry$updateActiveSoundsOnTransition(boolean isGamePaused, CallbackInfo ci)
    {
        if (isGamePaused)
            return;

        boolean isInVacuum = isCurrentlyInVacuum();
        if (isInVacuum != this.wasInVacuum)
        {
            this.wasInVacuum = isInVacuum;
            this.instanceToChannel.forEach((instance, handle) -> {
                if (instance.getAttenuation() == SoundInstance.Attenuation.LINEAR)
                {
                    handle.execute(source -> source.setVolume(isInVacuum ? 0.0F : this.calculateVolume(instance)));
                }
            });
        }
    }

    private boolean isCurrentlyInVacuum()
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return false;
        Level level = mc.player.level();

        CelestialBody body = OrbitUtil.getCelestialBody(level);
        if(body == null)
            return false;
        RoomManager manager = level.getData(AttachmentTypeInit.ROOM_MANAGER);
        Optional<Room> roomOptional = manager.getRoomAt(mc.player.blockPosition());
        if(roomOptional.isPresent())
            return roomOptional.get().getCurrentOxygen() == 0;

        return !body.hasAtmosphere();
    }
}