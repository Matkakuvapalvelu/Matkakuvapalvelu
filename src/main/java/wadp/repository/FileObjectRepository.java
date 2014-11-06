package wadp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wadp.domain.FileObject;

public interface FileObjectRepository extends JpaRepository<FileObject, Long> {

}
