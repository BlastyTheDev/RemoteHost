package net.duckycraftmc.remotehost.api.v1.security.user.quotas;

public class BasicQuota extends UsageQuota {

    @Override
    public boolean CAN_USE_PLUGIN_SERVERS() {
        return false;
    }

    @Override
    public boolean CAN_USE_MODDED_SERVERS() {
        return false;
    }

    @Override
    public boolean CAN_UPLOAD_WORLDS() {
        return true;
    }

    @Override
    public int MAX_SERVERS() {
        return 1;
    }

    @Override
    public int MAX_PROXIES() {
        return 0;
    }

    @Override
    public int MAX_RAM() {
        return 2048;
    }

    @Override
    public boolean CAN_ADD_CO_OWNERS() {
        return false;
    }

}
