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

    @Column(name = "second_user")
    @NotNull
    @EqualsAndHashCode.Include
    private Long secondUser;
}
