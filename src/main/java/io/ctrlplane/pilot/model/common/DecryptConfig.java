package io.ctrlplane.pilot.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class DecryptConfig {

    @JsonProperty
    Map<String, byte[][]> parameters;

}
