package wadp.domain;


import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Post domain class. Contains reference to image associated with the post as well as possible image text and user
 * comments to images
 */
@Entity
public class Post extends AbstractPersistable<Long> {

    @OneToOne(fetch = FetchType.LAZY)
    private Image image;

    @OneToMany
    private List<Comment> comments;

    @ManyToOne
    private User poster;

    @ManyToMany(mappedBy = "posts")
    private List<Trip> trips;

    private String imageText;

    @Temporal(TemporalType.TIMESTAMP)
    private Date postDate;


    public Post() {
        postDate = new Date();
        comments = new ArrayList<>();
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public String getImageText() {
        return imageText;
    }

    public void setImageText(String imageText) {
        this.imageText = imageText;
    }

    public User getPoster() {
        return poster;
    }

    public void setPoster(User poster) {
        this.poster = poster;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }
}
