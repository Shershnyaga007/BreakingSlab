package net.lollipopmc.breakingslab;

import com.j256.ormlite.jdbc.db.H2DatabaseType;
import com.j256.ormlite.jdbc.db.MysqlDatabaseType;
import lombok.Getter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.lollipopmc.breakingslab.database.BslabUserDatabase;
import net.lollipopmc.lib.LollipopLib;
import net.lollipopmc.lib.command.Command;
import net.lollipopmc.lib.command.CommandManager;
import net.lollipopmc.lib.command.execution.CommandExecutionCoordinator;
import net.lollipopmc.lib.command.minecraft.extras.MinecraftExceptionHandler;
import net.lollipopmc.lib.command.paper.PaperCommandManager;
import net.lollipopmc.lib.configuration.holder.ConfigHolder;
import net.lollipopmc.lib.database.engine.credentials.DatabaseCredentials;
import net.lollipopmc.lib.database.url.DatabaseTypeUrl;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public final class BreakingSlab extends JavaPlugin implements Listener {
    @Getter
    private static BreakingSlab INSTANCE;

    private CommandManager<CommandSender> commandManager;
    private Config config;

    @Getter
    private BslabUserDatabase database;
    private DatabaseCredentials credentials;

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.config = this.loadOrCreateConfig(new Config(new File(this.getDataFolder(), "config.yml")));
        this.commandManager = setupCommandSender();

        if (config.useMysql) {
            credentials = config.mysqlCredentials;
            database = new BslabUserDatabase(() -> LollipopLib.lib().getHikariPool(credentials),
                    new MysqlDatabaseType());
        }
        else {
            credentials = DatabaseCredentials.getCredentials(DatabaseTypeUrl.H2,
                    getDataFolder().getAbsolutePath() + File.separator + "BreakingSlab",
                    "admin", "admin");
            database = new BslabUserDatabase(() -> LollipopLib.lib().getHikariPool(credentials),
                    new H2DatabaseType());

        }

        breakingSlabCommandRegister();
        getServer().getPluginManager().registerEvents(new OnSlabBreak(), this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    private <T extends ConfigHolder<T>> T loadOrCreateConfig(T config) {
        return config.getBaseFilePath().exists() ? config.load(false) : config.loadAndSave();
    }

    private CommandManager<CommandSender> setupCommandSender() {
        CommandManager<CommandSender> manager = null;
        try {
            manager = new PaperCommandManager<>(
                    this, CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity()
            );

            new MinecraftExceptionHandler<CommandSender>()
                    .withDefaultHandlers()
                    .withHandler(MinecraftExceptionHandler.ExceptionType.NO_PERMISSION, (sender, exception) -> {
                        return LegacyComponentSerializer.legacyAmpersand().deserialize(
                                config.convertMiniMessageToString(config.noPermissionMessage).toString()
                        );
                    })
                    .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SYNTAX, (sender, exception) -> {
                        return LegacyComponentSerializer.legacySection().deserialize(
                                config.convertMiniMessageToString(config.invalidSyntaxMessage).toString()
                        );
                    })
                    .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SENDER, (sender, exception) ->
                            LegacyComponentSerializer.legacyAmpersand().deserialize(
                                    config.convertMiniMessageToString(config.invalidSenderMessage).toString()
                            )
                    )
                    .apply(manager, s -> s);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return manager;
    }

    private void breakingSlabCommandRegister() {
        Command.Builder<CommandSender> builder = commandManager.commandBuilder("bslab");

            commandManager.command(builder
                    .literal("change")
                    .permission(config.permission)
                    .handler(context -> {
                        CommandSender sender = context.getSender();

                        if (!(sender instanceof Player player))
                            return;

                        BslabUserDatabase.BslabUser user = database.load(player.getUniqueId());

                        if (Objects.requireNonNull(user).isEnabled()) {
                            user.setEnabled(false);
                            player.sendMessage(config.convertMiniMessageToString(config.disabledMessage));
                        }
                        else {
                            user.setEnabled(true);
                            player.sendMessage(config.convertMiniMessageToString(config.enabledMessage));
                        }

                        database.updateTable(user);
                    })
        );

    }

    @EventHandler
    private void onPlayerJoinAtFirstTime(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (database.load(playerUUID) == null) {
            BslabUserDatabase.BslabUser user = new BslabUserDatabase.BslabUser(playerUUID, config.enableDefault);
            database.updateTable(user);
        }

    }
}
