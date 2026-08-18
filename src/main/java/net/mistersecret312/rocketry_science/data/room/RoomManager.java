package net.mistersecret312.rocketry_science.data.room;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.rocketry_science.init.AttachmentTypeInit;
import net.mistersecret312.rocketry_science.util.EnvironmentUtil;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;

public class RoomManager implements INBTSerializable<CompoundTag>
{
    public static final StreamCodec<RegistryFriendlyByteBuf, RoomManager> STREAM_CODEC = StreamCodec.of(
            (buf, attach) -> buf.writeNbt(attach.serializeNBT(buf.registryAccess())), (buf) ->
                {
                    RoomManager attachment = new RoomManager();
                    CompoundTag tag = buf.readNbt();
                    if(tag != null) attachment.deserializeNBT(buf.registryAccess(), tag);
                    return attachment;
                });

    public Level level;
    private final Map<UUID, Room> rooms = new HashMap<>();
    private final List<Scanner> activeScans = new ArrayList<>();
    private final List<Decompression> activeDecompressions = new ArrayList<>();

    private final Long2ObjectOpenHashMap<Set<UUID>> spatialIndex = new Long2ObjectOpenHashMap<>();

    private boolean isDirty = false;

    public RoomManager()
    {
        this.level = null;
    }

    public void tick()
    {
        if(level == null || level.isClientSide())
            return;

        Iterator<Decompression> decIt = activeDecompressions.iterator();
        while (decIt.hasNext())
        {
            Decompression decompression = decIt.next();
            if (decompression.tick((ServerLevel) level))
            {
                decIt.remove();
                destroyRoom(decompression.getRoom().getUUID());
            }
        }

        Iterator<Scanner> it = activeScans.iterator();
        while(it.hasNext())
        {
            Scanner scanner = it.next();
            boolean isFinished = scanner.tick();
            if(isFinished)
            {
                it.remove();
                if(scanner.hasFailed())
                {
                    if(scanner.getExpandingRoomUUID() != null)
                    {
                        Room room = rooms.get(scanner.getExpandingRoomUUID());
                        if(room != null)
                        {
                            BlockPos breachPos = scanner.getBreachPos();
                            if (room.getInteriorSet().size() <= 1)
                            {
                                long startLong = scanner.getStartPos().asLong();
                                Room ghostRoom = null;

                                for (Room r : rooms.values())
                                {
                                    if (r.isGhost() && r.getInteriorSet().contains(startLong))
                                    {
                                        ghostRoom = r;
                                        break;
                                    }
                                }

                                if (ghostRoom != null)
                                {
                                    triggerDecompression(ghostRoom, breachPos);
                                    destroyRoom(room.getUUID());
                                    continue;
                                }
                            }

                            triggerDecompression(room, breachPos);
                        }
                    }
                }
                else
                {
                    if(scanner.getExpandingRoomUUID() != null)
                    {
                        Optional<Room> existing = getStableRoomAt(scanner.getStartPos());
                        if (existing.isPresent() && !existing.get().getUUID().equals(scanner.getExpandingRoomUUID()))
                        {
                            destroyRoom(scanner.getExpandingRoomUUID());
                            continue;
                        }

                        flushScan(scanner);
                    }
                    else
                    {
                        if (getStableRoomAt(scanner.getStartPos()).isPresent())
                            continue;
                        createRoomFromScan(scanner);
                    }
                }
            }
        }

        Set<UUID> expandingRooms = new HashSet<>();
        for (Scanner scan : activeScans)
        {
            if (scan.getExpandingRoomUUID() != null)
                expandingRooms.add(scan.getExpandingRoomUUID());
        }

        List<UUID> deadRooms = new ArrayList<>();
        for(Room room : rooms.values())
        {
            room.tickEconomy((ServerLevel) level);
            if(room.getCurrentOxygen() <= 0 && room.getOxygenNodes().isEmpty() && !expandingRooms.contains(room.getUUID()))
                deadRooms.add(room.getUUID());
        }
        deadRooms.forEach(this::destroyRoom);

        if(activeScans.isEmpty())
        {
            List<UUID> ghostsToKill = new ArrayList<>();
            for (Room room : rooms.values())
            {
                if (room.isGhost())
                    ghostsToKill.add(room.getUUID());
            }
            if (!ghostsToKill.isEmpty())
                ghostsToKill.forEach(this::destroyRoom);
        }

        if(level != null && isDirty)
        {
            level.syncData(AttachmentTypeInit.ROOM_MANAGER);
            isDirty = false;
        }
    }

    public void markDirty()
    {
        this.isDirty = true;
    }

