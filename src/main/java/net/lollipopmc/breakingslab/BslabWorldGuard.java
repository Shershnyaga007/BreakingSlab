package net.lollipopmc.breakingslab;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class BslabWorldGuard {
    public static boolean canPlayerHasPermissionToBreak(Player player, Location location) throws NoClassDefFoundError {

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld()))
            return true;

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return query.testState(BukkitAdapter.adapt(location), localPlayer, Flags.BLOCK_BREAK);
    }
}
