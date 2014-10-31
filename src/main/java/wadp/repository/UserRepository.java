package wadp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import wadp.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    public User findByUsername(String username);

}
