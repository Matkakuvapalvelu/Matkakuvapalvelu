package wadp.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import wadp.domain.Trip;
import wadp.domain.User;
import wadp.service.*;
import java.io.IOException;
import java.util.List;


@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TripService tripService;

    @Autowired
    private PostService postService;

    @Autowired
    private ProfilePicService profilePicService;

    @Autowired
    private CommentService commentService;
    
    @RequestMapping(method = RequestMethod.GET)
    public String showProfilePage(Model model) {
        addUserDetails(userService.getAuthenticatedUser(), model, true);
        return "profile";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String showProfilePage(@PathVariable Long id, Model model) {
        addUserDetails(userService.getUser(id), model, userService.getAuthenticatedUser().getId() == id);
        return "profile";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String createProfilePicture(@RequestParam("file") MultipartFile file, Model model) {
        try {
            profilePicService.createProfilePic(file.getContentType(), file.getOriginalFilename(), file.getBytes(), userService.getAuthenticatedUser());

        } catch (ImageValidationException ex) {
            model.addAttribute("error", "Unknown image type");
            return "redirect:/profile/";
        } catch (IOException ioExceptiom) {
            model.addAttribute("error", "An internal error has occurred while processing the image");
            return "redirect:/profile/";
        }
        return "redirect:/profile/";
    }


    private void addUserDetails(User user, Model model, boolean isLoggedInUser) {
        model.addAttribute("user", user);

        if (isLoggedInUser) {
            model.addAttribute("unreadnotifications",
                    notificationService.getUnreadNotificationCountForUser(userService.getAuthenticatedUser()));
        } else {
            model.addAttribute("canrequestfriendship",
                    !friendshipService.friendshipEntityExists(user, userService.getAuthenticatedUser()));
        }
        
        List<Trip> trips = tripService.getNewestTrips(user, userService.getAuthenticatedUser(), 4);
        model.addAttribute("trips", trips);
        model.addAttribute("show_edit", false);

        List<double[]> coordinates = tripService.getStartpointCoordinatesOfTrips(user, userService.getAuthenticatedUser());

        if (coordinates.size() > 0) {
            model.addAttribute("startPoint", coordinates.get(0));
        } else {
            model.addAttribute("startPoint", new double[]{0.00, 0.00});
        }
        model.addAttribute("coordinates", coordinates);
        model.addAttribute("isTripMap", false);                
        
        model.addAttribute("signupDate", user.getSignupDate());        
        model.addAttribute("tripCount", user.getTrips().size());
        model.addAttribute("commentCount", user.getComments().size());
        model.addAttribute("postCount", postService.getUserPosts(user).size());
        
    }
}
