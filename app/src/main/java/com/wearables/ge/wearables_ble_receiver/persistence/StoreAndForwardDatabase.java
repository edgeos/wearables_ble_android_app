package com.wearables.ge.wearables_ble_receiver.persistence;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {StoreAndForwardData.class}, version = 1, exportSchema = false)
public abstract class StoreAndForwardDatabase extends RoomDatabase {
    public abstract StoreAndForwardDataDao storeAndForwardDataDao();
}
