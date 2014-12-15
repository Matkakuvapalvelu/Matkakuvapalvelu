package wadp.repository;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import wadp.domain.Trip;
import wadp.domain.User;

public interface TripRepository extends JpaRepository<Trip, Long>{
    List<Trip> findByCreator(User creator);
    List<Trip> findByCreator(User creator, Sort sort);
}
