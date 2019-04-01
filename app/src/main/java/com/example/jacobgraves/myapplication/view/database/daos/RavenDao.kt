package com.example.jacobgraves.myapplication.view.database.daos

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.example.jacobgraves.myapplication.view.model.Raven

@Dao
interface RavenDao {                //Data Access Object.

    @Query("SELECT * FROM ravens")
    fun getAll(): List<Raven>

    @Insert(onConflict = REPLACE)
    fun insert(raven: Raven)

    @Update(onConflict = REPLACE)
    fun update(raven: Raven)

    @Delete
    fun delete(raven: Raven)

}