package com.easystation.infra.scheduler;

import com.easystation.infra.service.HostService;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Scheduler for periodic host reachability checks.
 */
@ApplicationScoped
public class HostReachabilityScheduler {

    @Inject
    HostService hostService;

    /**
     * Check reachability of all hosts every 5 minutes.
     * Can be disabled via configuration: quarkus.scheduler.enabled=false
     */
    @Scheduled(every = "5m", identity = "host-reachability-check")
    void checkAllHosts() {
        Log.info("Starting scheduled host reachability check...");
        try {
            var results = hostService.checkReachabilityAll();
            int online = (int) results.stream().filter(h -> h.status().name().equals("ONLINE")).count();
            int offline = results.size() - online;
            Log.infof("Host reachability check completed: %d online, %d offline", online, offline);
        } catch (Exception e) {
            Log.errorf("Failed to check host reachability: %s", e.getMessage());
        }
    }
}
