package com.isgm.camreport.roomdb;
import java.util.List;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface HistoryDao {
   @Query("SELECT * FROM History WHERE date=:sendDate AND isSent=:isSent")
   List<History> getHistory(String sendDate,boolean isSent);

   //By MH for getting not sent data
   @Query("SELECT * FROM History WHERE isSent=:isSent")
   List<History> getNotSendData(boolean isSent);

   @Query("Delete From History WHERE id=:id")
   void deleteById(int id);

   @Query("UPDATE History SET isSent=:isSent WHERE id=:id")
   void updateById(boolean isSent,int id);

   @Query("UPDATE History SET isSent=:isSent WHERE image=:image")
   void update(boolean isSent,String image);

   @Insert
   void insert(History history);

}
