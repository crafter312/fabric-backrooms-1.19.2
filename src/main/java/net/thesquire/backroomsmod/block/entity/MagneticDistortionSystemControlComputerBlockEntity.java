package net.thesquire.backroomsmod.block.entity;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.thesquire.backroomsmod.block.ModBlocks;
import net.thesquire.backroomsmod.config.ModConfig;
import net.thesquire.backroomsmod.portal.ModPortals;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.blockentity.MultiblockWriter;
import reborncore.common.blocks.BlockMachineBase;
import reborncore.common.screen.BuiltScreenHandler;
import reborncore.common.screen.BuiltScreenHandlerProvider;
import reborncore.common.screen.builder.ScreenHandlerBuilder;
import reborncore.common.util.RebornInventory;
import techreborn.blockentity.machine.GenericMachineBlockEntity;
import techreborn.init.TRContent;

import java.util.HashMap;
import java.util.Map;

public class MagneticDistortionSystemControlComputerBlockEntity extends GenericMachineBlockEntity implements BuiltScreenHandlerProvider {

    // true gives a multiplier of 1, false gives a multiplier of -1
    // this will be converted later
    public boolean side = true;
    public boolean active = false;

    private boolean portalActive = false;
    private final int initEnergyUsage = ModConfig.magneticDistortionSystemControlComputerInitEnergyUsage;
    private final int energyUsage = ModConfig.magneticDistortionSystemControlComputerEnergyUsage;

    private boolean checkPortalActive = true;

    public MagneticDistortionSystemControlComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGNETIC_DISTORTION_SYSTEM_CONTROL_COMPUTER, pos, state, "MagneticDistortionSystemControlComputer",
                ModConfig.magneticDistortionSystemControlComputerMaxInput, ModConfig.magneticDistortionSystemControlComputerMaxEnergy,
                ModBlocks.MAGNETIC_DISTORTION_SYSTEM_CONTROL_COMPUTER, 0);
        this.inventory = new RebornInventory<>(1, "MagneticDistortionSystemControlComputerBlockEntity", 64, this);
    }

    @Override
    public BuiltScreenHandler createScreenHandler(int syncID, PlayerEntity player) {
        return new ScreenHandlerBuilder("magneticdistortionsystem").player(player.getInventory()).inventory().hotbar()
                .addInventory().blockEntity(this).energySlot(0, 8, 72).syncEnergyValue()
                .sync(this::getSide, this::setSide).sync(this::getActive, this::setActive).addInventory().create(this, syncID);
    }

    @Override
    public void writeMultiblock(MultiblockWriter writer) {
        Map<Vec3i, BlockState> map = getPortalMultiblock(side, getFacing());
        for(Map.Entry<Vec3i, BlockState> entry : map.entrySet()) {
            Vec3i relPos = entry.getKey();
            writer.add(relPos.getX(), relPos.getY(), relPos.getZ(), entry.getValue());
        }
    }

    @Override
    public boolean isMultiblockValid() {
        MultiblockWriter.MultiblockVerifier verifier = new MultiblockWriter.MultiblockVerifier(this.getPos(), this.getWorld());
        this.writeMultiblock(verifier.rotate(this.getFacing().getOpposite()));
        return verifier.isValid();
    }

    @Override
    public boolean canBeUpgraded() {
        return false;
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, MachineBaseBlockEntity blockEntity) {
        super.tick(world, pos, state, blockEntity);

        if(world == null || world.isClient) return;

        BlockPos lightLoc = getPortalLightLocation();
        BlockPos oppositeLightLoc = getOppositePortalLightLocation();
        if(!isMultiblockValid()) {
            if(hasPortal(lightLoc)) world.setBlockState(lightLoc, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            if(hasOppositePortal(oppositeLightLoc)) world.setBlockState(oppositeLightLoc, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            setActive(false);
            portalActive = false;
            return;
        }
        if(active) {

            // This runs once on first tick to check if the portal is already lit upon loading the world
            if(checkPortalActive) {
                portalActive = !world.isAir(lightLoc);
                checkPortalActive = false;
            }

            if(!portalActive && getEnergy() > initEnergyUsage) {
                PortalPlacer.attemptPortalLight(world, lightLoc, PortalIgnitionSource.CustomSource(ModPortals.KV31_PORTAL_IGNITION_SOURCE));
//                Direction.Axis portalAxis = this.getFacing().getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
//                Level0PortalDestination.makePortalDestination(world, lightLoc, portalAxis);
                useEnergy(initEnergyUsage);
                portalActive = true;
            } else if(world.isAir(lightLoc)) {
                setActive(false);
                portalActive = false;
                return;
            }

            if(getEnergy() > energyUsage) useEnergy(maxInput / 2);
            else {
                world.setBlockState(lightLoc, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                setActive(false);
                portalActive = false;
            }
        }
    }

    @Override
    public void onBreak(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState) {
        super.onBreak(world, playerEntity, blockPos, blockState);
        world.setBlockState(getPortalLightLocation(), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        this.side = tag.getBoolean("side");
        this.active = tag.getBoolean("active");
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putBoolean("side", this.side);
        tag.putBoolean("active", this.active);
    }

    public boolean getSide() { return side; }

    public void setSide(boolean side) { this.side = side; }

    public boolean getActive() { return active; }

    public void setActive(boolean active) {
        this.active = active;
        assert world != null;
        world.setBlockState(pos, world.getBlockState(pos).with(BlockMachineBase.ACTIVE, active));
    }

    private BlockPos getPortalLightLocation() {
        Vec3i dir = getFacing().rotateYCounterclockwise().getVector().multiply(-1 + (2 * (side ? 1 : 0)));
        return pos.add(dir.multiply(2)).add(0,1,0);
    }

    private BlockPos getOppositePortalLightLocation() {
        Vec3i dir = getFacing().rotateYCounterclockwise().getVector().multiply(-1 + (2 * (side ? 1 : 0)));
        return pos.add(dir.multiply(-2)).add(0,1,0);
    }

    private boolean hasPortal(BlockPos lightLoc) {
        assert world != null;
        return world.getBlockState(lightLoc).isOf(CustomPortalsMod.portalBlock);
    }

    private boolean hasOppositePortal(BlockPos oppositeLightLoc) {
        assert world != null;
        return world.getBlockState(oppositeLightLoc).isOf(CustomPortalsMod.portalBlock);
    }

    /***************************************************************/

    public static Map<Vec3i, BlockState> getPortalMultiblock(Boolean side, Direction facing) {
        Map<Vec3i, BlockState> map = new HashMap<>();

        BlockState casing = TRContent.MachineBlocks.INDUSTRIAL.casing.getDefaultState();
        BlockState magnetVertical = ModBlocks.TFMC_MAGNET.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Y);

        Direction.Axis horizontalAxis = (facing.equals(Direction.NORTH) || facing.equals(Direction.SOUTH)) ? Direction.Axis.X : Direction.Axis.Z;
        BlockState magnetHorizontal = ModBlocks.TFMC_MAGNET.getDefaultState().with(PillarBlock.AXIS, horizontalAxis);

        Vec3i dir = Direction.SOUTH.getVector().multiply(-1 + (2 * (side ? 1 : 0)));
        Vec3i up = Direction.UP.getVector();

        // add corner blocks
        map.put(dir, casing);
        map.put(dir.multiply(4), casing);
        map.put(dir.add(up.multiply(4)), casing);
        map.put(dir.multiply(4).add(up.multiply(4)), casing);

        // add horizontal sides
        map.put(dir.multiply(2), magnetHorizontal);
        map.put(dir.multiply(3), magnetHorizontal);
        map.put(up.multiply(4).add(dir.multiply(2)), magnetHorizontal);
        map.put(up.multiply(4).add(dir.multiply(3)), magnetHorizontal);

        // add vertical sides
        map.put(dir.add(up), magnetVertical);
        map.put(dir.add(up.multiply(2)), magnetVertical);
        map.put(dir.add(up.multiply(3)), magnetVertical);
        map.put(dir.multiply(4).add(up), magnetVertical);
        map.put(dir.multiply(4).add(up.multiply(2)), magnetVertical);
        map.put(dir.multiply(4).add(up.multiply(3)), magnetVertical);

        return map;
    }

}
