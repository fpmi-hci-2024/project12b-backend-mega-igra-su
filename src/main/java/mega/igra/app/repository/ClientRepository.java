package mega.igra.app.repository;

import mega.igra.app.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByLogin(String login);

    boolean existsByNickname(String nickname);
}
