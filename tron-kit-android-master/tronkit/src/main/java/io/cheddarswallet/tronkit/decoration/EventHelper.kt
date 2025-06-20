package io.cheddarswallet.tronkit.decoration

import io.cheddarswallet.tronkit.decoration.trc20.Trc20ApproveEvent
import io.cheddarswallet.tronkit.decoration.trc20.Trc20TransferEvent
import io.cheddarswallet.tronkit.models.Trc20EventRecord

object EventHelper {

    fun eventFromRecord(record: Trc20EventRecord): Event? = when (record.type) {
        "Transfer" -> Trc20TransferEvent(record)
        "Approval" -> Trc20ApproveEvent(record)
        else -> null
    }

}
