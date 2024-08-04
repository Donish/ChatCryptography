package mai.cryptography.cw.ChatCryptography.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "username", unique = true) // ? TODO
    @NotBlank(message = "username can't be blank") // ?
    @NotNull // ?
    @EqualsAndHashCode.Include // ?
    private String username;

    @Column(name = "password")
    @NotBlank(message = "password can't be blank")
    @NotNull
    @EqualsAndHashCode.Include
    private String password;
}
