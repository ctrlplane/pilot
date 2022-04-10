package io.ctrlplane.pilot.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.ctrlplane.pilot.model.common.DecryptConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncryptConfig {

    @JsonProperty("Parameters")
    Map<String, byte[][]> parameters;

    @JsonProperty("DecryptConfig")
    DecryptConfig decryptConfig;

}
