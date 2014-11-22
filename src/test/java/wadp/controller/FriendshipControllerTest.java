package wadp.controller;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import wadp.Application;
import wadp.domain.Friendship;
import wadp.domain.User;
import wadp.repository.UserRepository;
import wadp.service.FriendshipService;
import wadp.service.UserService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FriendshipControllerTest {
    private final String URI = "/friendship";

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private FilterChainProxy springSecurityFilter;

    private MockMvc mockMvc;

    @Autowired
    private UserService userService;


    @Autowired
    private FriendshipService friendshipService;

    private class MockPrincipal implements Principal {

        public MockPrincipal(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object another) {
            if (another == null || !(another instanceof MockPrincipal)) {
                return false;
            }

            if (this == another) {
                return true;
            }

            return name.equals(((MockPrincipal)another).getName());

        }

        @Override
        public String toString() {
            return name.toString();
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String getName() {
            return name;
        }

        private String name;
    };

    // spring security and mockmvc won't play nicely together. This means we have to build our own mock http session
    // and add necessary authentication fields to it as we mock requests
    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webAppContext)
                .addFilter(springSecurityFilter, "/*")
                .build();

        webAppContext.getServletContext().setAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppContext);

        addTestData();
    }


    // create mock request so that user is authenticated correctly
    private MockHttpSession buildSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("USER"));

        // have to create principal or Authentication in userservice.getAuthentiatedUser() is null
        // that is, can't juste give username\password
        Principal principal = new MockPrincipal("loginuser");

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, "loginuser", grantedAuths);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return session;
    }
    User loggedInUser;
    private void addTestData() {
        loggedInUser = userService.createUser("loginuser", "loginuser");

        createFriends(loggedInUser);
        createFriendshipRequests(loggedInUser);
    }

    private void createFriends(User loggedInUser) {
        User friend = userService.createUser("friend1", "friend1");
        Friendship f = friendshipService.createNewFriendshipRequest(loggedInUser, friend);
        friendshipService.acceptRequest(f.getId());

        friend = userService.createUser("friend2", "friend2");
        f = friendshipService.createNewFriendshipRequest(friend, loggedInUser);
        friendshipService.acceptRequest(f.getId());
    }

    private void createFriendshipRequests(User loggedInUser) {
        User friend;
        friend = userService.createUser("friendRequest1", "friendRequest1");
        friendshipService.createNewFriendshipRequest(friend, loggedInUser);

        friend = userService.createUser("friendRequest2", "friendRequest2");
        friendshipService.createNewFriendshipRequest(friend, loggedInUser);
    }

    @Test
    public void statusIsOkWhenFetchingFriendsPage() throws Exception {
        MockHttpSession session = buildSession();
        mockMvc.perform(get(URI)
                    .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("friends"));
    }

    @Test
    public void friendPageModelContainsFriendAttributeAndCorrectFriends() throws Exception {
        MockHttpSession session = buildSession();

        MvcResult result = mockMvc
                .perform(get(URI)
                   .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("friends"))
                .andReturn();


        List<User> friends = (List)result.getModelAndView().getModel().get("friends");


        assertEquals("Friend1 not present", 1,
                friends.stream().filter(
                        f -> f.getUsername().equals("friend1")
                ).collect(Collectors.toList()).size()
        );

        assertEquals("Friend2 not present", 1,
                friends.stream().filter(
                        f -> f.getUsername().equals("friend2")
                ).collect(Collectors.toList()).size()
        );

        assertEquals(2, friends.size());
    }

    @Test
    public void friendPageModelContainsFriendRequestAttributeAndCorrectFriendRequests() throws Exception {
        MockHttpSession session = buildSession();
        MvcResult result = mockMvc.perform(get(URI)
                    .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("friendRequests"))
                .andReturn();

        List<Friendship> friendshipRequests = (List)result.getModelAndView().getModel().get("friendRequests");


        assertEquals(2, friendshipService.getFriendshipRequests(loggedInUser).size());
        assertEquals(2, friendshipRequests.size());

        assertEquals("friendRequest1 not present", 1,
                friendshipRequests.stream().filter(
                        f -> f.getSourceUser().getUsername().equals("friendRequest1"))
                .collect(Collectors.toList()).size()
        );

        assertEquals("friendRequest2 not present", 1,
                friendshipRequests.stream().filter(
                        f -> f.getSourceUser().getUsername().equals("friendRequest2"))
                        .collect(Collectors.toList()).size()
        );

    }
}
