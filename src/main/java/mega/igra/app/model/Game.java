package mega.igra.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double cost;

    @ElementCollection
    @CollectionTable(name = "game_keys", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "key")
    private List<String> keys;

    @Column(nullable = false)
    private boolean sold = false;
}
