package io.ctrlplane.pilot.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** The server that provides the GRPC key provider. */
@Component
public class KeyProviderServer {

    /** The logger for this class. */
    private static final Logger LOG =
            LoggerFactory.getLogger(KeyProviderServer.class);

    private final KeyProviderService service;

    /** The port for the gRPC server to listen to. */
    private final int port;

    /** The server for the key provider. */
    private Server server;

    @Autowired
    public KeyProviderServer(
            final KeyProviderService service,
            @Value("${grpc.port}") final int port) {
        this.service = service;
        this.port = port;
    }

    /** Initializes the server. */
    @PostConstruct
    public void init() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                start();
                blockUntilShutdown();
            } catch (final IOException e) {
                LOG.error("Error starting server", e);
            } catch (final InterruptedException e) {
                LOG.warn("Server execution interrupted", e);
            }
        });

    }

    /**
     * Starts the server.
     *
     * @throws IOException on error starting server.
     */
    private void start()
            throws IOException {
        server = ServerBuilder.forPort(this.port)
                .addService(this.service)
                .build()
                .start();
        LOG.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println(
                    "*** shutting down gRPC server since JVM is shutting down");
            try {
                KeyProviderServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
    }

    /**
     * Stops the server.
     *
     * @throws InterruptedException on shutdown error.
     */
    private void stop()
            throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon
     * threads.
     */
    private void blockUntilShutdown()
            throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

}
