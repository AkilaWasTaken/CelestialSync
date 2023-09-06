package dev.celestial.sync.database.profile;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;

import java.util.UUID;

@Data
@DatabaseTable(tableName = "verified")
public class VerifiedProfile {
    @DatabaseField(columnName = "UUID", dataType = DataType.UUID)
    private UUID uuid;

    @DatabaseField(columnName = "discord", dataType = DataType.STRING)
    private String DiscordID;

    @DatabaseField(columnName = "code", dataType = DataType.STRING)
    private String code;
}
