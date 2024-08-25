package mai.cryptography.cw.ChatCryptography.model;

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
@Table(name = "room")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "room_name", unique = true)
    @NotBlank(message = "room name can't be blank")
    @NotNull
    @EqualsAndHashCode.Include
    private String roomName;

    @Column(name = "creator_id")
    @NotNull
    @EqualsAndHashCode.Include
    private Long creatorUser;

    @Embedded
    @NotNull
    @EqualsAndHashCode.Include
    private RoomCipherParams roomCipherParams;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_room",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();
}
