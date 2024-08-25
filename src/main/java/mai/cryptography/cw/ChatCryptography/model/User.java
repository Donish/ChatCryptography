package mai.cryptography.cw.ChatCryptography.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
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

    @Column(name = "username", unique = true)
    @NotBlank(message = "username can't be blank")
    @NotNull
    @EqualsAndHashCode.Include
    private String username;

    @Column(name = "password")
    @NotBlank(message = "password can't be blank")
    @NotNull
    @EqualsAndHashCode.Include
    private String password;

    @JsonIgnore
    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    private Set<Room> rooms = new HashSet<>();
}
