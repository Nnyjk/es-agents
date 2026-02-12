package com.easystation;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Main implements QuarkusApplication {
    public static void main(String... args) {
        Log.info("Main.main() called");
        Quarkus.run(Main.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        Log.info("Main.run() called - HTTP Server should be ready");
        Quarkus.waitForExit();
        return 0;
    }
}
