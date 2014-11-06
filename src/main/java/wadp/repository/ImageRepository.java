package wadp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wadp.domain.Image;

public interface ImageRepository extends JpaRepository<Image, Long>{
    
}
