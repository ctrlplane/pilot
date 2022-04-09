package io.ctrlplane.pilot.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WrapInput {

    @JsonProperty
    OperationType op;

    @JsonProperty("keywrapparams")
    KeyWrapParams keyWrapParams;

    @JsonProperty("keyunwrapparams")
    KeyUnwrapParams keyUnWrapParams;

}
