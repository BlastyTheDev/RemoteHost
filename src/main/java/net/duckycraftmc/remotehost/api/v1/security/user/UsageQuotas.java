package net.duckycraftmc.remotehost.api.v1.security.user;

public class UsageQuotas {

    // MAX_RAM is in MiB, the total amount of memory that can be allocated to all servers

    public static class Basic {

        public static final boolean CAN_USE_PLUGIN_SERVERS = false;
        public static final boolean CAN_USE_MODDED_SERVERS = false;

        public static final boolean CAN_UPLOAD_WORLDS = true;

        public static final int MAX_SERVERS = 1;
        public static final int MAX_PROXIES = 0;
        public static final int MAX_RAM = 2048;

        public static final boolean CAN_ADD_CO_OWNERS = false;

    }

    public static class Enhanced {

        public static final boolean CAN_USE_PLUGIN_SERVERS = true;
        public static final boolean CAN_USE_MODDED_SERVERS = true;

        public static final boolean CAN_UPLOAD_WORLDS = true;

        public static final int MAX_SERVERS = 2;
        public static final int MAX_PROXIES = 0;
        public static final int MAX_RAM = 5120;

        public static final boolean CAN_ADD_CO_OWNERS = true;

    }

    public static class Advanced {

        public static final boolean CAN_USE_PLUGIN_SERVERS = true;
        public static final boolean CAN_USE_MODDED_SERVERS = true;

        public static final boolean CAN_UPLOAD_WORLDS = true;

        public static final int MAX_SERVERS = 5;
        public static final int MAX_PROXIES = 1;
        public static final int MAX_RAM = 10240;

        public static final boolean CAN_ADD_CO_OWNERS = true;

    }

    public static class Full {

        public static final boolean CAN_USE_PLUGIN_SERVERS = true;
        public static final boolean CAN_USE_MODDED_SERVERS = true;

        public static final boolean CAN_UPLOAD_WORLDS = true;

        public static final int MAX_SERVERS = 10;
        public static final int MAX_PROXIES = 2;
        public static final int MAX_RAM = 20480;

        public static final boolean CAN_ADD_CO_OWNERS = true;

    }

}
