package duper;

import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;

@Mod(ShulkerDuper.MOD_ID)
public class ShulkerDuper {

    public ShulkerDuper() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        MinecraftForge.EVENT_BUS.register(this);
    }
    public static final String MOD_ID = "shulkerduper";
    public static boolean fra = true;
    public static int thex = 0;
    public static int they = 0;
    private static int blockBreakingCooldown;
    private static BlockPos currentBreakingPos = new BlockPos(-1,-1,-1);
    private static boolean breakingBlock;
    private static float currentBreakingProgress;

    private static int timer = 0;

    public static void setFra(boolean fra) {
        ShulkerDuper.fra = fra;
    }
    private static void sendPlayerAction(ServerboundPlayerActionPacket.Action action, BlockPos pos, Direction direction) {
        Util.CLIENT.getConnection().send(new ServerboundPlayerActionPacket(action, pos, direction));
    }
    public static boolean breakBlock(BlockPos pos) {
        if (Util.CLIENT.player.blockActionRestricted(Util.CLIENT.level, pos, Util.CLIENT.gameMode.getPlayerMode())) {
            return false;
        } else {
            Level world = Util.CLIENT.level;
            BlockState blockState = world.getBlockState(pos);
            if (!Util.CLIENT.player.getMainHandItem().getItem().canAttackBlock(blockState, world, pos, Util.CLIENT.player)) {
                return false;
            } else {
                Block block = blockState.getBlock();
                if (block instanceof CommandBlock && !Util.CLIENT.player.isCreative()) {
                    return false;
                } else if (blockState.isAir()) {
                    return false;
                } else {
                    FluidState fluidState = world.getFluidState(pos);
                    return block.onDestroyedByPlayer(blockState, world, pos, Util.CLIENT.player, false, fluidState);
                }
            }
        }
    }

    private static boolean attackBlock(BlockPos pos, Direction direction) {
        if (Util.CLIENT.player.blockActionRestricted(Util.CLIENT.level, pos, Util.CLIENT.gameMode.getPlayerMode())) {
            return false;
        } else if (!Util.CLIENT.level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        } else {
            BlockState blockState;
            if (Util.CLIENT.player.isCreative()) {
                blockState = Util.CLIENT.level.getBlockState(pos);
                Util.CLIENT.getTutorial().onDestroyBlock(Util.CLIENT.level, pos, blockState, 1.0F);
                ShulkerDuper.sendPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction);
                ShulkerDuper.breakBlock(pos);
                ShulkerDuper.blockBreakingCooldown = 5;
            } else if (!ShulkerDuper.breakingBlock || !ShulkerDuper.isCurrentlyBreaking(pos)) {
                if (ShulkerDuper.breakingBlock) {
                    ShulkerDuper.sendPlayerAction(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, ShulkerDuper.currentBreakingPos, direction);
                }

                blockState = Util.CLIENT.level.getBlockState(pos);
                Util.CLIENT.getTutorial().onDestroyBlock(Util.CLIENT.level, pos, blockState, 0.0F);
                ShulkerDuper.sendPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction);
                boolean bl = !blockState.isAir();
                if (bl && ShulkerDuper.currentBreakingProgress == 0.0F) {
                    FluidState fluidState = Util.CLIENT.level.getFluidState(pos);
                    blockState.onDestroyedByPlayer(Util.CLIENT.level, pos, Util.CLIENT.player, false, fluidState);
                }

                if (bl && blockState.getDestroyProgress(Util.CLIENT.player, Util.CLIENT.player.clientLevel, pos) >= 1.0F) {
                    ShulkerDuper.breakBlock(pos);
                } else {
                    ShulkerDuper.breakingBlock = true;
                    ShulkerDuper.currentBreakingPos = pos;
                    ShulkerDuper.currentBreakingProgress = 0.0F;
                }
            }

            return true;
        }
    }

    public static boolean updateBlockBreakingProgress(BlockPos pos, Direction direction) {
        if (ShulkerDuper.blockBreakingCooldown > 0) {
            --ShulkerDuper.blockBreakingCooldown;
            return true;
        } else {
            BlockState blockState;
            if (Util.CLIENT.player.isCreative() && Util.CLIENT.level.getWorldBorder().isWithinBounds(pos)) {
                ShulkerDuper.blockBreakingCooldown = 5;
                blockState = Util.CLIENT.level.getBlockState(pos);
                Util.CLIENT.getTutorial().onDestroyBlock(Util.CLIENT.level, pos, blockState, 1.0F);
                ShulkerDuper.sendPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction);
                ShulkerDuper.breakBlock(pos);
                return true;
            } else if (ShulkerDuper.isCurrentlyBreaking(pos)) {
                blockState = Util.CLIENT.level.getBlockState(pos);
                if (blockState.isAir()) {
                    ShulkerDuper.breakingBlock = false;
                    return false;
                } else {
                    ShulkerDuper.currentBreakingProgress += blockState.getDestroyProgress(Util.CLIENT.player, Util.CLIENT.player.clientLevel, pos);

                    Util.CLIENT.getTutorial().onDestroyBlock(Util.CLIENT.level, pos, blockState, Mth.clamp(ShulkerDuper.currentBreakingProgress, 0.0F, 1.0F));
                    if (ShulkerDuper.currentBreakingProgress >= 1.0F) {
                        ShulkerDuper.breakingBlock = false;
                        ShulkerDuper.sendPlayerAction(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, direction);
                        ShulkerDuper.breakBlock(pos);
                        ShulkerDuper.currentBreakingProgress = 0.0F;
                        ShulkerDuper.blockBreakingCooldown = 5;
                    }
                    return true;
                }
            } else {
                return ShulkerDuper.attackBlock(pos, direction);
            }
        }
    }
    private static boolean isCurrentlyBreaking(BlockPos pos) {
        return pos.equals(ShulkerDuper.currentBreakingPos);
    }

    public static void tick() {
        boolean b1 = (Util.CLIENT.screen instanceof ShulkerBoxScreen);
        if (SharedVariables.shouldDupe || SharedVariables.shouldDupeAll) {
            HitResult hit = Util.CLIENT.hitResult;
            if (hit instanceof BlockHitResult) {
                BlockHitResult blockHit = (BlockHitResult) hit;
                if (Util.CLIENT.level.getBlockState(blockHit.getBlockPos()).getBlock() instanceof ShulkerBoxBlock && b1) {
                    ShulkerDuper.updateBlockBreakingProgress(blockHit.getBlockPos(), Direction.DOWN);
                } else {
                    Util.log("You need to have a shulker box screen open and look at a shulker box.");
                    Util.CLIENT.player.closeContainer();
                    SharedVariables.shouldDupe = false;
                    SharedVariables.shouldDupeAll = false;
                }
            }
        }
        if (b1) {

        } else {
            setFra(true);
        }
    }

    private void preInit(final FMLCommonSetupEvent event)
    {
        System.out.println("duper starting");
    }
}
