package net.mistersecret312.rocketry_science.footprint;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record Footprint(long packedPos, short subBlockPos, byte rotation, byte variant)
{
	public BlockPos toBlockPos()
	{
		return BlockPos.of(packedPos);
	}

	public float getSubBlockX()
	{
		return (subBlockPos & 0x0F) / 16.0f;
	}

	public float getSubBlockY()
	{
		return ((subBlockPos >> 4) & 0x0F) / 16.0f;
	}

	public float getSubBlockZ()
	{
		return ((subBlockPos >> 8) & 0x0F) / 16.0f;
	}

	public float getRotationDegrees()
	{
		return (rotation & 0xFF) * (360f/255f);
	}

	public static byte packRotation(float yaw)
	{
		float normalized = (yaw % 360f + 360f) % 360f;
		return (byte) Math.round((normalized / 360f) * 255f);
	}

	public static byte[] encode(List<Footprint> list)
	{
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeVarInt(list.size());
		for (Footprint fp : list)
		{
			buf.writeLong(fp.packedPos());
			buf.writeShort(fp.subBlockPos());
			buf.writeByte(fp.rotation());
			buf.writeByte(fp.variant());
		}

		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		buf.release();
		return bytes;
	}

	public static List<Footprint> decode(byte[] bytes)
	{
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
		int size = buf.readVarInt();
		List<Footprint> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
			list.add(new Footprint(buf.readLong(), buf.readShort(), buf.readByte(), buf.readByte()));

		buf.release();
		return list;
	}
}
