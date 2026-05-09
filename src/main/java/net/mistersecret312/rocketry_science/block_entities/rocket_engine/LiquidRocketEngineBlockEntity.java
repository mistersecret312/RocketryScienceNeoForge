package net.mistersecret312.rocketry_science.block_entities.rocket_engine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.rocketry_science.block_entities.fuel_tank.RocketFuelTank;
import net.mistersecret312.rocketry_science.blocks.NozzleBlock;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import net.mistersecret312.rocketry_science.items.CombustionChamberItem;
import net.mistersecret312.rocketry_science.items.TurboPumpItem;
import net.mistersecret312.rocketry_science.util.RocketFuel;
import net.mistersecret312.rocketry_science.util.RocketMaterial;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.mistersecret312.rocketry_science.blocks.CombustionChamberBlock.FACING;


public class LiquidRocketEngineBlockEntity extends RocketEngineBlockEntity
{
    public RocketFuelTank fuelTank = createTank();
    public IFluidHandler fluidHolder;

    public ItemStackHandler handler = createHandler();

    public RocketFuel rocketFuel;

    public LiquidRocketEngineBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockEntityInit.ROCKET_ENGINE.get(), pos, state);
    }

    @Override
    public void onLoad()
    {
        this.fluidHolder = fuelTank;
        super.onLoad();
    }

    public void refreshCapability() {
        fluidHolder = fuelTank;
        invalidateCapabilities();
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        for (int i = 0; i < handler.getSlots(); i++)
        {
            if(handler.getStackInSlot(i).isEmpty())
            {
                this.efficiency = 0;
                this.thrust = 0;
                this.mass = 0;
                this.rocketFuel = null;
            }
        }
        double materialEfficiencyMult = 0;
        double materialThrustMult = 0;
        for (int i = 0; i < handler.getSlots(); i++)
        {
            ItemStack stack = handler.getStackInSlot(i);
            if(stack.getItem() instanceof CombustionChamberItem chamber)
            {
                RocketFuel fuelType = chamber.getFuelType(stack);
                RocketMaterial material = chamber.getMaterial(stack);

                if(fuelType == null || material == null)
                    return;

                this.rocketFuel = fuelType;
                this.setMass(2500*material.getMassCoefficient());
                this.setThrust(fuelType.getThrustKiloNewtons());
                this.setEfficiency(fuelType.getEfficiency());
                materialEfficiencyMult += material.getEfficiencyCoefficient();
                materialThrustMult += material.getThrustCoefficient();
            }
            else if(stack.getItem() instanceof TurboPumpItem pump)
            {
                RocketMaterial material = pump.getMaterial(stack);

                if(material == null)
                    return;

                this.setMass(this.getMass()+(1000*material.getMassCoefficient()));
                materialThrustMult += material.getThrustCoefficient();
                materialEfficiencyMult += material.getEfficiencyCoefficient();
            }
        }
        this.setThrust(this.getThrust()*(materialThrustMult/3));
        this.setEfficiency(this.getEfficiency()*(materialEfficiencyMult/3));
    }

    public boolean isVacuum()
    {
        if(getNozzle() != null && getNozzle().getBlock() instanceof NozzleBlock nozzle)
            return nozzle.isVacuum();

        return false;
    }

    @Override
    public boolean hasPropellantMixture()
    {
        RocketFuel fuelType = rocketFuel;

        List<Boolean> hasFuel = new ArrayList<>();
        hasFuel.add(this.fuelTank.getPropellants().stream().allMatch(fluidStack -> fluidStack.getFluidAmount() > 0));
        for (int i = 0; i < this.fuelTank.getTanks(); i++)
            hasFuel.add(fuelType.getPropellants().get(i).test(this.fuelTank.getFluidInTank(i)));

        return hasFuel.stream().allMatch(bool -> bool);
    }

    @Override
    public @Nullable BlockState getNozzle()
    {
        BlockPos nozzlePos = this.getBlockPos().relative(this.getBlockState().getValue(FACING).getOpposite());
        BlockState nozzleState = this.level.getBlockState(nozzlePos);
        if(nozzleState.getBlock() instanceof NozzleBlock)
        {
            if(nozzleState.getValue(NozzleBlock.FACING).equals(this.getBlockState().getValue(FACING)))
                return nozzleState;
        }
        return super.getNozzle();
    }

    @Override
    public @Nullable BlockPos getNozzlePos()
    {
        BlockPos nozzlePos = this.getBlockPos().relative(this.getBlockState().getValue(FACING).getOpposite());
        BlockState nozzleState = this.level.getBlockState(nozzlePos);
        if(nozzleState.getBlock() instanceof NozzleBlock)
        {
            if(nozzleState.getValue(NozzleBlock.FACING).equals(this.getBlockState().getValue(FACING)))
                return nozzlePos;
        }
        return super.getNozzlePos();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LiquidRocketEngineBlockEntity rocketEngine)
    {
        if(level.isClientSide())
            return;
        if(rocketEngine.rocketFuel == null || rocketEngine.thrust == 0 || rocketEngine.efficiency == 0)
            return;
        BlockPos nozzlePos = pos.relative(state.getValue(FACING).getOpposite());
        BlockState nozzleState = level.getBlockState(nozzlePos);
        if(nozzleState.getBlock() instanceof NozzleBlock && nozzleState.getValue(NozzleBlock.FACING).equals(state.getValue(FACING)))
        {
            rocketEngine.isBuilt = true;
            if(nozzleState.getValue(NozzleBlock.HOT) > 0 && level.getGameTime() % 400 == 0)
            {
                int targetHotness = Math.max(0, nozzleState.getValue(NozzleBlock.HOT) - 1);
                BlockState targetNozzleState = nozzleState.setValue(NozzleBlock.HOT, targetHotness);
                level.setBlock(nozzlePos, targetNozzleState, 2);
            }
        }
        else rocketEngine.isBuilt = false;
    }

    public int getFuelStored()
    {
        int stored = 0;
        for (int tank = 0; tank < this.fuelTank.getTanks(); tank++)
            stored += this.fuelTank.getFluidInTank(tank).getAmount();


        return stored;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup)
    {
        super.saveAdditional(tag, lookup);
        this.fuelTank.writeToNBT(tag, lookup);
        tag.put("chamber", this.handler.serializeNBT(lookup));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup)
    {
        super.loadAdditional(tag, lookup);
        this.fuelTank.readFromNBT(tag, lookup);
        this.handler.deserializeNBT(lookup, tag.getCompound("chamber"));
    }

    public RocketFuelTank createTank()
    {
        return new RocketFuelTank(RocketFuel.HYDROLOX.getPropellants(), COMBUSTION_CHAMBER_CAPACITY)
        {
            @Override
            protected void onContentsChanged()
            {
                setChanged();
            }
        };
    }

    public ItemStackHandler createHandler()
    {
        return new ItemStackHandler(3)
        {
            @Override
            protected void onContentsChanged(int slot)
            {
                super.onContentsChanged(slot);
                setChanged();
            }
        };
    }
}
