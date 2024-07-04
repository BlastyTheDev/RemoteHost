package net.duckycraftmc.remotehost.api.v1.security.key;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.user.AccountTier;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account_keys")
public class AccountKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String value;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountTier tier;
    @Column(nullable = false)
    private Boolean used;
    @Column(name = "user_id")
    private Integer userId; // user who used the key

}
