package io.cheddarswallet.tronkit.rpc

class GasPriceJsonRpc : LongJsonRpc(
        method = "eth_gasPrice",
        params = listOf()
)
