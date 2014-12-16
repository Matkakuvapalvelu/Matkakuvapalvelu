package wadp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.domain.Image;
import wadp.domain.Post;
import wadp.domain.Trip;
import wadp.domain.User;
import wadp.repository.PostRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

// postgresql barfs without the @Transactional annotation as images might be split into multiple values inside database
// and therefore database needs multiple queries to fetch all the parts -> requires transaction for safety

/**
 * Service that handles anything post related, such as creating and listing posts
 */
@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    TripService tripService;

    /**
     * This method creates and saves new post into database and returns the resulting object.
     *
     * @param image Image associated with the post. This is mandatory and must not be null
     * @param imageText Image text. Can be empty
     * @param trips List of trips this image is associated with. Can be empty
     * @param poster User who created this post. Mandatory
     * @return Instance of post after it is saved to database
     */

    @Transactional
    public Post createPost(Image image, String imageText, List<Trip> trips, User poster) {

        if (image == null || trips == null) {
            throw new IllegalArgumentException("Image must not be null when creating new post");
        }

        Post post = new Post();
        post.setImageText(imageText);
        post.setImage(image);
        post.setPoster(poster);
        post.setTrips(trips);


        post = postRepository.save(post);

        for (Trip trip : trips) {
            trip.getPosts().add(post);
            tripService.updateTrip(trip, poster);
        }

        return post;
    }



    /**
     * This method returns list of posts made by given user
     *
     * @param user User whose posts are wanted
     * @return List of posts by the user
     */
    @Transactional
    public List<Post> getUserPosts(User user) {
        return postRepository.findByPoster(user);
    }

    /**
     *  Returns single post with given id or null if post does not exist
     * @param id Post id
     * @return Post with id or null if post does not exist
     */
    @Transactional
    public Post getPost(Long id) {
        return postRepository.findOne(id);
    }
    
    /**
     *  Returns List of newest posts from specific trip
     * @param trip trip which posts are wanted
     * @param top how many values we want
     * @return List of top posts
     */
    @Transactional
    public List<Post> getNewestPosts(Trip trip, int top) {
        return postRepository.findByTrips(trip, new PageRequest(0, top, Sort.Direction.DESC, "postDate"));
    }

    public void updatePost(Post post) {
        postRepository.save(post);
    }
}
