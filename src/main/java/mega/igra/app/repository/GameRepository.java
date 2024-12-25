package mega.igra.app.repository;

import mega.igra.app.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    boolean existsByKeysContaining(String key);
    List<Game> findBySoldFalse();
}

