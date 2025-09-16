# Nexus Development Guide

To better support backwards compatability, events and data should be retrieved from the Bukkit or 
Paper API whenever possible, whenever possible Nexus should aim to support the latest stable version
of PaperMC, and rely on plugins like ViaVersion for backwards compatability.

In the context of events, Bukkit events should be preferred as mutations by other plugins will be
performed before synchronizing the event across servers. Realistically every event should be on
the `MONITOR` event priority while making sure to ignore any cancellable event.

## Plugin Support

While fundamentally not possible, Nexus should attempt to make existing plugins just "work" whenever
possible, however this obviously isn't possible in all cases (for example, cross-server context
simply doesn't exist without hooking into Nexus' API.)

## Chat Signing

Chat signing by default is perfectly supported on Nexus, this obviously can be disabled by plugins such as 
[FreedomChat](https://modrinth.com/plugin/freedomchat), however as Nexus is trying to attempt to replicate behaviour as
close to vanilla as possible, support for chat signing is going to be added.

## NMS / PacketEvents usage

Whenever possible, working in the Paper API context is always going to be better than NMS or PacketEvents as we allow
for events to be ran through the Paper Event API allowing for plugins to perform modifications before we send them out
across servers. However, in situations where that may not be possible, working with PacketEvents should be preferred 
over NMS as working with it provides a highly volatile API which is prone to significant changes between versions. These
changes make maintaining Nexus harder and consume more time, however it isn't always avoidable.

## Asynchronous Operations

Nexus by default runs everything it does under two virtual thread pools
- `nexus-io-x` - The thread pool used when handling messages, such as parsing, compressing, decompressing, etc. You
normally don't need to interact with this thread pool

- `nexus-handle-x` - The main operation pool, used for handling events, this is the main thread pool used for 
essentially everything else on Nexus. This should be kept in mind when working with Nexus and it's APIs as most work is
done off the main server thread. Working with the API will typically context switch for you when necessary (such 
as publishing a packet)

When working on Nexus, it's necessary to keep this in mind when interacting with Bukkit, as a significant part of the 
API is not thread safe, when you need to switch back to the main thread, do so when you need to do interactions with the
Bukkit API (try seeing if the API you're working with is thread safe first), all other work should be kept to the
Nexus handle thread pool (or the Bukkit async scheduler pool)