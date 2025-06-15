package io.cheddarswallet.tronkit.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.cheddarswallet.tronkit.models.Balance
import io.cheddarswallet.tronkit.models.ChainParameter
import io.cheddarswallet.tronkit.models.InternalTransaction
import io.cheddarswallet.tronkit.models.LastBlockHeight
import io.cheddarswallet.tronkit.models.Transaction
import io.cheddarswallet.tronkit.models.TransactionSyncState
import io.cheddarswallet.tronkit.models.TransactionTag
import io.cheddarswallet.tronkit.models.Trc20EventRecord

@Database(
    entities = [
        LastBlockHeight::class,
        Balance::class,
        TransactionSyncState::class,
        Transaction::class,
        InternalTransaction::class,
        Trc20EventRecord::class,
        TransactionTag::class,
        ChainParameter::class,
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class MainDatabase : RoomDatabase() {

    abstract fun lastBlockHeightDao(): LastBlockHeightDao
    abstract fun balanceDao(): BalanceDao
    abstract fun transactionDao(): TransactionDao
    abstract fun tagsDao(): TransactionTagDao
    abstract fun chainParameterDao(): ChainParameterDao

    companion object {
        fun getInstance(context: Context, databaseName: String): MainDatabase {
            return Room.databaseBuilder(context, MainDatabase::class.java, databaseName)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        }
    }
}
