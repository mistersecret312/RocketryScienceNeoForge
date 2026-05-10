package net.mistersecret312.rocketry_science.vessel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.rocketry_science.init.BlockDataInit;
import net.mistersecret312.rocketry_science.init.BlockInit;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.mistersecret312.rocketry_science.util.OrbitalMath;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockDataType;
import net.mistersecret312.rocketry_science.vessel.block_data.FuelTankData;
import net.mistersecret312.rocketry_science.vessel.block_data.RocketEngineData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stage
{
	public List<BlockState> palette;
	public HashMap<BlockPos, BlockData> blocks;
	public List<FluidStack> fluidStacks;
	public List<FluidStack> maxFluids;

	public VesselData vessel;

	public Stage(VesselData vessel)
	{
		this.vessel = vessel;

		this.palette = new ArrayList<>();
		this.blocks = new HashMap<>();
		this.fluidStacks = new ArrayList<>();
		this.maxFluids = new ArrayList<>();
	}

	public Stage(VesselData vessel, List<BlockState> palette, HashMap<BlockPos, BlockData> blocks,
				 List<FluidStack> fluidStacks, List<FluidStack> maxFluids)
	{
		this.palette = palette;
		this.blocks = blocks;
		this.fluidStacks = fluidStacks;
		this.maxFluids = maxFluids;

		this.vessel = vessel;
	}

	public void tick(Level level)
	{
		if(this.blocks.isEmpty())
		{
			this.vessel.removeStage(this);
			return;
		}

		for (Map.Entry<BlockPos, BlockData> entry : blocks.entrySet())
		{
			BlockData data = entry.getValue();
			if (data.doesTick(level))
				data.tick(level);
		}
	}

	public void orbitalTick(MinecraftServer server)
	{
		if(this.blocks.isEmpty())
		{
			this.vessel.removeStage(this);
			return;
		}

		for (Map.Entry<BlockPos, BlockData> entry : blocks.entrySet())
		{
			BlockData data = entry.getValue();
			if (data.ticksInSpace(server))
				data.orbitalTick(server);
		}
	}

	public VesselData getVessel()
	{
		return vessel;
	}

	public double calculateDeltaV()
	{
		double averageIsp = getAverageIsp();
		if(getTotalDryMass() == 0)
			return 0;
		double massRatio = getTotalMass()/getTotalDryMass();
		double log = Math.log(massRatio);
		return OrbitUtil.getCelestialBody(getVessel().level()).getGravityMS2()*averageIsp*log;
	}

	public double getTotalMass()
	{
		double mass = 0;
		for(Map.Entry<BlockPos, BlockData> entry : this.blocks.entrySet())
		{
			mass += entry.getValue().getMass();
		}

		return mass;
	}

	public double getFuelMass()
	{
		return getTotalMass()-getTotalDryMass();
	}

	public int getFuelTypeAmount()
	{
		int amount = 0;
		for(Map.Entry<BlockPos, BlockData> entry : this.blocks.entrySet())
		{
			if(entry.getValue() instanceof RocketEngineData data)
			{
				amount = data.fuelType.getPropellants().size();
			}
		}

		return amount;
	}

	public double getTotalDryMass()
	{
		double mass = 0;
		for(Map.Entry<BlockPos, BlockData> entry : this.blocks.entrySet())
		{
			mass += entry.getValue().getDryMass();
		}

		return mass;
	}

	public void consumeFuelByDeltaV(double deltaV)
	{
		int fuelMass = OrbitalMath.deltaVToFuelMass(this, deltaV);
		consumption : while(fuelMass > 0)
		{
			for(Map.Entry<BlockPos, BlockData> entry : this.blocks.entrySet())
			{
				if(entry.getValue() instanceof FuelTankData tank)
				{
					int drained = tank.tank.drain(fuelMass/getFuelTypeAmount(), IFluidHandler.FluidAction.EXECUTE).getAmount();
					fuelMass -= drained;
					if(drained != 0)
						continue consumption;
				}
			}

			if(fuelMass > 0)
			{
				for(Map.Entry<BlockPos, BlockData> entry : this.blocks.entrySet())
				{
					if(entry.getValue() instanceof RocketEngineData engineData)
					{
						int drained = engineData.tank.drain(fuelMass/getFuelTypeAmount(), IFluidHandler.FluidAction.EXECUTE).getAmount();
						fuelMass -= drained;
						if(drained != 0)
							continue consumption;
					}
				}
			}

			if(fuelMass > 0)
				break;
		}
	}

	public double getAverageIsp()
	{
		double Isp = 0;
		int amount = 0;
		for(Map.Entry<BlockPos, BlockData> entry : this.blocks.entrySet())
		{
			if(entry.getValue() instanceof RocketEngineData data)
			{
				Isp += data.getIsp();
				amount++;
			}
		}
		if(amount == 0)
			return 0;

		return Isp/amount;
	}

	public void toNetwork(RegistryFriendlyByteBuf buffer)
	{
		buffer.writeCollection(this.palette, (writer, state) -> {
			writer.writeById(Block.BLOCK_STATE_REGISTRY::getId, state);
		});

		buffer.writeCollection(this.blocks.entrySet(), (writer, data) -> {
			writer.writeBlockPos(data.getKey());
			BlockDataInit.STREAM_CODEC.encode(buffer, data.getValue().getType());
			data.getValue().toNetwork((RegistryFriendlyByteBuf) writer);
		});

		buffer.writeCollection(fluidStacks, (encoder, stack) ->
			{
				FluidStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) encoder, stack);
			});
		buffer.writeCollection(maxFluids, (encoder, stack) ->
			{
				FluidStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) encoder, stack);
			});
	}

	public static Stage fromNetwork(RegistryFriendlyByteBuf buffer, VesselData vessel)
	{
		Stage stage = new Stage(vessel);

		List<BlockState> pallete = buffer.readCollection(ArrayList::new, reader -> reader.readById(Block.BLOCK_STATE_REGISTRY::byId));

		HashMap<BlockPos, BlockData> blocks = new HashMap<>();
		int sizeBlocks = buffer.readVarInt();
		for (int i = 0; i < sizeBlocks; i++)
		{
			BlockPos pos = buffer.readBlockPos();
			BlockDataType<?> type = BlockDataInit.STREAM_CODEC.decode(buffer);
			BlockData data = type.supplier.get();
			data.fromNetwork(buffer, pos, stage);
			blocks.put(pos, data);
		}
		List<FluidStack> fluidStacks = buffer.readCollection(ArrayList::new, (decoder) -> FluidStack.OPTIONAL_STREAM_CODEC.decode(
				(RegistryFriendlyByteBuf) decoder));
		List<FluidStack> maxFluids = buffer.readCollection(ArrayList::new, (decoder) -> FluidStack.OPTIONAL_STREAM_CODEC.decode(
				(RegistryFriendlyByteBuf) decoder));

		stage.vessel = vessel;
		stage.palette = pallete;
		stage.blocks = blocks;
		stage.fluidStacks = fluidStacks;
		stage.maxFluids = maxFluids;

		return stage;
	}

	public CompoundTag save()
	{
		CompoundTag tag = new CompoundTag();

		ListTag paletteTag = new ListTag();
		for(BlockState state : palette)
		{
			paletteTag.add(NbtUtils.writeBlockState(state));
		}
		tag.put("palette", paletteTag);

		ListTag blocksTag = new ListTag();
		for(Map.Entry<BlockPos, BlockData> entry : blocks.entrySet())
		{
			blocksTag.add(entry.getValue().save());
		}
		tag.put("blocks", blocksTag);

		ListTag storedFluidTag = new ListTag();
		ListTag maxFluidsTag = new ListTag();
		for(FluidStack stack : fluidStacks)
			storedFluidTag.add(stack.save(getVessel().level().registryAccess()));
		for(FluidStack stack : maxFluids)
			maxFluidsTag.add(stack.save(getVessel().level().registryAccess()));
		tag.put("stored_fluid", storedFluidTag);
		tag.put("max_fluid", maxFluidsTag);

		return tag;
	}

	public void load(CompoundTag tag, HolderLookup.Provider lookup)
	{
		ListTag paletteTag = tag.getList("palette", Tag.TAG_COMPOUND);
		List<BlockState> palette = new ArrayList<>();
		for(Tag listTag : paletteTag)
			palette.add(NbtUtils.readBlockState(lookup.lookupOrThrow(Registries.BLOCK), (CompoundTag) listTag));
		this.palette = palette;

		ListTag blocksTag = tag.getList("blocks", Tag.TAG_COMPOUND);
		HashMap<BlockPos, BlockData> blocks = new HashMap<>();
		for(Tag listTag : blocksTag)
		{
			CompoundTag listCompound = ((CompoundTag) listTag);
			ResourceLocation type = ResourceLocation.tryParse(listCompound.getString("type"));
			BlockDataType<?> dataType = BlockDataInit.REGISTRY.get(type);
			BlockData data = dataType.supplier.get();

			data.load(listCompound, this);
			data.initializeData(this);
			blocks.put(data.pos, data);
		}
		this.blocks = blocks;

		ListTag storedFluidTag = tag.getList("stored_fluid", Tag.TAG_COMPOUND);
		List<FluidStack> storedFluid = new ArrayList<>();
		for(Tag listTag : storedFluidTag)
		{
			storedFluid.add(FluidStack.OPTIONAL_CODEC.parse(getVessel().level().registryAccess().createSerializationContext(
					NbtOps.INSTANCE), listTag).getOrThrow());
		}

		ListTag maxStoredFluidTag = tag.getList("max_stored_fluid", Tag.TAG_COMPOUND);
		List<FluidStack> maxStoredFluid = new ArrayList<>();
		for(Tag listTag : maxStoredFluidTag)
		{
			maxStoredFluid.add(FluidStack.OPTIONAL_CODEC.parse(getVessel().level().registryAccess().createSerializationContext(
					NbtOps.INSTANCE), listTag).getOrThrow());
		}

		this.fluidStacks = storedFluid;
		this.maxFluids = maxStoredFluid;
	}
}
