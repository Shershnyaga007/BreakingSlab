package net.lollipopmc.breakingslab;

import net.kyori.adventure.text.Component;
import net.lollipopmc.lib.configurate.objectmapping.ConfigSerializable;
import net.lollipopmc.lib.configuration.holder.ConfigHolder;
import net.lollipopmc.lib.database.engine.credentials.DatabaseCredentials;
import net.lollipopmc.lib.database.url.DatabaseTypeUrl;
import net.lollipopmc.lib.minimessage.MiniMessage;

import java.io.File;

@ConfigSerializable
public class Config extends ConfigHolder<Config> {
    public String disabledMessage;
    public String enabledMessage;
    public String permission;
    public String noPermissionMessage;
    public String invalidSyntaxMessage;
    public String invalidSenderMessage;
    public boolean enableDefault;
    public boolean useMysql;
    public DatabaseCredentials mysqlCredentials;


    public Config(File baseFilePath) {
        super(baseFilePath);
        this.disabledMessage = "Функция выключена";
        this.enabledMessage = "Функция включена";
        this.permission = "net.lollipopmc.breadkingslab";
        this.noPermissionMessage = "";
        this.invalidSyntaxMessage = "";
        this.invalidSenderMessage = "";
        this.enableDefault = true;
        this.useMysql = false;
        this.mysqlCredentials = DatabaseCredentials.getCredentials(DatabaseTypeUrl.MYSQL,
                "localhost:3306/tests", "admin", "admin");
    }

    private Config() {
        this((File)null);
    }

    public Component convertMiniMessageToString(String message) {
        MiniMessage miniMessage = MiniMessage.get();
        return miniMessage.deserialize(message);
    }
}
