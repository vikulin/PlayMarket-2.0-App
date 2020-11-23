package com.blockchain.store.dao.database;



import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.blockchain.store.dao.database.model.Proposal;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface IProposalDao {

    @Insert
    void insert(Proposal proposal);

    @Update
    void update(Proposal proposal);

    @Delete
    void delete(Proposal proposal);

    @Insert
    void insert(ArrayList<Proposal> proposals);

    @Query("SELECT * FROM Proposal WHERE proposalID = :proposalId")
    Proposal getById(long proposalId);

    @Query("SELECT * FROM Proposal")
    List<Proposal> getAll();
}
