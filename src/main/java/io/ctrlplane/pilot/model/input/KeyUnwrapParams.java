package io.ctrlplane.pilot.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.ctrlplane.pilot.model.common.DecryptConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyUnwrapParams {

    @JsonProperty
    byte[] annotation;

    @JsonProperty("dc")
    DecryptConfig decryptConfig;
}
