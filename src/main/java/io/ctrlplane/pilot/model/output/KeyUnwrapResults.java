package io.ctrlplane.pilot.model.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyUnwrapResults {

    @JsonProperty("optsdata")
    byte[] optsData;

}
