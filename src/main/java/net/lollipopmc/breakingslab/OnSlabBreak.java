package net.lollipopmc.breakingslab;

import net.lollipopmc.breakingslab.database.BslabUserDatabase;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class OnSlabBreak implements Listener {

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (!block.getType().name().contains("SLAB"))
            return;

        if (!block.getBlockData().toString().contains("type=double"))
            return;

        Player player = event.getPlayer();

        try {
            if (!BslabWorldGuard.canPlayerHasPermissionToBreak(player, event.getBlock().getLocation()))
                return;

        } catch (NoClassDefFoundError ignored) {

        }

        event.setCancelled(true);

        Executor executor = Executors.newCachedThreadPool();
        CompletableFuture<BslabUserDatabase.BslabUser> future = new CompletableFuture<>();

        executor.execute(() -> {
            BslabUserDatabase.BslabUser user = BreakingSlab.getDatabase().load(player.getUniqueId());
            future.complete(user);
        });

        future.thenAccept(entity -> {
            if (!entity.isEnabled()) {
                event.getBlock().breakNaturally();
                return;
            }

            Location location = player.getEyeLocation();
            Vector direction = location.getDirection();

            RayTraceResult result = player.getWorld().rayTraceBlocks(location, direction, 8);
            Vector resultDirection = Objects.requireNonNull(result).getHitPosition();

            double resultYAtBlock;
            if (resultDirection.getBlockY() > block.getLocation().getBlockY())
                resultYAtBlock = 1;
            else
                resultYAtBlock = Math.abs(resultDirection.getY() % 1);

            if (block.getLocation().getBlockY() < 0 && resultYAtBlock != 0 && resultYAtBlock != 1) {
                resultYAtBlock = 1 - resultYAtBlock;
            }

            updateSlab(player, block,
                    resultYAtBlock <= .5d);
        });

        // future.join();
    }

    private void updateSlab(Player player, Block block, boolean isUpperSlabHasLeft) {
        String blockDataOldStr = block.getBlockData().getAsString();
        Location dropSpawnLocation = getCenterBlockLocation(block.getLocation());

        String typeReplacement;
        if (isUpperSlabHasLeft)
            typeReplacement = "top";
        else
            typeReplacement = "bottom";

        String blockDataNewStr = blockDataOldStr.replace("double", typeReplacement);
        BlockData blockDataNew = Bukkit.createBlockData(blockDataNewStr);

        block.setBlockData(blockDataNew);

        if (player.getGameMode() != GameMode.CREATIVE)
            dropSpawnLocation.getWorld().dropItemNaturally(dropSpawnLocation,
                    new ItemStack(block.getType(), 1));
    }

    private Location getCenterBlockLocation(Location location) {
        Location centerLocation = location.toCenterLocation();

        if (centerLocation.getX() >= 0)
            centerLocation.add(0.5f, 0, 0);
        else
            centerLocation.add(-0.5f, 0, 0);

        if (centerLocation.getZ() >= 0)
            centerLocation.add(0, 0, 0.5f);
        else
            centerLocation.add(0, 0, -0.5f);

        return location;
    }
}
