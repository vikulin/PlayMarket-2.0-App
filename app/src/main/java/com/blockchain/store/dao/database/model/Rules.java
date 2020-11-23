package com.blockchain.store.dao.database.model;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Rules {
    @PrimaryKey
    public int id;
    public long minimumQuorum;
    public long debatingPeriodDuration;
    public long requisiteMajority;
}
