package net.lollipopmc.breakingslab;

import net.kyori.adventure.text.Component;
import net.lollipopmc.lib.configurate.objectmapping.ConfigSerializable;
import net.lollipopmc.lib.configurate.serialize.TypeSerializerCollection;
import net.lollipopmc.lib.configuration.holder.ConfigHolder;
import net.lollipopmc.lib.configuration.serializers.ComponentSerializer;
import net.lollipopmc.lib.database.engine.credentials.DatabaseCredentials;
import net.lollipopmc.lib.database.url.DatabaseTypeUrl;
import net.lollipopmc.lib.minimessage.MiniMessage;

import java.io.File;

@ConfigSerializable
public class Config extends ConfigHolder<Config> {
    public Component disabledMessage = Component.text("Функция выключена");
    public Component enabledMessage  = Component.text("Функция включена");
    public String permission = "net.lollipopmc.breadkingslab";
    public Component noPermissionMessage = Component.text("");
    public Component invalidSyntaxMessage = Component.text("");
    public Component invalidSenderMessage = Component.text("");
    public boolean enableDefault = false;
    public boolean useMysql = false;
    public DatabaseCredentials mysqlCredentials  = DatabaseCredentials.getCredentials(DatabaseTypeUrl.MYSQL,
            "localhost:3306/tests", "admin", "admin");


    public Config(File baseFilePath) {
        super(baseFilePath, TypeSerializerCollection.builder()
                .register(Component.class, new ComponentSerializer())
                .build());
    }

    private Config() {
        this((File)null);
    }
}