    private void triggerDecompression(Room room, BlockPos breachPos)
    {
        for(Decompression decompression : activeDecompressions)
        {
            if(decompression.getRoom().getUUID().equals(room.getUUID()))
                return;
        }

        if(room.getFilledPercentage() >= 0.2f && room.getInteriorSet().size() >= 100)
        {
            Leak field = Leak.generateFlowField((ServerLevel) level, breachPos, room.getInteriorSet());
            activeDecompressions.add(new Decompression(room, field, breachPos, 60));
        }
        makeRoomGhost(room.getUUID());
    }

    public void startScan(BlockPos startPos, float concentration, BlockPos causePos)
    {
        long startLong = startPos.asLong();
        if(getStableRoomAt(startPos).isPresent())
            return;

        for(Scanner scanner : activeScans)
            if(scanner.getVisited().contains(startLong))
                return;

        UUID newId = UUID.randomUUID();
        LongOpenHashSet initialInterior = new LongOpenHashSet();
        initialInterior.add(startLong);

        Room newRoom = new Room(newId, initialInterior, new LongOpenHashSet(), new LongOpenHashSet());
        newRoom.setCurrentOxygen(level, 100f * concentration);
        rooms.put(newId, newRoom);
        indexRoom(newId, newRoom);

        int startingVolume = 20000;
        int startingSpeed = 500;

        BlockEntity be = level.getBlockEntity(startPos);
        if (be instanceof IOxygenNode node)
        {
            startingVolume = Math.max(startingVolume, node.getBaseVolume());
            startingSpeed = Math.max(startingSpeed, node.getScanSpeed());
        }

        Scanner scanner = new Scanner((ServerLevel) level, startPos, startingVolume, true, startingSpeed, concentration, causePos);
        scanner.setExpandingRoomUUID(newId);
        activeScans.add(scanner);
    }

    private void createRoomFromScan(Scanner scanner)
    {
        long startPosLong = scanner.getVisited().iterator().nextLong();
        Optional<Room> existingRoom = getStableRoomAt(BlockPos.of(startPosLong));

        if(existingRoom.isPresent())
        {
            existingRoom.get().getOxygenNodes().addAll(scanner.getFoundNodes());
            return;
        }

        UUID newId = UUID.randomUUID();
        Room newRoom = new Room(newId, scanner.getVisited(), scanner.getBoundaries(), scanner.getFoundNodes());

        newRoom.setCurrentOxygen(level, scanner.getInitialConcentration());
        if(scanner.getCapturedEnvironment() != null)
        {
            newRoom.setTargetAtmosphere(level, scanner.getCapturedEnvironment().getPressureSeaLevel());
            newRoom.setTargetTemperature(level, scanner.getCapturedEnvironment().getTemperatureGradient().getMinTemp());
        }
        else
        {
            newRoom.setTargetAtmosphere(level, 1);
            newRoom.setTargetTemperature(level, 296);
        }

        rooms.put(newId, newRoom);
        indexRoom(newId, newRoom);
    }

    private void indexRoom(UUID roomID, Room room)
    {
        indexRoom(roomID, room.getInteriorSet(), room.getWallsSet());
    }

    private void indexRoom(UUID roomID, LongOpenHashSet interiorSet, LongOpenHashSet wallSet)
    {
        for(long posLong : interiorSet)
        {
            long chunkLong = ChunkPos.asLong(BlockPos.getX(posLong) >> 4, BlockPos.getZ(posLong) >> 4);
            spatialIndex.computeIfAbsent(chunkLong, k -> new HashSet<>()).add(roomID);
        }

        for(long posLong : wallSet)
        {
            long chunkLong = ChunkPos.asLong(BlockPos.getX(posLong) >> 4, BlockPos.getZ(posLong) >> 4);
            spatialIndex.computeIfAbsent(chunkLong, k -> new HashSet<>()).add(roomID);
        }
        if(level != null) markDirty();
    }

    public Optional<Room> getRoomAt(BlockPos pos)
    {
        long posLong = pos.asLong();
        Optional<Room> stableRoom = getStableRoomAt(pos);

        if (stableRoom.isPresent())
            return stableRoom;

        for (Room room : rooms.values())
        {
            if (room.isGhost() && room.getInteriorSet().contains(posLong))
                return Optional.of(room);
        }

        return Optional.empty();
    }

    public Optional<Room> getStableRoomAt(BlockPos pos)
    {
        long chunkLong = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        Set<UUID> roomsInChunk = spatialIndex.get(chunkLong);

        if (roomsInChunk == null || roomsInChunk.isEmpty())
            return Optional.empty();

        long posLong = pos.asLong();
        for (UUID roomId : roomsInChunk)
        {
            Room room = rooms.get(roomId);
            if (room != null && !room.isGhost() && room.getInteriorSet().contains(posLong))
                return Optional.of(room);
        }
        return Optional.empty();
    }

