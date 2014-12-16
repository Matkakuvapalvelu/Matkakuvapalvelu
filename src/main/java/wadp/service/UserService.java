package wadp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import wadp.domain.User;
import wadp.repository.UserRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service that handles anything user related, such as creation, authentication and getting the authenticated user
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostService postService;

    /**
     * Creates and saves new user into database and returns the resulting object.
     *
     * @param username Username. Must be unique
     * @param password Password
     * @return Instance of User after it has been saved into the database
     * @throws wadp.service.UsernameAlreadyTakenException if username was taken
     */
    public User createUser(String username, String password) {

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username must not be null or empty");
        }

        username = username.trim();
        if (userRepository.findByUsername(username) != null) {
            throw new UsernameAlreadyTakenException();
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        return userRepository.save(user);
    }

    /**
     * Authenticates user with given username and password or throws if no such user exists
     *
     * @param username Username
     * @param password Password
     * @return User with matching username/password
     * @throws AuthenticationException If no user with username and password was found
     */
    public User authenticate(String username, String password) throws AuthenticationException {
        User user = userRepository.findByUsername(username);

        if (user == null || !user.passwordEquals(password)) {
            throw new AuthenticationException("Unable to authenticate user " + username) {
            };
        }

        return user;
    }

    /**
     * Returns the user who is logged in, or null if no user is currently logged in.
     *
     * @return Authenticated user or null if no authenticated user
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName());
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUser(Long id) {
        return userRepository.getOne(id);
    }

    /**
     *
     * Returns list of users ordered by activity, up to userCount users
     *
     * @param userCount number of active users
     * @return List of n active users, where n is usercount or number of users in system, whichever is smaller, ordered by
     * activity
     */
    public List<User> getMostActiveUsers(int userCount) {
        List<User> users = userRepository.findAll();
        final Map<User, Integer> userPostCounts = new HashMap<>();
        for (User u : users) {
            userPostCounts.put(u, postService.getUserPosts(u).size());
        }

        // really really really should be done on database level
        return users
                .stream()
                .sorted((o1, o2) -> {
                    int firstPostCommentCount = 0;
                    int secondPostCommentCount = 0;

                    firstPostCommentCount = o1.getComments().size() + userPostCounts.get(o1);
                    secondPostCommentCount = o2.getComments().size() +  userPostCounts.get(o2);

                    return secondPostCommentCount - firstPostCommentCount;
                })
                .limit(userCount).collect(Collectors.toList());
    }
}
