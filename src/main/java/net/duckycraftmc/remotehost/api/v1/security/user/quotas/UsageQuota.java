package net.duckycraftmc.remotehost.api.v1.security.user.quotas;

import net.duckycraftmc.remotehost.api.v1.security.user.AccountTier;

public abstract class UsageQuota {

    public abstract boolean CAN_USE_PLUGIN_SERVERS();
    public abstract boolean CAN_USE_MODDED_SERVERS();

    public abstract boolean CAN_UPLOAD_WORLDS();

    public abstract int MAX_SERVERS();
    public abstract int MAX_PROXIES();
    public abstract int MAX_RAM();

    public abstract boolean CAN_ADD_CO_OWNERS();

    public UsageQuota getQuotas(AccountTier tier) {
        return switch (tier) {
            case BASIC -> new BasicQuota();
            case ENHANCED -> new EnhancedQuota();
            case ADVANCED -> new AdvancedQuota();
            case FULL -> new FullQuota();
            case ADMIN -> new AdminQuota();
            default -> throw new IllegalStateException("Unexpected value: " + tier);
        };
    }

    private static UsageQuota INSTANCE;

    public static UsageQuota getInstance() {
        if (INSTANCE == null)
            INSTANCE = new UsageQuota() {
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
                    return false;
                }

                @Override
                public int MAX_SERVERS() {
                    return 0;
                }

                @Override
                public int MAX_PROXIES() {
                    return 0;
                }

                @Override
                public int MAX_RAM() {
                    return 0;
                }

                @Override
                public boolean CAN_ADD_CO_OWNERS() {
                    return false;
                }
            };
        return INSTANCE;
    }

}
