package net.lollipopmc.breakingslab;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;

@AllArgsConstructor
public class BreakingSlabScheduler {
    private BreakingSlab breakingSlab;

    public void runAsyncBukkitTask(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(breakingSlab, runnable);
    }

    public void runSyncBukkitTask(Runnable runnable) {
        Bukkit.getScheduler().runTask(breakingSlab, runnable);
    }
}
