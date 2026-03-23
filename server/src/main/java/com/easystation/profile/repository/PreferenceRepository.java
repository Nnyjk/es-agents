package com.easystation.profile.repository;

import com.easystation.profile.domain.UserPreference;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PreferenceRepository implements PanacheRepositoryBase<UserPreference, UUID> {

    public Optional<UserPreference> findByUserId(UUID userId) {
        return find("userId", userId).firstResultOptional();
    }
}