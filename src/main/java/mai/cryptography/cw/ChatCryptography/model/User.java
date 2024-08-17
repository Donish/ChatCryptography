package mai.cryptography.cw.ChatCryptography.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @ElementCollection
    @EqualsAndHashCode.Include
    private List<Long> rooms = new ArrayList<>();
}
