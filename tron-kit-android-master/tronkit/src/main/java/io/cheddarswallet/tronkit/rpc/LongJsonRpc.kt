package io.cheddarswallet.tronkit.rpc

open class LongJsonRpc(
    method: String, params: List<Any>
) : JsonRpc<Long>(method, params) {
    @Transient
    override val typeOfResult = Long::class.java
}
