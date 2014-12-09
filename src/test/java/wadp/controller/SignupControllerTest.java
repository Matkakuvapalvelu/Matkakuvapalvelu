package wadp.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import wadp.Application;
import wadp.controller.utility.MockMvcTesting;
import wadp.domain.User;
import wadp.domain.form.UserForm;
import wadp.service.UserService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SignupControllerTest {

    private final String URI = "/signup";

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private FilterChainProxy springSecurityFilter;

    private MockMvcTesting mockMvcTesting;

    @Autowired
    private UserService userService;

    @Before
    public void setUp() {
        mockMvcTesting = new MockMvcTesting(webAppContext, springSecurityFilter);
    }

    @Test
    public void userSignupPageIsShown() throws Exception {
        MvcResult res = mockMvcTesting.makeGet(URI, "signup", "user");
        assertNotNull(res.getModelAndView().getModel().get("user"));
        assertTrue(res.getModelAndView().getModel().get("user") instanceof UserForm);
    }

    @Test
    public void canCreateNewUser() throws Exception {

        Map<String, String> parameters = new HashMap<>();
        parameters.put("username", "foobar");
        parameters.put("password", "happyfluffybunnies");
        parameters.put("confirmpassword", "happyfluffybunnies");
        mockMvcTesting.makePost(URI, "index", parameters);

        assertEquals(1, userService.getUsers().size());
        User user = userService.getUsers().get(0);
        assertEquals("foobar", user.getUsername());
        assertTrue(user.passwordEquals("happyfluffybunnies"));
    }

    @Test
    public void doesNotCreateUserAndGivesErrorMessageIfPasswordIsTooShort() throws Exception {

        Map<String, String> parameters = new HashMap<>();
        parameters.put("username", "foobar");
        parameters.put("password", "short");
        parameters.put("confirmpassword", "short");
        mockMvcTesting.makePostExpectErrors(URI, "signup", parameters, "user");

        assertEquals(0, userService.getUsers().size());
    }

    @Test
    public void doesNotCreateUserAndGivesErrorMessageIfPasswordsDoNotMatch() throws Exception {

        Map<String, String> parameters = new HashMap<>();
        parameters.put("username", "foobar");
        parameters.put("password", "verygoodpassword");
        parameters.put("confirmpassword", "verybadpassword");
        mockMvcTesting.makePostExpectErrors(URI, "signup", parameters, "user");

        assertEquals(0, userService.getUsers().size());
    }

    @Test
    public void doesNotCreateUserAndGivesErrorMessageIfUsernameIsTaken() throws Exception {

        userService.createUser("foobar", "averygoodpassword");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("username", "foobar");
        parameters.put("password", "happyfluffybunnies");
        parameters.put("confirmpassword", "happyfluffybunnies");
        mockMvcTesting.makePostExpectErrors(URI, "signup", parameters, "user");

        assertEquals(1, userService.getUsers().size());
        assertTrue(userService.getUsers().get(0).passwordEquals("averygoodpassword"));
    }

}
