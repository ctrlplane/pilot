package io.ctrlplane.pilot.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyWrapParams {

    @JsonProperty("optsdata")
    byte[] optsData;

    @JsonProperty("ec")
    EncryptConfig encryptConfig;
}
