package com.isgm.camreport.roomdb;
import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(entities = {History.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HistoryDao historyDao();
}