package io.cheddarswallet.tronkit.rpc

import io.cheddarswallet.tronkit.contracts.ContractMethodHelper
import io.cheddarswallet.tronkit.contracts.trc20.DecimalsMethod
import io.cheddarswallet.tronkit.contracts.trc20.NameMethod
import io.cheddarswallet.tronkit.contracts.trc20.SymbolMethod
import io.cheddarswallet.tronkit.decoration.TokenInfo
import io.cheddarswallet.tronkit.models.Address
import io.cheddarswallet.tronkit.network.ApiKeyProvider
import io.cheddarswallet.tronkit.network.Network
import io.cheddarswallet.tronkit.network.TronGridService
import io.cheddarswallet.tronkit.toBigInteger
import io.cheddarswallet.tronkit.toHexString

class Trc20Provider(
    private val tronGridService: TronGridService
) {
    class TokenNotFoundException : Throwable()

    suspend fun getTokenInfo(contractAddress: Address): TokenInfo {
        val name = getTokenName(contractAddress)
        val symbol = getTokenSymbol(contractAddress)
        val decimals = getDecimals(contractAddress)

        return TokenInfo(name, symbol, decimals)
    }

    suspend fun getDecimals(contractAddress: Address): Int {
        val response = tronGridService.ethCall(
            contractAddress = "0x${contractAddress.hex}",
            data = DecimalsMethod().encodedABI().toHexString()
        )
        if (response.isEmpty()) throw TokenNotFoundException()

        return response.sliceArray(IntRange(0, 31)).toBigInteger().toInt()
    }

    suspend fun getTokenSymbol(contractAddress: Address): String {
        val response = tronGridService.ethCall(
            contractAddress = "0x${contractAddress.hex}",
            data = SymbolMethod().encodedABI().toHexString()
        )

        if (response.isEmpty()) throw TokenNotFoundException()

        val argumentTypes = listOf(ByteArray::class)
        val parsedArguments = ContractMethodHelper.decodeABI(response, argumentTypes)
        val stringBytes = parsedArguments[0] as? ByteArray ?: throw TokenNotFoundException()

        return String(stringBytes)
    }

    suspend fun getTokenName(contractAddress: Address): String {
        val response = tronGridService.ethCall(
            contractAddress = "0x${contractAddress.hex}",
            data = NameMethod().encodedABI().toHexString()
        )

        if (response.isEmpty()) throw TokenNotFoundException()

        val argumentTypes = listOf(ByteArray::class)
        val parsedArguments = ContractMethodHelper.decodeABI(response, argumentTypes)
        val stringBytes = parsedArguments[0] as? ByteArray ?: throw TokenNotFoundException()

        return String(stringBytes)
    }

    companion object {
        fun getInstance(network: Network, tronGridApiKeys: List<String>): Trc20Provider {
            val apiKeyProvider = ApiKeyProvider(tronGridApiKeys)
            val tronGridService = TronGridService(network, apiKeyProvider)
            return Trc20Provider(tronGridService)
        }
    }

}
