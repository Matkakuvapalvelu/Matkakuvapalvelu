package wadp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import wadp.domain.User;
import wadp.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void createUser(String username, String password) {

        if (userRepository.findByUsername(username) != null) {
            throw new UsernameAlreadyTakenException();
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

    }

}
