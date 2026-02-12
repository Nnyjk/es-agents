package com.easystation.system.record;

import java.util.Set;
import java.util.UUID;

public record RoleRecord(
    UUID id,
    String code,
    String name,
    String description,
    Set<UUID> moduleIds,
    Set<UUID> actionIds
) {}
