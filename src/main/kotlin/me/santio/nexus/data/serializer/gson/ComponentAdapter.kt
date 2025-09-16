package me.santio.nexus.data.serializer.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer

internal object ComponentAdapter: TypeAdapter<Component>() {
    private val serializer = JSONComponentSerializer.json()

    override fun write(out: JsonWriter, value: Component?) {
        if (value == null) out.nullValue()
        else out.value(serializer.serialize(value))
    }

    override fun read(`in`: JsonReader): Component? {
        val next = `in`.peek()
        if (next == JsonToken.NULL) return null

        return serializer.deserialize(`in`.nextString())
    }
}