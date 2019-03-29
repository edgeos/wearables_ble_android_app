package com.wearables.ge.wearables_ble_receiver.persistence;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface StoreAndForwardDataDao {
    @Query("SELECT * FROM storeandforwarddata WHERE sent = 0 ORDER BY timestamp ASC LIMIT :limit")
    List<StoreAndForwardData> getNotSent(long limit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StoreAndForwardData data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(StoreAndForwardData... data);

    @Query("DELETE FROM storeandforwarddata WHERE sent = 1")
    void deleteAllSent();
}
