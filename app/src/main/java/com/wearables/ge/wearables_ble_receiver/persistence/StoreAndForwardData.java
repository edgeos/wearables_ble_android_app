package com.wearables.ge.wearables_ble_receiver.persistence;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"timestamp", "device_id"})
public class StoreAndForwardData implements Serializable {
    @NonNull
    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "sent")
    public boolean sent;

    @NonNull
    @ColumnInfo(name = "device_id")
    public String deviceId;

    @ColumnInfo(name = "data_line")
    public String dataLine;

    @Override
    public String toString() {
        // For now leave this unaltered
        return dataLine;
    }
}
