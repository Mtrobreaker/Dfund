package com.dfund.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface SavingGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SavingGoalEntity goal);

    @Update
    void update(SavingGoalEntity goal);

    @Query("SELECT * FROM saving_goals ORDER BY createdAt DESC")
    LiveData<List<SavingGoalEntity>> getAllGoalsLive();

    @Query("SELECT * FROM saving_goals WHERE category = :category LIMIT 1")
    SavingGoalEntity getGoalByCategory(String category);

    @Query("DELETE FROM saving_goals")
    void deleteAll();
}
