package io.cheddarswallet.tronkit

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import io.cheddarswallet.hdwalletkit.Mnemonic
import io.cheddarswallet.tronkit.account.AccountInfoManager
import io.cheddarswallet.tronkit.contracts.ContractMethodHelper
import io.cheddarswallet.tronkit.contracts.trc20.TransferMethod
import io.cheddarswallet.tronkit.crypto.InternalBouncyCastleProvider
import io.cheddarswallet.tronkit.database.Storage
import io.cheddarswallet.tronkit.database.TronDatabaseManager
import io.cheddarswallet.tronkit.decoration.DecorationManager
import io.cheddarswallet.tronkit.decoration.trc20.Trc20TransactionDecorator
import io.cheddarswallet.tronkit.models.Address
import io.cheddarswallet.tronkit.models.Contract
import io.cheddarswallet.tronkit.models.FullTransaction
import io.cheddarswallet.tronkit.models.TransferContract
import io.cheddarswallet.tronkit.models.TriggerSmartContract
import io.cheddarswallet.tronkit.network.ApiKeyProvider
import io.cheddarswallet.tronkit.network.ConnectionManager
import io.cheddarswallet.tronkit.network.Network
import io.cheddarswallet.tronkit.network.TronGridService
import io.cheddarswallet.tronkit.sync.ChainParameterManager
import io.cheddarswallet.tronkit.sync.SyncTimer
import io.cheddarswallet.tronkit.sync.Syncer
import io.cheddarswallet.tronkit.transaction.Fee
import io.cheddarswallet.tronkit.transaction.FeeProvider
import io.cheddarswallet.tronkit.transaction.Signer
import io.cheddarswallet.tronkit.transaction.TransactionManager
import io.cheddarswallet.tronkit.transaction.TransactionSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.math.BigInteger
import java.security.Security
import java.util.Objects

