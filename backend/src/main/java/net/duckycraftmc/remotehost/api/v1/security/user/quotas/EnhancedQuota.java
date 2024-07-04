package net.duckycraftmc.remotehost.api.v1.security.user.quotas;

public class EnhancedQuota extends UsageQuota {

    @Override
    public boolean CAN_USE_PLUGIN_SERVERS() {
        return true;
    }

    @Override
    public boolean CAN_USE_MODDED_SERVERS() {
        return true;
    }

    @Override
    public boolean CAN_UPLOAD_WORLDS() {
        return true;
    }

    @Override
    public int MAX_SERVERS() {
        return 2;
    }

    @Override
    public int MAX_PROXIES() {
        return 0;
    }

    @Override
    public int MAX_RAM() {
        return 5120;
    }

    @Override
    public boolean CAN_ADD_CO_OWNERS() {
        return true;
    }

}
