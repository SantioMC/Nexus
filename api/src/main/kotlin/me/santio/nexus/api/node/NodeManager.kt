package me.santio.nexus.api.node

/**
 * A manager for holding information about nodes, this is used to keep track of
 * which nodes are online and what their status is at.
 * @author santio
 */
interface NodeManager {
    /**
     * Get a node representation by its unique identifier
     * @param id The node identifier
     * @return The node representation, or null if not found
     */
    fun get(id: String): Node?

    /**
     * @return The list of all known nodes
     */
    fun all(): List<Node>

    /**
     * Get the amount of known nodes
     * @return The amount of nodes known to this server
     */
    fun size(): Int

    /**
     * Elect the specified node, this will alert all other nodes of the new election
     * @param node The node to elect
     */
    fun elect(node: String)

    /**
     * Send a message to all nodes saying that this current node is taking over as master
     */
    fun electSelf()

    /**
     * Send a message to all nodes saying that the node with the lowest MSPT is becoming elected.
     * @param exclusions The list of nodes to ignore when choosing the best node
     */
    fun electBest(exclusions: List<String> = emptyList())

    /**
     * Get the current master node, or null if no node is currently elected
     * @return The master node, or null if none is elected.
     */
    fun master(): Node?

    /**
     * Get the node representation of this current server
     * @return The [Node], or null if not yet registered
     */
    fun self(): Node?
}
