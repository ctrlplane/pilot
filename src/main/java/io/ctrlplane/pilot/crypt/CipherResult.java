package io.ctrlplane.pilot.crypt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CipherResult {

    private final byte[] ciphertext;

    private final byte[] iv;

}