    private void makeRoomGhost(UUID roomId)
    {
        Room room = rooms.get(roomId);
        if (room != null)
        {
            room.setGhost(true);
            spatialIndex.values().forEach(set -> set.remove(roomId));
            markDirty();
        }
    }
    public Room getRoom(UUID uuid)
    {
        return rooms.get(uuid);
    }

    public void onBlockStateChanged(BlockPos pos, BlockState oldState, BlockState newState)
    {
        if(level == null || level.isClientSide())
            return;

        long chunkLong = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        Set<UUID> roomsInChunk = spatialIndex.get(chunkLong);
        if(roomsInChunk == null || roomsInChunk.isEmpty())
            return;
        long posLong = pos.asLong();

        boolean oldIsPermeable = EnvironmentUtil.isPermeable(oldState);
        boolean newIsPermeable = EnvironmentUtil.isPermeable(newState);

        VoxelShape oldShape = oldIsPermeable ? Shapes.empty() : oldState.getCollisionShape(level, pos);
        VoxelShape newShape = newIsPermeable ? Shapes.empty() : newState.getCollisionShape(level, pos);

        if(oldShape.equals(newShape))
            return;

        List<UUID> toSplit = new ArrayList<>();
        List<UUID> toExpand = new ArrayList<>();

        for(UUID roomId : roomsInChunk)
        {
            Room room = rooms.get(roomId);
            if(room == null)
                continue;

            if(room.getInteriorSet().contains(posLong))
                toSplit.add(roomId);
            else if(room.getWallsSet().contains(posLong))
                toExpand.add(roomId);
        }

        for(UUID roomId : toSplit)
        {
            Room oldRoom = rooms.get(roomId);
            if(oldRoom != null)
            {
                float concentration = oldRoom.getVolume() > 0 ? (oldRoom.getCurrentOxygen() / oldRoom.getVolume()) : 0f;

                LongOpenHashSet oldInterior = oldRoom.getInteriorSet();
                long[] nodesToRescan = oldRoom.getOxygenNodes().toLongArray();

                makeRoomGhost(roomId);

                for(Direction dir : Direction.values())
                {
                    BlockPos adj = pos.relative(dir);
                    if(oldInterior.contains(adj.asLong()))
                        startScan(adj, concentration, pos);
                }

                for(long nodePosLong : nodesToRescan)
                    startScan(BlockPos.of(nodePosLong), concentration, pos);
            }
        }

        if (!toExpand.isEmpty())
        {
            UUID primaryRoomId = toExpand.getFirst();
            Room primaryRoom = rooms.get(primaryRoomId);

            for (int i = 1; i < toExpand.size(); i++)
            {
                UUID otherRoomId = toExpand.get(i);
                mergeRooms(primaryRoom, rooms.get(otherRoomId));
            }
            startExpansionScan(primaryRoomId, pos);
        }
    }

    private void mergeRooms(Room primary, Room secondary)
    {
        if(primary == null || secondary == null || primary.getUUID().equals(secondary.getUUID())) return;

        primary.setCurrentOxygen(level, primary.getCurrentOxygen() + secondary.getCurrentOxygen());
        primary.setVolume(primary.getVolume() + secondary.getVolume());

        primary.getInteriorSet().addAll(secondary.getInteriorSet());
        primary.getWallsSet().addAll(secondary.getWallsSet());
        primary.getOxygenNodes().addAll(secondary.getOxygenNodes());

        for(Set<UUID> chunkRooms : spatialIndex.values())
        {
            if(chunkRooms.contains(secondary.getUUID()))
            {
                chunkRooms.remove(secondary.getUUID());
                chunkRooms.add(primary.getUUID());
            }
        }
        rooms.remove(secondary.getUUID());
        if(level != null) markDirty();
    }

    public void startExpansionScan(UUID roomId, BlockPos startPos)
    {
        Room room = rooms.get(roomId);
        if (room == null)
            return;

        long startLong = startPos.asLong();
        for (Scanner active : activeScans)
        {
            if (roomId.equals(active.getExpandingRoomUUID()) && active.getVisited().contains(startLong))
                return;
        }

        int inheritedMaxVolume = 20000;
        int inheritedSpeed = 500;
        boolean firstNode = true;

        for (long nodePosLong : room.getOxygenNodes())
        {
            BlockPos nodePos = BlockPos.of(nodePosLong);
            if (level.isLoaded(nodePos))
            {
                BlockEntity be = level.getBlockEntity(nodePos);
                if (be instanceof IOxygenNode node)
                {
                    if (firstNode)
                    {
                        inheritedMaxVolume = Math.max(inheritedMaxVolume, node.getBaseVolume());
                        inheritedSpeed = Math.max(inheritedSpeed, node.getScanSpeed());
                        firstNode = false;
                    }
                    else inheritedMaxVolume += node.getVolumeBonus();
                }
            }
        }

        Scanner scanner = new Scanner((ServerLevel) level, startPos, inheritedMaxVolume,
                false, inheritedSpeed, room.getCurrentOxygen(), startPos);
        scanner.setExpandingRoomUUID(roomId);

        scanner.getVisited().addAll(room.getInteriorSet());
        scanner.getBoundaries().addAll(room.getWallsSet());
        scanner.getFoundNodes().addAll(room.getOxygenNodes());

        activeScans.add(scanner);
    }

