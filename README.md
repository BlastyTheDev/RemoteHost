# RemoteHost (WIP)

### How to Use (if you want to use it while it's in alpha)
1. Clone the repository into your IDE (IntelliJ IDEA is used for this guide)
2. Add maven to the project using the `Add Framework Support` option in the menu opened by double pressing shift
3. Click the reload button in the top right of the maven tab to load dependencies
4. Set up an SQL database and enter the details in the `application.yml` file
5. Create a file called `.env` and set the following variables:
    - JWT_SIGNING_KEY: a 256bit key for signing JWTs
    - DISCORD_BOT_TOKEN: your Discord bot token
    - DISCORD_GUILD_ID: the ID of the Discord server you want to use for verification
6. Done! You can now use the API to host Minecraft servers and stuff.

You may want an ADMIN account to manage the servers and users. To do this you will have to:
1. Sign up for an account
2. Go to your SQL database and find the `users` table
3. Find your user and set the `role` column to `ADMIN`
4. Done! You now have an admin account that can create keys, manage servers, and manage users.

Keep in mind that the JWT authentication requires you to send a header with the key `Authorization` and the value `Bearer <your token>`. You will need this to access secured parts of the API.

### Notes
- The `RemoteHost` API is written in Java using Spring Boot.
- `RemoteHost` is written for Linux support. Windows support is currently not planned.
- The frontend is currently being written in plain HTML5, JS, and CSS. It is planned to be redesigned and rewritten using NextJS, after completion.

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
    - Proxy Softwares (Not Yet Implemented)
        - Velocity (all versions and builds available on https://papermc.io/downloads/all)
        - Waterfall (not yet implemented, will have all versions and builds available on https://papermc.io/downloads/all)
- Discord Bot for user verification
    - `RemoteHost` is intended for private use within known people who are in a Discord server. Discord user verification is intended make only Discord server members capable of receiving any account privileges
