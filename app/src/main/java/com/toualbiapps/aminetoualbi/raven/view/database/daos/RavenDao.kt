package com.toualbiapps.aminetoualbi.raven.view.database.daos

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.OnConflictStrategy.IGNORE
import com.toualbiapps.aminetoualbi.raven.view.model.Raven

@Dao
interface RavenDao {                //Data Access Object.

    @Query("SELECT * FROM ravens")
    fun getAll(): List<Raven>

    @Insert(onConflict = IGNORE)
    fun insert(raven: Raven)

    @Update(onConflict = REPLACE)
    fun update(raven: Raven)

    @Delete
    fun delete(raven: Raven)

}