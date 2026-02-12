package com.easystation.system.record;

import java.util.UUID;

public record ModuleActionRecord(
    UUID id,
    UUID moduleId,
    String code,
    String name
) {}
