package dev.celestial.sync.database.profile;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;

@Data
@DatabaseTable(tableName = "unverified")
public class CodeProfile {
    @DatabaseField(columnName = "discord", dataType = DataType.STRING)
    private String DiscordID;

    @DatabaseField(columnName = "code", dataType = DataType.STRING)
    private String code;
}
