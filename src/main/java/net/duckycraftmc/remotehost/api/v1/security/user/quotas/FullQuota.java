package net.duckycraftmc.remotehost.api.v1.security.user.quotas;

public class FullQuota extends UsageQuota {

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
        return 10;
    }

    @Override
    public int MAX_PROXIES() {
        return 2;
    }

    @Override
    public int MAX_RAM() {
        return 20480;
    }

    @Override
    public boolean CAN_ADD_CO_OWNERS() {
        return true;
    }

}
