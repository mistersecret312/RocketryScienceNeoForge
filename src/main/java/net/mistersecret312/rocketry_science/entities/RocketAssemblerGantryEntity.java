package net.mistersecret312.rocketry_science.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.rocketry_science.init.EntityDataSerializersInit;
import net.mistersecret312.rocketry_science.init.EntityInit;
import net.mistersecret312.rocketry_science.vessel.Rocket;

import java.util.LinkedHashSet;

public class RocketAssemblerGantryEntity extends Entity
{
	private static final EntityDataAccessor<Float> X_WIDTH =
			SynchedEntityData.defineId(RocketAssemblerGantryEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> Z_WIDTH =
			SynchedEntityData.defineId(RocketAssemblerGantryEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> HEIGHT =
			SynchedEntityData.defineId(RocketAssemblerGantryEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> PROGRESS =
			SynchedEntityData.defineId(RocketAssemblerGantryEntity.class, EntityDataSerializers.FLOAT);

	public RocketAssemblerGantryEntity(EntityType<RocketAssemblerGantryEntity> type, Level level)
	{
		super(type, level);
	}

	public RocketAssemblerGantryEntity(Level level)
	{
		super(EntityInit.ASSEMBLER_GANTRY.get(), level);
	}


	public void setXWidth(float xWidth)
	{
		this.entityData.set(X_WIDTH, xWidth);
	}

	public void setHeight(float height)
	{
		this.entityData.set(HEIGHT, height);
	}

	public void setZWidth(float zWidth)
	{
		this.entityData.set(Z_WIDTH, zWidth);
	}

	public void setProgress(float progress)
	{
		this.entityData.set(PROGRESS, progress);
	}

	public float getXWidth()
	{
		return this.entityData.get(X_WIDTH);
	}

	public float getZWidth()
	{
		return this.entityData.get(Z_WIDTH);
	}

	public float getHeight()
	{
		return this.entityData.get(HEIGHT);
	}

	public float getProgress()
	{
		return this.entityData.get(PROGRESS);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder)
	{
		builder.define(X_WIDTH, 0f);
		builder.define(Z_WIDTH, 0f);
		builder.define(HEIGHT, 0f);
		builder.define(PROGRESS, 0f);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag)
	{
		this.entityData.set(X_WIDTH, tag.getFloat("x_width"));
		this.entityData.set(Z_WIDTH, tag.getFloat("z_width"));
		this.entityData.set(HEIGHT, tag.getFloat("height"));
		this.entityData.set(PROGRESS, tag.getFloat("progress"));

	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag)
	{
		tag.putFloat("x_width", this.entityData.get(X_WIDTH));
		tag.putFloat("z_width", this.entityData.get(Z_WIDTH));
		tag.putFloat("height", this.entityData.get(HEIGHT));
		tag.putFloat("progress", this.entityData.get(PROGRESS));
	}

	@Override
	public AABB makeBoundingBox()
	{
		AABB box = new AABB(position(), position()).inflate(getXWidth()/2f, 0, getZWidth()/2f).expandTowards(0, getHeight(), 0);

		return box;
	}
}
