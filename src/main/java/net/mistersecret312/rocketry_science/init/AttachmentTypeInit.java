package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.room.RoomManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class AttachmentTypeInit
{
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
			NeoForgeRegistries.Keys.ATTACHMENT_TYPES, RocketryScience.MODID);

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<RoomManager>> ROOM_MANAGER =
			ATTACHMENT_TYPES.register("room_manager",
					() -> AttachmentType.builder(RoomManager::new)
										.serialize(new IAttachmentSerializer<CompoundTag, RoomManager>() {
											@Override
											public RoomManager read(IAttachmentHolder holder, CompoundTag tag,
																				HolderLookup.Provider provider)
											{
												RoomManager capability = new RoomManager();
												if(holder instanceof Level level)
													capability.level = level;
												capability.deserializeNBT(provider, tag);
												return capability;
											}

											@Override
											public @Nullable CompoundTag write(RoomManager attachment,
																			   HolderLookup.Provider provider)
											{
												return attachment.serializeNBT(provider);
											}
										})
										.sync(RoomManager.STREAM_CODEC)
										.build());

	public static void register(IEventBus bus)
	{
		ATTACHMENT_TYPES.register(bus);
	}
}
