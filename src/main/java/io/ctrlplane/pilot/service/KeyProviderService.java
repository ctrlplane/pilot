package io.ctrlplane.pilot.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import io.ctrlplane.pilot.client.KeyRequestClient;
import io.ctrlplane.pilot.crypt.AesCipher;
import io.ctrlplane.pilot.crypt.CipherResult;
import io.ctrlplane.pilot.keyprovider.KeyProviderServiceGrpc;
import io.ctrlplane.pilot.keyprovider.keyProviderKeyWrapProtocolInput;
import io.ctrlplane.pilot.keyprovider.keyProviderKeyWrapProtocolOutput;
import io.ctrlplane.pilot.model.common.Annotation;
import io.ctrlplane.pilot.model.input.KeyUnwrapParams;
import io.ctrlplane.pilot.model.input.KeyWrapParams;
import io.ctrlplane.pilot.model.input.WrapInput;
import io.ctrlplane.pilot.model.output.KeyUnwrapResults;
import io.ctrlplane.pilot.model.output.KeyWrapResults;
import io.ctrlplane.pilot.model.output.WrapOutput;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** The GRPC implementation. */
@Component
public class KeyProviderService
        extends KeyProviderServiceGrpc.KeyProviderServiceImplBase {

    /** The logger for this class. */
    private static final Logger LOG =
            LoggerFactory.getLogger(KeyProviderService.class);

    /** The mapper for serializing JSON. */
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /** The cipher implementation for key wrapping/unwrapping. */
    private final AesCipher encryptor;

    private final KeyRequestClient keyRequestClient;

    /**
     * Constructor.
     *
     * @param encryptor        The cipher implementation.
     * @param keyRequestClient The web client for requesting key encryption keys
     *                         from copilot.
     */
    @Autowired
    public KeyProviderService(
            AesCipher encryptor,
            KeyRequestClient keyRequestClient) {
        this.encryptor = encryptor;
        this.keyRequestClient = keyRequestClient;
    }

    @Override
    public void wrapKey(
            final keyProviderKeyWrapProtocolInput request,
            final StreamObserver<keyProviderKeyWrapProtocolOutput> responseObserver) {
        handleErrors(
                responseObserver,
                () -> {
                    final WrapInput input = convertInput(request);
                    final KeyWrapParams params =
                            input.getKeyWrapParams();
                    final byte[] kek = getKek(params);
                    final byte[] data = params.getOptsData();
                    final CipherResult result =
                            this.encryptor.encrypt(kek, data);
                    final keyProviderKeyWrapProtocolOutput response =
                            convertWrapOutput(
                                    result.getIv(),
                                    result.getCiphertext());
                    responseObserver.onNext(response);
                });
        responseObserver.onCompleted();
    }

    @Override
    public void unWrapKey(
            final keyProviderKeyWrapProtocolInput request,
            final StreamObserver<keyProviderKeyWrapProtocolOutput> responseObserver) {
        handleErrors(
                responseObserver,
                () -> {
                    final WrapInput input = convertInput(request);
                    final byte[] key = getKek(input.getKeyUnWrapParams());
                    final Annotation annotation =
                            deserializeAnnotation(
                                    input.getKeyUnWrapParams()
                                            .getAnnotation());
                    final keyProviderKeyWrapProtocolOutput response =
                            convertUnwrapOutput(
                                    this.encryptor.decrypt(
                                            key,
                                            annotation.getIv(),
                                            annotation.getWrappedKey()));
                    responseObserver.onNext(response);
                });
        responseObserver.onCompleted();
    }

    /**
     * Convert an HTTP status code to a GRPC status.
     *
     * @param httpStatus The HTTP status code.
     *
     * @return The GRPC status.
     */
    private Status convertHttpStatus(final HttpStatus httpStatus) {
        final Status status;
        switch (httpStatus) {
            case FORBIDDEN:
                status = Status.PERMISSION_DENIED;
                break;
            case NOT_FOUND:
                status = Status.NOT_FOUND;
                break;
            case UNAUTHORIZED:
                status = Status.UNAUTHENTICATED;
                break;
            case BAD_REQUEST:
                status = Status.INVALID_ARGUMENT;
                break;
            case INTERNAL_SERVER_ERROR:
                status = Status.INTERNAL;
                break;
            default:
                status = Status.UNKNOWN;
        }
        return status;
    }

    /**
     * Converts the protobuf input into deserialized {@link WrapInput}.
     *
     * @param request The protobuf request.
     *
     * @return The input object.
     *
     * @throws IOException on deserialization error.
     */
    private WrapInput convertInput(
            final keyProviderKeyWrapProtocolInput request)
            throws IOException {
        final String inputJson =
                request.getKeyProviderKeyWrapProtocolInput()
                        .toStringUtf8();
        LOG.debug("Received input: {}", inputJson);
        return OBJECT_MAPPER.readValue(
                inputJson,
                WrapInput.class);
    }

    /**
     * Converts the output from a wrap operation into protobuf response.
     *
     * @param iv         The encryption IV.
     * @param ciphertext The wrapped key.
     *
     * @return The response.
     *
     * @throws JsonProcessingException on serialization error.
     */
    private keyProviderKeyWrapProtocolOutput convertWrapOutput(
            final byte[] iv,
            final byte[] ciphertext)
            throws JsonProcessingException {
        final Annotation annotation =
                new Annotation(
                        iv,
                        "AES-GCM",
                        ciphertext);
        final WrapOutput output =
                new WrapOutput(
                        new KeyWrapResults(
                                serializeAnnotation(annotation)),
                        null);
        return convertOutput(output);
    }

    /**
     * Converts the output from a wrap operation into protobuf response.
     *
     * @param outputData The unwrapped key.
     *
     * @return The response.
     *
     * @throws JsonProcessingException on serialization error.
     */
    private keyProviderKeyWrapProtocolOutput convertUnwrapOutput(
            final byte[] outputData)
            throws JsonProcessingException {
        final WrapOutput output =
                new WrapOutput(
                        null,
                        new KeyUnwrapResults(outputData));
        return convertOutput(output);
    }

    /**
     * Converts a {@link WrapOutput} into protobuf response.
     *
     * @param output The output object.
     *
     * @return The response.
     *
     * @throws JsonProcessingException on serialization error.
     */
    private keyProviderKeyWrapProtocolOutput convertOutput(
            final WrapOutput output)
            throws JsonProcessingException {
        final String outputSer = OBJECT_MAPPER.writeValueAsString(output);
        LOG.debug("Sending response: {}", outputSer);
        return keyProviderKeyWrapProtocolOutput.newBuilder()
                .setKeyProviderKeyWrapProtocolOutput(
                        ByteString.copyFromUtf8(outputSer))
                .build();
    }

    /**
     * Deserializes into an {@link Annotation}.
     *
     * @param annotation The annotation bytes.
     *
     * @return The object.
     *
     * @throws IOException on deserialization error.
     */
    private Annotation deserializeAnnotation(
            final byte[] annotation)
            throws IOException {
        return OBJECT_MAPPER.readValue(annotation, Annotation.class);
    }

    /**
     * Gets a key-encryption key from wrap parameters.
     *
     * @param wrapParams The wrap parameters.
     *
     * @return The key bytes.
     */
    private byte[] getKek(final KeyWrapParams wrapParams) {
        final Map<String, byte[][]> parameterMap =
                wrapParams.getEncryptConfig().getParameters();
        return getKek(parameterMap);
    }

    /**
     * Gets a key-encryption key from unwrap parameters.
     *
     * @param unwrapParams The unwrap parameters.
     *
     * @return The key bytes.
     */
    private byte[] getKek(final KeyUnwrapParams unwrapParams) {
        final Map<String, byte[][]> parameterMap =
                unwrapParams.getDecryptConfig().getParameters();
        return getKek(parameterMap);
    }

    /**
     * Gets a key-encryption key from a parameters map.
     *
     * @param parameterMap The parameters.
     *
     * @return The key bytes.
     */
    private byte[] getKek(final Map<String, byte[][]> parameterMap) {
        final byte[][] pilotParam = parameterMap.get("pilot");
        if (pilotParam.length < 1) {
            throw new IllegalArgumentException(
                    "Argument required with key pilot");
        }
        return this.keyRequestClient.requestKey(
                new String(pilotParam[0], StandardCharsets.UTF_8));
    }

    /**
     * Handles errors and passes off to response observer.
     *
     * @param responseObserver The response observer.
     * @param wrapMethod       The method to wrap in error handling.
     */
    private void handleErrors(
            final StreamObserver<?> responseObserver,
            final WrapInterface wrapMethod) {
        try {
            wrapMethod.execute();
        } catch (final WebClientResponseException e) {
            LOG.error("Response {}: {}",
                      e.getStatusCode(), e.getStatusText());
            responseObserver.onError(
                    convertHttpStatus(e.getStatusCode())
                            .asRuntimeException());
        } catch (final WebClientRequestException e) {
            LOG.error("Request error", e);
            responseObserver.onError(
                    Status.UNAVAILABLE
                            .withDescription("Error connecting to copilot")
                            .asRuntimeException());
        } catch (final Exception e) {
            LOG.warn("Error during encryption", e);
            responseObserver.onError(
                    Status.UNKNOWN.withDescription("Unknown error occurred")
                            .asRuntimeException());
        }
    }

    /**
     * Serializes an {@link Annotation} into bytes.
     *
     * @param annotation The object.
     *
     * @return The serialized bytes.
     *
     * @throws JsonProcessingException on serialization error.
     */
    private byte[] serializeAnnotation(
            final Annotation annotation)
            throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsBytes(annotation);
    }

    /** An interface for passing wrap methods into error handling.*/
    @FunctionalInterface
    private interface WrapInterface {

        /**
         * Executes the wrap method.
         *
         * @throws Exception on error.
         */
        void execute() throws Exception;
    }
}