    private void destroyRoom(UUID roomId)
    {
        rooms.remove(roomId);
        spatialIndex.values().forEach(set -> set.remove(roomId));
        markDirty();
    }

    private void flushScan(Scanner scanner)
    {
        if (scanner.getExpandingRoomUUID() == null)
            return;
        Room room = rooms.get(scanner.getExpandingRoomUUID());
        if (room == null)
            return;

        int blocksAdded = 0;
        for (long pos : scanner.getVisited())
        {
            if (room.getInteriorSet().add(pos))
                blocksAdded++;
        }

        room.getWallsSet().addAll(scanner.getBoundaries());
        room.getOxygenNodes().addAll(scanner.getFoundNodes());
        room.setVolume(room.getInteriorSet().size() * 100f);

        if(scanner.getCapturedEnvironment() != null)
        {
            room.setTargetAtmosphere(level, scanner.getCapturedEnvironment().pressure);
            room.setTargetTemperature(level, scanner.getCapturedEnvironment().getTemperatureGradient().getMinTemp());
        }

        if (scanner.isProportional())
        {
            float conc = scanner.getInitialConcentration();
            if(conc <= 0f && !room.getOxygenNodes().isEmpty())
                conc = 0.02f;

            float addedOxygen = blocksAdded * 100f * conc;
            room.setCurrentOxygen(level, Math.min(room.getCurrentOxygen() + addedOxygen, room.getVolume()));
        }
        else room.setCurrentOxygen(level, Math.min(room.getCurrentOxygen(), room.getVolume()));

        indexRoom(room.getUUID(), scanner.getVisited(), scanner.getBoundaries());
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider)
    {
        CompoundTag tag = new CompoundTag();
        ListTag roomsTag = new ListTag();

        for(Room room : rooms.values())
        {
            CompoundTag roomTag = new CompoundTag();
            roomTag.putUUID("uuid", room.getUUID());
            roomTag.putLongArray("interior", room.getInteriorSet().toLongArray());
            roomTag.putLongArray("walls", room.getWallsSet().toLongArray());
            roomTag.putLongArray("oxygen_nodes", room.getOxygenNodes().toLongArray());
            roomTag.putFloat("current_oxygen", room.getCurrentOxygen());
            roomTag.putFloat("volume", room.getVolume());
            roomsTag.add(roomTag);
        }

        tag.put("rooms", roomsTag);
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag)
    {
        Map<UUID, Room> newRooms = new HashMap<>();
        Long2ObjectOpenHashMap<Set<UUID>> newSpatialIndex = new Long2ObjectOpenHashMap<>();

        ListTag roomsTag = tag.getList("rooms", Tag.TAG_COMPOUND);
        for(int i = 0; i < roomsTag.size(); i++)
        {
            CompoundTag roomTag = roomsTag.getCompound(i);

            UUID id = roomTag.getUUID("uuid");
            LongOpenHashSet interior = new LongOpenHashSet(roomTag.getLongArray("interior"));
            LongOpenHashSet boundary = new LongOpenHashSet(roomTag.getLongArray("walls"));
            LongOpenHashSet nodes = new LongOpenHashSet(roomTag.getLongArray("oxygen_nodes"));

            Room room = new Room(id, interior, boundary, nodes);
            room.setCurrentOxygen(level, roomTag.getFloat("current_oxygen"));
            room.setVolume(roomTag.getFloat("volume"));

            newRooms.put(id, room);
            for(long posLong : interior)
            {
                long chunkLong = ChunkPos.asLong(BlockPos.getX(posLong) >> 4, BlockPos.getZ(posLong) >> 4);
                newSpatialIndex.computeIfAbsent(chunkLong, k -> new HashSet<>()).add(id);
            }
            for(long posLong : boundary)
            {
                long chunkLong = ChunkPos.asLong(BlockPos.getX(posLong) >> 4, BlockPos.getZ(posLong) >> 4);
                newSpatialIndex.computeIfAbsent(chunkLong, k -> new HashSet<>()).add(id);
            }
        }

        this.rooms.clear();
        this.rooms.putAll(newRooms);

        this.spatialIndex.clear();
        this.spatialIndex.putAll(newSpatialIndex);
    }
}