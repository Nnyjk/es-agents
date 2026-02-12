package com.easystation.auth.record;

import java.util.List;

public record RouteRecord(
    String path,
    String name,
    String icon,
    List<RouteRecord> routes
) {}
