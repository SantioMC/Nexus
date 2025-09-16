package me.santio.nexus.api

/**
 * Represents a custom packet in Nexus that's sent between the cluster.
 *
 * Creating custom packets requires them to be serializable, this is done by making
 * the class serializable with Kotlinx Serialization, meaning they need to be written in Kotlin.
 *
 * @author santio
 */
interface NexusPacket