package wadp.domain;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
public class Trip extends AbstractPersistable<Long> {

    @OneToMany
    private List<Comment> comments;
}
