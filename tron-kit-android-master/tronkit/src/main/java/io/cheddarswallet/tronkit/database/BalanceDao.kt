package io.cheddarswallet.tronkit.database

import androidx.room.*
import io.cheddarswallet.tronkit.models.Balance

@Dao
interface BalanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(balance: Balance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(balances: List<Balance>)

    @Query("DELETE FROM Balance")
    fun deleteAll()

    @Query("SELECT * FROM Balance where id=:id")
    fun getBalance(id: String): Balance?
}
