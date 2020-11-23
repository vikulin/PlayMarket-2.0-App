package com.blockchain.store.dao.database;

import com.blockchain.store.dao.database.model.Proposal;
import com.blockchain.store.dao.database.model.Rules;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Proposal.class, Rules.class}, version = 2)
public abstract class DaoDatabase extends RoomDatabase {
    public abstract IProposalDao proposalDao();
    public abstract IRulesDao rulesDao();
}
