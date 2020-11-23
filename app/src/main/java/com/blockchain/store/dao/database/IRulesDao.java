package com.blockchain.store.dao.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.blockchain.store.dao.database.model.Rules;

@Dao
public interface IRulesDao {

    @Insert
    void insert(Rules rules);

    @Update
    void update(Rules rules);

    @Delete
    void delete(Rules rules);

    @Query("SELECT * FROM Rules")
    Rules getRules();

}
