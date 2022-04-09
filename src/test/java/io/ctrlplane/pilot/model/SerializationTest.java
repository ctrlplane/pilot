package io.ctrlplane.pilot.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ctrlplane.pilot.model.input.*;
import io.ctrlplane.pilot.model.output.KeyUnwrapResults;
import io.ctrlplane.pilot.model.output.KeyWrapResults;
import io.ctrlplane.pilot.model.output.WrapOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class SerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void testDeserializeWrapInput() throws JsonProcessingException {
        final Map<String, byte[][]> params =
                new HashMap<>();
        params.put("keyprovider-1",new byte[][]{
                {0x5, 0x6, 0x5, 0x6},
                {0x7, 0x8, 0x7, 0x8}});
        final WrapInput wrapInput =
                new WrapInput(OperationType.WRAP,
                        new KeyWrapParams(new byte[] {0x0, 0x1, 0x2, 0x3},
                                new EncryptConfig(params, null)),
                        null);
        final String serialized = this.objectMapper.writeValueAsString(wrapInput);
        System.out.println(serialized);
        final WrapInput deserialized =
                this.objectMapper.readValue(serialized, WrapInput.class);
    }

    @Test
    public void testDeserializeUnwrapInput() throws JsonProcessingException {
        final WrapInput wrapInput =
                new WrapInput(OperationType.UNWRAP,
                        null,
                        new KeyUnwrapParams(new byte[] {0x42, 0x43, 0x44}, null));
        final String serialized = this.objectMapper.writeValueAsString(wrapInput);
        System.out.println(serialized);
        final WrapInput deserialized =
                this.objectMapper.readValue(serialized, WrapInput.class);
    }

    @Test
    public void testSerializeWrapOutput() throws JsonProcessingException {
        final WrapOutput wrapOutput =
                new WrapOutput(
                        new KeyWrapResults(new byte[] {0x41, 0x42, 0x43}),
                        null);
        final String serialized =
                this.objectMapper.writeValueAsString(wrapOutput);
        System.out.println(serialized);
        final WrapOutput deserialized =
                this.objectMapper.readValue(serialized,
                        WrapOutput.class);
    }

    @Test
    public void testSerializeUnwrapOutput() throws JsonProcessingException {
        final WrapOutput wrapOutput =
                new WrapOutput(
                        null,
                        new KeyUnwrapResults(new byte[]{0x9, 0x9, 0x9}));
        final String serialized =
                this.objectMapper.writeValueAsString(wrapOutput);
        System.out.println(serialized);
        final WrapOutput deserialized =
                this.objectMapper.readValue(serialized,
                        WrapOutput.class);
    }

}
