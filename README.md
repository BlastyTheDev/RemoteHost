# RemoteHost (WIP)

### (context for anyone from hackclub reading this)
This is intended to be a backend to host Minecraft servers and an SQL database to manage accounts, etc.. The planning for this project concluded on 24 February 2024 and we have ever since forgotten about this until early June. The original planned completion day was 24 June this year

### Features
- Token Authentication using Json Web Token (JWT)
- (Under development) API and Website/Webapp for Creation and Management of Minecraft Servers including:
    - Creation, Modification, Deletion, etc..
    - Automatic Server Jar Downloading
    - (Planned) Modrinth and Curseforge API for users to add plugins/mods/datapacks to their servers
- (Under development) More advanced access for admin accounts:
    - Ability to see and modify users
    - Ability to view all existing servers
    - Ability to view host machine load
- (Under development) User accounts able to create/modify servers depending on their "tier" and owner/co-owner status
    - Start/Stop/Restart servers
    - See and run commands in server console
    - See and change server whitelist
    - See and change server plugin/mod configurations
    - Add and remove users as co-owners (collaborators)
- (Under development) Currently available server/proxy types (Still in development, not ready to ship):
    - Server Softwares
        - Paper (all versions and builds available on https://papermc.io/downloads/all)
        - Purpur (all versions and builds available on https://papermc.io/downloads/all)
    - Proxy Softwares
        - Velocity (all versions and builds available on https://papermc.io/downloads/all)
        - Waterfall (not yet implemented, will have all versions and builds available on https://papermc.io/downloads/all)
- Discord Bot for user verification
    - `RemoteHost` is intended for private use within known people who are in a Discord server. Discord user verification is intended make only Discord server members capable of receiving any account privileges
