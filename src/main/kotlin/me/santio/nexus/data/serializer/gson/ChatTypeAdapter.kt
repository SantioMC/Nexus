package me.santio.nexus.data.serializer.gson

import com.github.retrooper.packetevents.protocol.chat.ChatType
import com.github.retrooper.packetevents.protocol.chat.ChatTypes
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

internal object ChatTypeAdapter: TypeAdapter<ChatType>() {
    override fun write(out: JsonWriter, value: ChatType) {
        out.value(value.name.toString())
    }

    override fun read(`in`: JsonReader): ChatType {
        return ChatTypes.getByName(`in`.nextString())
    }
}