package io.ctrlplane.pilot.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/** A client for requesting keys from copilot. */
@Component
public class KeyRequestClient {

    /** The logger for this class. */
    private static final Logger LOG =
            LoggerFactory.getLogger(KeyRequestClient.class);

    /** The client that makes requests to copilot. */
    private final WebClient webClient;

    /**
     * Constructor.
     * @param builder The builder for a web client.
     * @param requestUrl The URL to send requests to.
     */
    public KeyRequestClient(
            final WebClient.Builder builder,
            @Value("${keyRequest.baseUrl}") final String requestUrl) {
        this.webClient =
                builder.baseUrl(requestUrl)
                        .build();
    }

    /**
     * Requests key with given ID from copilot.
     *
     * @param kekId The key ID.
     *
     * @return The key bytes.
     */
    @Cacheable("kek")
    public byte[] requestKey(final String kekId) {
        LOG.debug("Requesting key from copilot {}", kekId);
        return this.webClient.get()
                .uri("/{kekId}", kekId)
                .exchangeToMono(response -> {
                    final HttpStatus status = response.statusCode();
                    if (status.equals(HttpStatus.OK)) {
                        return response.bodyToMono(byte[].class);
                    } else {
                        LOG.info("Copilot response with code {}",
                                  status);
                        return response.createException()
                                .flatMap(Mono::error);
                    }
                })
                .onErrorResume(Mono::error)
                .block();
    }

}
