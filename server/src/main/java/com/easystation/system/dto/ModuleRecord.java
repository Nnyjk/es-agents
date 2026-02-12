package com.easystation.system.record;

import com.easystation.system.domain.enums.ModuleType;
import java.util.UUID;

public record ModuleRecord(
    UUID id,
    String code,
    String name,
    ModuleType type,
    String path,
    UUID parentId,
    Integer sortOrder
) {}
