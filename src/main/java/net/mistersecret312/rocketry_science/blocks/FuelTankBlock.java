package net.mistersecret312.rocketry_science.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.rocketry_science.block_entities.fuel_tank.FuelTankBlockEntity;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import net.mistersecret312.rocketry_science.util.ConnectivityHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FuelTankBlock extends BaseEntityBlock
{
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);

    public static final MapCodec<FuelTankBlock> CODEC = simpleCodec(FuelTankBlock::new);

    public int capacityPerFluid;

    public FuelTankBlock(Properties properties)
    {
        super(properties);
    }

    public FuelTankBlock(Properties pProperties, int capacityPerFluid)
    {
        super(pProperties);
        this.capacityPerFluid = capacityPerFluid;
        registerDefaultState(this.defaultBlockState().setValue(TOP, true).setValue(BOTTOM, true).setValue(SHAPE, Shape.SINGLE));
    }

    public static boolean isTank(BlockState state)
    {
        return state.getBlock() instanceof FuelTankBlock;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock())
            return;
        if (moved)
            return;
        Optional.ofNullable(world.getBlockEntity(pos)).ifPresent(blockEntity -> {
            if(blockEntity instanceof FuelTankBlockEntity fuelTank)
                fuelTank.updateConnectivity();
        });
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        if(level.isClientSide())
            return ItemInteractionResult.SUCCESS;

        FuelTankBlockEntity fuelTank = ConnectivityHandler.partAt(BlockEntityInit.FUEL_TANK.get(), level, pos).getControllerBE();

        if (player.getItemInHand(hand).getItem() instanceof BucketItem bucket)
        {
            FluidStack bucketStack = new FluidStack(bucket.content, 1000);
            for (int tankI = 0; tankI < fuelTank.getTankInventory().getTanks(); tankI++)
            {
                IFluidTank tank = fuelTank.getTank(tankI);
                if (tank != null && tank.isFluidValid(bucketStack))
                {
                    tank.fill(bucketStack, IFluidHandler.FluidAction.EXECUTE);
                    fuelTank.onFluidStackChanged(bucketStack);
                    fuelTank.setChanged();
                    return ItemInteractionResult.SUCCESS;
                }
            }

            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof FuelTankBlockEntity tankBE))
                return;
            world.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(tankBE);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(TOP, BOTTOM, SHAPE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return BlockEntityInit.FUEL_TANK.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return createTickerHelper(blockEntity, BlockEntityInit.FUEL_TANK.get(), FuelTankBlockEntity::tick);
    }


    public enum Shape implements StringRepresentable
    {
        SINGLE, DOUBLE, TRIPLE_EDGE, TRIPLE_CENTER, TRIPLE_CORNER;

        Shape() {}

        public String getSerializedName()
        {
            return this.name().toLowerCase();
        }
    }
}
