package net.lollipopmc.breakingslab;

import lombok.AllArgsConstructor;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Objects;

@AllArgsConstructor
public class OnSlabBreak implements Listener {
    private BreakingSlab.BreakingSlabScheduler breakingSlabScheduler;
    private BslabUserDatabase database;

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (!block.getType().name().contains("SLAB"))
            return;

        if (!block.getBlockData().toString().contains("type=double"))
            return;

        Player player = event.getPlayer();

        try {
            if (!BslabWorldGuard.canPlayerHasPermissionToBreak(player, block.getLocation()))
                return;

        } catch (NoClassDefFoundError ignored) {

        }

        JavaPlugin plugin = BreakingSlab.getProvidingPlugin(BreakingSlab.class);
        BreakingSlab.BreakingSlabScheduler breakingSlabScheduler =
                new BreakingSlab.BreakingSlabScheduler((BreakingSlab) plugin);

        event.setCancelled(true);

        breakingSlabScheduler.runAsyncBukkitTask(() -> {

            if (!isPlayerHasEnabledFunc(player)) {
                breakingSlabScheduler.runSyncBukkitTask(block::breakNaturally);
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

        breakingSlabScheduler.runSyncBukkitTask(() -> {

            block.setBlockData(blockDataNew);

            if (player.getGameMode() != GameMode.CREATIVE)
                dropSpawnLocation.getWorld().dropItemNaturally(dropSpawnLocation,
                        new ItemStack(block.getType(), 1));
        });
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

    private boolean isPlayerHasEnabledFunc(Player player) {
        BslabUserDatabase.BslabUser user = database.load(player.getUniqueId());

        return Objects.requireNonNull(user).isEnabled();
    }

}
