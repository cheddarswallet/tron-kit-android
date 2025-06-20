package io.cheddarswallet.tronkit.rpc

import com.google.gson.JsonElement

data class RpcResponse(val id: Int, val result: JsonElement?, val error: Error?) {
    data class Error(val code: Int, val message: String)
}
