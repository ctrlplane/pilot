package io.ctrlplane.pilot.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import io.ctrlplane.pilot.crypt.AesCipher;
import io.ctrlplane.pilot.crypt.CipherResult;
import io.ctrlplane.pilot.keyprovider.KeyProviderServiceGrpc;
import io.ctrlplane.pilot.keyprovider.keyProviderKeyWrapProtocolInput;
import io.ctrlplane.pilot.keyprovider.keyProviderKeyWrapProtocolOutput;
import io.ctrlplane.pilot.model.common.Annotation;
import io.ctrlplane.pilot.model.input.WrapInput;
import io.ctrlplane.pilot.model.output.KeyUnwrapResults;
import io.ctrlplane.pilot.model.output.KeyWrapResults;
import io.ctrlplane.pilot.model.output.WrapOutput;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

    /**
     * Constructor.
     *
     * @param encryptor The cipher implementation.
     */
    @Autowired
    public KeyProviderService(AesCipher encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public void wrapKey(
            final keyProviderKeyWrapProtocolInput request,
            final StreamObserver<keyProviderKeyWrapProtocolOutput> responseObserver) {
        try {
            final WrapInput input = convertInput(request);
            final byte[] data =
                    input.getKeyWrapParams().getOptsData();
            final CipherResult result =
                    this.encryptor.encrypt(data);
            final keyProviderKeyWrapProtocolOutput response =
                    convertWrapOutput(
                            result.getIv(),
                            result.getCiphertext());
            responseObserver.onNext(response);
        } catch (final Exception e) {
            LOG.warn("Error wrapping", e);
            responseObserver.onError(
                    new StatusRuntimeException(Status.INVALID_ARGUMENT));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void unWrapKey(
            final keyProviderKeyWrapProtocolInput request,
            final StreamObserver<keyProviderKeyWrapProtocolOutput> responseObserver) {
        try {
            final WrapInput input = convertInput(request);
            final Annotation annotation =
                    deserializeAnnotation(
                            input.getKeyUnWrapParams()
                                    .getAnnotation());
            final keyProviderKeyWrapProtocolOutput response =
                    convertUnwrapOutput(
                            this.encryptor.decrypt(
                                    annotation.getIv(),
                                    annotation.getWrappedKey()));
            responseObserver.onNext(response);
        } catch (final Exception e) {
            LOG.warn("Error unwrapping", e);
            responseObserver.onError(
                    new StatusRuntimeException(Status.INVALID_ARGUMENT));
        }
        responseObserver.onCompleted();
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
     * @param iv The encryption IV.
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
}
