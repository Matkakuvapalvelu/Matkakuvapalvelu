package wadp.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import wadp.Application;
import wadp.repository.UserRepository;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class UserServiceTest {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository userRepository;


    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void newUserIsCreated() {
        service.createUser("Foo", "barbarbar");

        assertNotNull(userRepository.findByUsername("Foo"));
    }

}
