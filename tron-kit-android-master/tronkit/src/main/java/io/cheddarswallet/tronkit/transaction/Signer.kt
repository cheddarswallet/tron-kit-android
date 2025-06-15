package io.cheddarswallet.tronkit.transaction

import io.cheddarswallet.hdwalletkit.ECKey
import io.cheddarswallet.hdwalletkit.HDWallet
import io.cheddarswallet.tronkit.crypto.Utils
import io.cheddarswallet.tronkit.hexStringToByteArray
import io.cheddarswallet.tronkit.models.Address
import io.cheddarswallet.tronkit.network.CreatedTransaction
import io.cheddarswallet.tronkit.network.Network
import io.cheddarswallet.tronkit.toRawHexString
import java.math.BigInteger

class Signer(
    private val privateKey: BigInteger
) {

    fun sign(createdTransaction: CreatedTransaction): ByteArray {
        val rawTransactionHash = io.cheddarswallet.hdwalletkit.Utils.sha256(createdTransaction.raw_data_hex.hexStringToByteArray())

        return Utils.ellipticSign(rawTransactionHash, privateKey)
    }

    companion object {
        fun getInstance(seed: ByteArray, network: Network): Signer {
            return Signer(privateKey(seed, network))
        }

        fun privateKey(seed: ByteArray, network: Network): BigInteger {
            val hdWallet = HDWallet(seed, network.coinType, HDWallet.Purpose.BIP44)
            return hdWallet.privateKey(0, 0, true).privKey
        }

        fun address(privateKey: BigInteger, network: Network): Address {
            val publicKey = ECKey(privateKey, false).pubKey.drop(1).toByteArray()
            val raw = byteArrayOf(network.addressPrefixByte) + Utils.sha3(publicKey).takeLast(20).toByteArray()
            return Address(raw)
        }
    }
}
