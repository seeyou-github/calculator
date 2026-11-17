package net.youapps.calcyou.persistence

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import net.you_apps.calcyou.CalculationHistory
import java.io.InputStream
import java.io.OutputStream

object CalculationHistorySerializer : Serializer<CalculationHistory> {
    override val defaultValue: CalculationHistory = CalculationHistory.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): CalculationHistory {
        try {
            return CalculationHistory.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: CalculationHistory, output: OutputStream) {
        return t.writeTo(output)
    }
}

val Context.calculationHistoryDataStore: DataStore<CalculationHistory> by dataStore(
    fileName = "calculation_history.pb",
    serializer = CalculationHistorySerializer
)
