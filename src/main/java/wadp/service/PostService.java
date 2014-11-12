package wadp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.domain.Image;
import wadp.domain.Post;
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

    public Post createPost(Image image, String imageText, User poster) {

        if (image == null) {
            throw new IllegalArgumentException("Image must not be null when creating new post");
        }

        Post post = new Post();
        post.setImageText(imageText);
        post.setImage(image);
        post.setPoster(poster);

        return postRepository.save(post);
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
