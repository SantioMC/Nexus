package me.santio.nexus.io

//import lombok.experimental.Accessors;
//import lombok.extern.slf4j.Slf4j;
//import me.santio.nexus.packets.NexusPacket;
//import net.kyori.adventure.text.Component;
//import net.kyori.adventure.text.event.HoverEvent;
//import net.kyori.adventure.text.format.NamedTextColor;
//import org.bukkit.Bukkit;
//import org.bukkit.entity.Player;
//import org.checkerframework.checker.nullness.qual.Nullable;
//import org.jetbrains.annotations.NotNull;
//
//import java.lang.reflect.Field;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
/**** */ // * Handles debugging events being sent over the cluster
// * @author santio
// */
//@Slf4j
//@Accessors(fluent = true)
//public class PacketDebugger {
//    
//    private final Map<UUID, @Nullable String> debugging = new HashMap<>();
//    
//    /**
//     * Mark the player as debugging, if null is passed for the event class then the player
//     * will be subscribed to all events
//     * @param uuid The unique id of the player
//     * @param clazz The class of the event to subscribe to, or null for all events
//     */
//    public void setDebugging(UUID uuid, @Nullable String clazz) {
//        debugging.put(uuid, clazz);
//    }
//    
//    /**
//     * Checks if the player is debugging for the specific event
//     * @param uuid The unique id of the player
//     * @param clazz The event class to check for, or null to check if any
//     * @return Whether the player is debugging the specified event
//     */
//    public boolean isDebugging(UUID uuid, @Nullable String clazz) {
//        if (!debugging.containsKey(uuid)) return false;
//        if (clazz == null) return debugging.containsKey(uuid);
//        
//        final @Nullable String debuggingClass = debugging.get(uuid);
//        if (debuggingClass == null) return true;
//        
//        return debuggingClass.equalsIgnoreCase(clazz);
//    }
//    
//    /**
//     * Removes the player from debugging events
//     * @param uniqueId The unique id of the player to remove
//     */
//    public void stopDebugging(@NotNull UUID uniqueId) {
//        debugging.remove(uniqueId);
//    }
//    
//    /**
//     * Broadcast a debug message to all players who are debugging
//     * @param event The nexus event to push out
//     */
//    public void broadcast(NexusPacket event) {
//        final List<? extends Player> listeners = Bukkit.getOnlinePlayers()
//            .stream()
//            .filter(player -> this.isDebugging(player.getUniqueId(), event.getClass().getSimpleName()))
//            .toList();
//        
//        if (listeners.isEmpty()) return;
//        Component hover = Component.newline();
//        
//        try {
//            for (Field field : event.getClass().getDeclaredFields()) {
//                field.setAccessible(true);
//                hover = hover.append(Component.text(field.getName(), NamedTextColor.GOLD))
//                    .append(Component.text(": ", NamedTextColor.GRAY))
//                    .append(Component.text(field.get(event).toString(), NamedTextColor.YELLOW))
//                    .appendNewline();
//            }
//        } catch (Exception e) {
//            log.error("Failed to decode field", e);
//            hover = Component.text("Failed to build component", NamedTextColor.RED);
//        }
//        
//        final Component component = Component.text("- ", NamedTextColor.DARK_GRAY)
//            .append(Component.text(event.getClass().getSimpleName(), NamedTextColor.AQUA))
//            .hoverEvent(HoverEvent.showText(hover));
//        
//        listeners.forEach(player -> player.sendMessage(component));
//    }
//}

