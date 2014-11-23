package wadp.controller;


import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import wadp.Application;
import wadp.domain.*;
import wadp.service.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static wadp.controller.utility.ControllerTestHelpers.buildSession;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TripControllerTest {

    private final String URI = "/trips";

    @Autowired
    private TripService tripService;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private FilterChainProxy springSecurityFilter;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private PostService postService;

    private User loggedInUser;
    private User otherUser;



    private MockMvc mockMvc;

    @Before
    public void setUp() {
        loggedInUser = userService.createUser("loginuser", "loginuser");
        otherUser = userService.createUser("otheruser", "otheruser");

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webAppContext)
                .addFilter(springSecurityFilter, "/*")
                .build();

        webAppContext.getServletContext().setAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppContext);
    }


    @Test
    public void nothingIsAddedToModelIfUserHasNoRightToSeeTrip() throws Exception {
        Trip t = tripService.createTrip("description", Trip.Visibility.PRIVATE, otherUser);

        MockHttpSession session = buildSession();

        mockMvc.perform(get(URI + "/" + t.getId())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("startPoint", "trip", "coordinates"))
                .andExpect(view().name("trips"));

    }

    @Test
    public void attributesAreAddedToModelWhenViewingSingleTripWhenFriends() throws Exception {
        Trip t = tripService.createTrip("description", Trip.Visibility.FRIENDS, otherUser);

        Friendship f = friendshipService.createNewFriendshipRequest(otherUser, loggedInUser);
        friendshipService.acceptRequest(f.getId());
        MockHttpSession session = buildSession();


        mockMvc.perform(get(URI + "/" + t.getId())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("startPoint", "trip", "coordinates"))
                .andExpect(view().name("trip"));
    }

    @Test
    public void attributesAreAddedToModelWhenViewingSingleTripWhenTripIsPublic() throws Exception {
        Trip t = tripService.createTrip("description", Trip.Visibility.PUBLIC, otherUser);

        MockHttpSession session = buildSession();

        mockMvc.perform(get(URI + "/" + t.getId())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("startPoint", "trip", "coordinates"))
                .andExpect(view().name("trip"));
    }

    @Test
    public void tripAttributeHasCorrectValueWhenViewingSingleTrip() throws Exception {
        Trip t = tripService.createTrip("description", Trip.Visibility.PUBLIC, otherUser);

        MockHttpSession session = buildSession();

        MvcResult result = mockMvc
                .perform(get(URI + "/" + t.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("trip"))
                .andReturn();

        Trip modelTrip = (Trip)result.getModelAndView().getModel().get("trip");
        assertEquals(t.getId(), modelTrip.getId());
    }

    @Test
    public void startPointHasDefaultValueWhenTripHasNoPosts() throws Exception {

        Trip t = tripService.createTrip("description", Trip.Visibility.PUBLIC, otherUser);

        MockHttpSession session = buildSession();

        MvcResult result = mockMvc
                .perform(get(URI + "/" + t.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("trip"))
                .andReturn();

        double[] latitudeLongitudeId = (double[])result.getModelAndView().getModel().get("startPoint");

        assertEquals(2, latitudeLongitudeId.length);
        assertEquals(0.0, latitudeLongitudeId[0], 0.0001);
        assertEquals(0.0, latitudeLongitudeId[1], 0.0001);
    }

    @Test
    public void coordinatesListIsEmptyWhenTripHasNoPosts() throws Exception {

        Trip t = tripService.createTrip("description", Trip.Visibility.PUBLIC, otherUser);

        MockHttpSession session = buildSession();

        MvcResult result = mockMvc
                .perform(get(URI + "/" + t.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("trip"))
                .andReturn();

        List<double[]> latitudeLongitudeIds = (List<double[]>)result.getModelAndView().getModel().get("coordinates");
        assertEquals(0, latitudeLongitudeIds.size());
    }

    @Test
    public void startPointAttributeHasCorrectValueWhenViewingSingleTripWithPosts() throws Exception {

        Trip t = tripService.createTrip("description", Trip.Visibility.PUBLIC, otherUser);

        Image firstImage = loadTestImage("src/test/testimg.jpg", t);
        Image secondImage = loadTestImage("src/test/testimg3.jpg", t);

        MockHttpSession session = buildSession();

        MvcResult result = mockMvc
                .perform(get(URI + "/" + t.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("trip"))
                .andReturn();

        double[] latitudeLongitudeId = (double[])result.getModelAndView().getModel().get("startPoint");

        assertEquals(3, latitudeLongitudeId.length);
        assertEquals(secondImage.getLatitude(), latitudeLongitudeId[0], 0.0001);
        assertEquals(secondImage.getLongitude(), latitudeLongitudeId[1], 0.0001);
        assertEquals((double)secondImage.getId(), latitudeLongitudeId[2], 0.0001);
    }

    @Test
    @Transactional
    public void coordinatesHaveCorrectValueWhenViewingSingleTripWithPosts() throws Exception {

        Trip t = tripService.createTrip("description", Trip.Visibility.PUBLIC, otherUser);

        Image firstImage = loadTestImage("src/test/testimg.jpg", t);
        Image secondImage = loadTestImage("src/test/testimg3.jpg", t);

        MockHttpSession session = buildSession();

        MvcResult result = mockMvc
                .perform(get(URI + "/" + t.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("trip"))
                .andReturn();

        List<double[]> latitudeLongitudeIds = (List<double[]>)result.getModelAndView().getModel().get("coordinates");

        assertEquals(2, latitudeLongitudeIds.size());

        assertEquals(secondImage.getLatitude(), latitudeLongitudeIds.get(0)[0], 0.0001);
        assertEquals(secondImage.getLongitude(), latitudeLongitudeIds.get(0)[1], 0.0001);
        assertEquals((double)secondImage.getId(), latitudeLongitudeIds.get(0)[2], 0.0001);

        assertEquals(firstImage.getLatitude(), latitudeLongitudeIds.get(1)[0], 0.0001);
        assertEquals(firstImage.getLongitude(), latitudeLongitudeIds.get(1)[1], 0.0001);
        assertEquals((double)firstImage.getId(), latitudeLongitudeIds.get(1)[2], 0.0001);
    }

    private Image loadTestImage(String name, Trip trip) throws IOException {
        File imageFile = new File(name);
        InputStream is = new FileInputStream(imageFile.getAbsoluteFile());
        byte[] data = IOUtils.toByteArray(is);
        is.close();

        Image image = imageService.addImage(new Image(), "image/", "foo", data);
        Post post = postService.createPost(image, "Hello!", Arrays.asList(trip), otherUser);

        return image;
    }


}
