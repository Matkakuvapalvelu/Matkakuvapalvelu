package wadp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.domain.Image;
import wadp.domain.Post;
import wadp.domain.Trip;
import wadp.domain.User;
import wadp.repository.PostRepository;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that handles anything post related, such as creating and listing posts
 */
@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    TripService tripService;

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
            tripService.updateTrip(trip);
        }

        return post;
    }

    // postgresql barfs without the @Transactional annotation as images might be split into multiple values inside database
    // and therefore database needs multiple queries to fetch all the parts -> requires transaction for safety
    @Transactional
    public List<Post> getUserPosts(User user) {
        return postRepository.findByPoster(user);
    }

    @Transactional
    public Object getPost(Long id) {
        return postRepository.findOne(id);
    }
}
