package io.ctrlplane.pilot.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Annotation {

    @JsonProperty
    byte[] wrappedKey;

    @JsonProperty
    String method;

}
