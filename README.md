<div align="center">

# Nexus

### Cross Server Synchronization

</div>

## What is Nexus?

Nexus is a Paper Plugin that allows you to horizontally scale your server, allowing you to distribute load between 
multiple instances, to allow for better performance at higher player counts while mimicking vanilla behaviour to 
provide seamless behaviour for players.

## How does it work?

Nexus will synchronize events between server nodes using [NATs](https://nats.io), where all servers will track
all entities and players between the servers. Each real player and entity on their respective server will be considered
the "master" server or source of truth for that entity & player and is responsible for handling interactions to that
entity. For example, if Player A sends a message in chat, all the handling for formatting and handling that chat message
falls under the responsibility of the server that the player is on, and the result forwarded to all other nodes.

## Installation Guide

Installing Nexus is very straightforward, this guide assumes you have basic understanding of configuring and setting
up NATs. Nexus is also designed to be operated behind a load balancer (to distribute players between servers as evenly 
as possible.), using a proxy like Velocity is recommended.

1. Install and configure NATs. 

    We recommend deploying NATs in a docker container, with proper authentication setup. See
    [the documentation](https://docs.nats.io/running-a-nats-service/introduction/installation#installing-via-docker) for 
    more information


2. Install ViaVersion on all your nodes.

   This is a required dependency for PacketEvents, which is used by Nexus.


> [!NOTE]  
> Nexus is designed to be run on the same Minecraft version on all servers, please make sure your server versions match!
3. Install Nexus on all your nodes, you'll need to restart to generate a configuration file where you'll put in the NATs
connection uri and specify the pool name (make sure the name is the same between all servers)


4. Once Nexus is installed on all nodes, you can give them a restart and try joining one of the nodes to ensure 
everything is working.

## Supported Features

Nexus is still in development, not all features in vanilla Minecraft are synchronized yet. This checklist should also 
not be considered inclusive of all features required for Nexus to mimic a completely vanilla server. Features on this
checklist may not be completed in the way they are ordered.

### Feature Checklist

- [x] Player Synchronization
- [x] Tablist Synchronization
- [x] Cross-Server PvP (punching)
- [x] Cross-Server Chat Signing
- [ ] Proper world synchronization
- [ ] Entity Synchronization (requires optimizing movements)
- [ ] Item dropping & pickup between servers
- [ ] Making sure entities like arrows work between servers
- [ ] Synchronizing sounds & particles between servers
- [ ] Performance Testing
- [ ] Synchronize messages (/say, advancements, etc.) *(selectors like @a will not be synchronized)*
- [ ] And a lot more

## Plugin Support

Since Nexus is a Paper plugin, other plugins should generally work well, however since a significant amount of plugins
are not made with scaling in mind, issues are bound to arise. These issues are generally because of a lack of context
of players on a server. For example `Bukkit.getOnlinePlayers()` will only return the online players for that node, not 
the server cluster. Nexus provides an API to resolve these issues, however support for other plugins shouldn't be fully
expected out of the box and may require work on your end to support.

## Contributing to Nexus

Contributions to Nexus are always welcomed, please feel free to open a pull request fixing issues, supporting more
features, optimizing tasks, or adding backwards compatability for Minecraft/Player versions. Check out the [development
documentation](./docs/DEVELOPMENT.md)

Not a developer? You're more than welcome to help test Nexus, either by just test around with some of your own accounts
or by testing with a larger group of people. If you want to get in contact, feel free to shoot me a discord message,
my DMs are open: `@santio.`