class TronKit(
    val address: Address,
    val network: Network,
    private val syncer: Syncer,
    private val accountInfoManager: AccountInfoManager,
    private val transactionManager: TransactionManager,
    private val transactionSender: TransactionSender,
    private val feeProvider: FeeProvider,
    private val chainParameterManager: ChainParameterManager
) {
    private var started = false
    private var scope: CoroutineScope? = null

    val lastBlockHeight: Long
        get() = syncer.lastBlockHeight

    val lastBlockHeightFlow: StateFlow<Long>
        get() = syncer.lastBlockHeightFlow

    val trxBalance: BigInteger
        get() = accountInfoManager.trxBalance

    val trxBalanceFlow: StateFlow<BigInteger>
        get() = accountInfoManager.trxBalanceFlow

    val syncState: SyncState
        get() = syncer.syncState

    val syncStateFlow: StateFlow<SyncState>
        get() = syncer.syncStateFlow

    val transactionsFlow: StateFlow<Pair<List<FullTransaction>, Boolean>>
        get() = transactionManager.transactionsFlow

    fun start() {
        if (started) return
        started = true

        scope = CoroutineScope(Dispatchers.IO)
            .apply {
                syncer.start(this)

                launch {
                    chainParameterManager.sync()
                }
            }
    }

    fun stop() {
        started = false
        syncer.stop()

        scope?.cancel()
    }

    fun refresh() {
        syncer.refresh()
    }

    fun getTrc20Balance(contractAddress: String): BigInteger {
        return accountInfoManager.getTrc20Balance(contractAddress)
    }

    fun getTrc20BalanceFlow(contractAddress: String): Flow<BigInteger> {
        return accountInfoManager.getTrc20BalanceFlow(contractAddress)
    }

    fun getFullTransactionsFlow(tags: List<List<String>>): Flow<List<FullTransaction>> {
        return transactionManager.getFullTransactionsFlow(tags)
    }

    suspend fun getFullTransactions(tags: List<List<String>>, fromHash: ByteArray? = null, limit: Int? = null): List<FullTransaction> {
        return transactionManager.getFullTransactions(tags, fromHash, limit)
    }

    suspend fun getFullTransactions(hashes: List<ByteArray>): List<FullTransaction> {
        return transactionManager.getFullTransactions(hashes)
    }

    suspend fun estimateFee(contract: Contract): List<Fee> {
        return feeProvider.estimateFee(contract)
    }

    suspend fun isAccountActive(address: Address): Boolean {
        return feeProvider.isAccountActive(address)
    }

    fun transferContract(amount: BigInteger, toAddress: Address) = TransferContract(
        amount = amount,
        ownerAddress = address,
        toAddress = toAddress
    )

    fun transferTrc20TriggerSmartContract(
        contractAddress: Address,
        toAddress: Address,
        amount: BigInteger
    ): TriggerSmartContract {
        val transferMethod = TransferMethod(toAddress, amount)
        val data = transferMethod.encodedABI().toRawHexString()
        val parameter = ContractMethodHelper
            .encodedABI(methodId = byteArrayOf(), arguments = transferMethod.getArguments())
            .toRawHexString()

        return TriggerSmartContract(
            data = data,
            ownerAddress = address,
            contractAddress = contractAddress,
            callTokenValue = null,
            callValue = null,
            tokenId = null,
            functionSelector = TransferMethod.methodSignature,
            parameter = parameter
        )
    }

    suspend fun send(contract: Contract, signer: Signer, feeLimit: Long? = null): String {
        val createdTransaction = transactionSender.createTransaction(contract, feeLimit)
        val response = transactionSender.broadcastTransaction(createdTransaction, signer)

        check(response.result) {
            throw IllegalStateException(response.code + " " + response.message)
        }

        transactionManager.handle(createdTransaction)

        return response.txid
    }

    fun statusInfo(): Map<String, Any> {
        val statusInfo = LinkedHashMap<String, Any>()

        statusInfo["Started"] = started
        statusInfo["Last Block Height"] = lastBlockHeight
        statusInfo["Sync State"] = syncState
        statusInfo["Chain Parameters Sync State"] = chainParameterManager.syncState

        return statusInfo
    }

    sealed class SyncState {
        class Synced : SyncState()
        class NotSynced(val error: Throwable) : SyncState()
        class Syncing(val progress: Double? = null) : SyncState()

        override fun toString(): String = when (this) {
            is Syncing -> "Syncing ${progress?.let { "${it * 100}" } ?: ""}"
            is NotSynced -> "NotSynced ${error.javaClass.simpleName} - message: ${error.message}"
            else -> this.javaClass.simpleName
        }

        override fun equals(other: Any?): Boolean {
            if (other !is SyncState)
                return false

            if (other.javaClass != this.javaClass)
                return false

            if (other is Syncing && this is Syncing) {
                return other.progress == this.progress
            }

            return true
        }

        override fun hashCode(): Int {
            if (this is Syncing) {
                return Objects.hashCode(this.progress)
            }
            return Objects.hashCode(this.javaClass.name)
        }
    }

    sealed class SyncError : Throwable() {
        class NotStarted : SyncError()
        class NoNetworkConnection : SyncError()
    }

    sealed class TransactionError : Throwable() {
        class NotSupportedContract(val contract: Contract) : TransactionError()
        class InvalidCreatedTransaction(val rawDataHex: String) : TransactionError()
        class NoFunctionSelector(val triggerSmartContract: TriggerSmartContract) : TransactionError()
        class NoParameter(val triggerSmartContract: TriggerSmartContract) : TransactionError()
        class NoFeeLimit(val triggerSmartContract: TriggerSmartContract) : TransactionError()
    }

    companion object {

        fun init() {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
            Security.addProvider(InternalBouncyCastleProvider.getInstance())
        }

        fun clear(context: Context, network: Network, walletId: String) {
            TronDatabaseManager.clear(context, network, walletId)
        }

        fun getInstance(
            application: Application,
            seed: ByteArray,
            network: Network,
            tronGridApiKeys: List<String>,
            walletId: String
        ): TronKit {
            val privateKey = Signer.privateKey(seed, network)
            val address = Signer.address(privateKey, network)

            return getInstance(application, address, network, tronGridApiKeys, walletId)
        }

        fun getInstance(
            application: Application,
            address: Address,
            network: Network,
            tronGridApiKeys: List<String>,
            walletId: String
        ): TronKit {
            val syncTimer = SyncTimer(30, ConnectionManager(application))
            val apiKeyProvider = ApiKeyProvider(tronGridApiKeys)
            val tronGridService = TronGridService(network, apiKeyProvider)
            val mainDatabase = TronDatabaseManager.getMainDatabase(application, network, walletId)
            val storage = Storage(mainDatabase)
            val accountInfoManager = AccountInfoManager(storage)
            val decorationManager = DecorationManager(storage).apply {
                addTransactionDecorator(Trc20TransactionDecorator(address))
            }
            val transactionManager = TransactionManager(address, storage, decorationManager, Gson())
            val syncer = Syncer(address, syncTimer, tronGridService, accountInfoManager, transactionManager, storage)
            val transactionSender = TransactionSender(tronGridService)
            val chainParameterManager = ChainParameterManager(tronGridService, storage)
            val feeProvider = FeeProvider(tronGridService, chainParameterManager)

            return TronKit(address, network, syncer, accountInfoManager, transactionManager, transactionSender, feeProvider, chainParameterManager)
        }
    }

}
