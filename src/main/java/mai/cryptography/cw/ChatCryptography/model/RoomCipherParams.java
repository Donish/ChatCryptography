package mai.cryptography.cw.ChatCryptography.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomCipherParams {

    @Column(name = "algorithm")
    @NotBlank(message = "algorithm can't be blank")
    @NotNull
    private String algorithm;

    @Column(name = "cipher_mode")
    @NotBlank(message = "cipher mode can't be blank")
    @NotNull
    private String cipherMode;

    @Column(name = "padding")
    @NotBlank(message = "padding can't be blank")
    @NotNull
    private String padding;

    @Column(name = "iv")
    @NotNull
    private byte[] IV;

    @Column(name = "primitive_root")
    @NotNull
    private byte[] g;

    @Column(name = "modulo")
    @NotNull
    private byte[] p;
}
