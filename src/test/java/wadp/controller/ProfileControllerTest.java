package wadp.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import wadp.Application;
import wadp.controller.utility.MockMvcTesting;
import wadp.domain.Notification;
import wadp.domain.User;
import wadp.service.NotificationService;
import wadp.service.UserService;

import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProfileControllerTest {

    private final String URI = "/profile";

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private FilterChainProxy springSecurityFilter;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    private MockMvcTesting mockMvcTesting;
    private User loggedInUser;
    private User other;

    @Before
    public void setUp() {
        mockMvcTesting = new MockMvcTesting(webAppContext, springSecurityFilter);
        loggedInUser = userService.createUser("loginuser", "loginuser");
        other = userService.createUser("otheruser", "otheruser");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loggedInUser.getUsername(), loggedInUser.getPassword()));

        notificationService.createNewNotification("reason", "text", other, loggedInUser);
        notificationService.createNewNotification("reason2", "text2", other, loggedInUser);
        Notification n = notificationService.createNewNotification("reason3", "text3", other, loggedInUser);
        n.setRead(true);
        notificationService.updateNotification(n);

        notificationService.createNewNotification("reason4", "text4", loggedInUser, other);
    }

    @Test
    public void userDataIsAddedToModelWhenRequestingProfilePageForSelf() throws Exception {
        MvcResult res = mockMvcTesting.makeGet(URI, "profile", "user", "unreadnotifications", "trips");
        Long readCount = (Long)res.getModelAndView().getModel().get("unreadnotifications");
        assertEquals((Long)2l, readCount);
        assertNull(res.getModelAndView().getModel().get("canrequestfriendship"));
        User modelUser = (User)res.getModelAndView().getModel().get("user");
        assertEquals(loggedInUser.getId(), modelUser.getId());
    }

    @Test
    public void userDataIsAddedToModelWhenRequestingProfilePageForOther() throws Exception {
        MvcResult res = mockMvcTesting.makeGet(URI + "/" + other.getId(), "profile", "user", "canrequestfriendship", "trips");
        Boolean canrequestfriendship = (Boolean)res.getModelAndView().getModel().get("canrequestfriendship");

        assertEquals(true, canrequestfriendship);
        assertNull(res.getModelAndView().getModel().get("unreadnotifications"));
        User modelUser = (User)res.getModelAndView().getModel().get("user");
        assertEquals(other.getId(), modelUser.getId());
    }

}
