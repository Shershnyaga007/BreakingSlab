package net.lollipopmc.breakingslab.database;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;
import net.lollipopmc.lib.database.engine.typed.NamedAutoClosableDao;
import net.lollipopmc.lib.database.interfaces.IDatabase;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class BslabUserDatabase extends NamedAutoClosableDao<BslabUserDatabase.BslabUsersTable, Long>
        implements IDatabase<BslabUserDatabase.BslabUsersTable, Long> {

    private static final String TABLE_NAME = "bslab_users";

    public BslabUserDatabase(DataSourceSupplier dataSourceSupplier, BaseDatabaseType type) {
        super(dataSourceSupplier, BslabUsersTable.class, TABLE_NAME, type);
        // createTable("utf8").join();
        createTable().join();
    }

    public BslabUser load(UUID playerUUID) {
        List<BslabUsersTable> table = getElements(builder -> {
            return builder.where().eq(BslabUsersTable.COLUMN_PLAYER_UUID, playerUUID).queryBuilder();
        }).join().stream().toList();

        if (table.size() == 0) {
            return null;
        }

        return new BslabUser(table.get(0).uuid, table.get(0).enabled);
    }

    public void updateTable(@NotNull BslabUser user) {
        List<BslabUsersTable> table = getElements(builder -> {
            return builder.where().eq(BslabUsersTable.COLUMN_PLAYER_UUID, user.getUuid()).queryBuilder();
        }).join().stream().toList();

        if (table.size() == 0) {
            createElement(new BslabUsersTable(user.getUuid(), user.isEnabled())).join();
        }
        updateFirstElement(builder -> {

            builder.where().eq(BslabUsersTable.COLUMN_PLAYER_UUID, user.getUuid());
            builder.updateColumnValue(BslabUsersTable.COLUMN_PLAYER_BSLAB_ENABLED, user.isEnabled());
            return builder;
        });
    }

    @Override
    public Long idOf(BslabUsersTable usersTable) {
        return usersTable.getId();
    }

    @DatabaseTable(tableName = TABLE_NAME)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @With
    public static class BslabUsersTable {
        private static final String COLUMN_PLAYER_UUID = "player_uuid";
        private static final String COLUMN_PLAYER_BSLAB_ENABLED = "enabled";

        @DatabaseField(index = true, generatedId = true, columnName = "id")
        private Long id;

        @DatabaseField(index = true, unique = true, canBeNull = false, columnName = COLUMN_PLAYER_UUID)
        private UUID uuid;

        @DatabaseField(defaultValue = "FALSE", canBeNull = false, columnName = COLUMN_PLAYER_BSLAB_ENABLED)
        private boolean enabled;

        public BslabUsersTable(UUID uuid, boolean enabled) {
            this.uuid = uuid;
            this.enabled = enabled;
        }
    }

    @AllArgsConstructor
    public static class BslabUser {
        @Getter
        private UUID uuid;

        @Getter
        @Setter
        private boolean enabled;
    }

}
