package wadp.domain;


import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Post domain class. Contains reference to image associated with the post as well as possible image text and user
 * comments to images
 */
@Entity
public class Post extends AbstractPersistable<Long> {
/**
 * Following classes are yet to be implemented, so this is crude sketching for now
    @OneToOne
    private TravelImage image;

    @OneToMany
    private List<Comment> comments;
 */
    @ManyToOne
    private User poster;

    private String imageText;

    @Temporal(TemporalType.TIMESTAMP)
    Date postDate;

}
