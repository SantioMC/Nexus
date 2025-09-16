package me.santio.nexus.data.serializer.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.Instant

internal object InstantAdapter: TypeAdapter<Instant>() {
    override fun write(out: JsonWriter, value: Instant) {
        out.value(value.toEpochMilli())
    }

    override fun read(`in`: JsonReader): Instant {
        return Instant.ofEpochMilli(`in`.nextLong())
    }
}