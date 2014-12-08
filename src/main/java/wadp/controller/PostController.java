package wadp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import wadp.domain.Image;
import wadp.domain.Trip;
import wadp.service.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.ModelAttribute;
import wadp.domain.Comment;
import wadp.domain.Post;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    PostService postService;

    @Autowired
    ImageService imageService;

    @Autowired
    UserService userService;

    @Autowired
    TripService tripService;
    
    @Autowired
    CommentService commentService;

    /*
    * For now, shows list of posts made by user (this functionality should be considered as placeholder; feel free to
    * change if needed) and shows button where new post can be created
    * */
    @RequestMapping(method = RequestMethod.GET)
    public String showPosts(Model model) {
        model.addAttribute("posts", postService.getUserPosts(userService.getAuthenticatedUser()));
        return "posts";
    }

    @RequestMapping(value="/new", method = RequestMethod.GET)
    public String showNewPostCreation(Model model) {
        model.addAttribute("trips", tripService.getUserTrips(userService.getAuthenticatedUser()));
        return "newpost";
    }


    @RequestMapping(method = RequestMethod.POST)
    public String createNewPost(
            @RequestParam("file") MultipartFile file,
            @RequestParam("image_text") String text,
            @RequestParam(value ="trips", required = false) String [] tripIds,
            Model model){

        if (file.isEmpty()) {
            model.addAttribute("error", "Image cannot be empty");
            return "/posts";
        }

        Image image;
        try {
            image  = imageService.addImage(file.getContentType(), file.getOriginalFilename(),
                    file.getBytes());
        } catch (ImageValidationException ex) {
            model.addAttribute("error", "Unknown image type");
            return "/posts";
        } catch (IOException ioExceptiom) {
            model.addAttribute("error", "An internal error has occured while processing the image");
            return "/posts";
        }

        List<Trip> trips = new ArrayList<>();

        addTripsToList(tripIds, trips);

        Post post = postService.createPost(image, text, trips, userService.getAuthenticatedUser());

        return "redirect:/posts/" + post.getId();
    }
    
    @RequestMapping(value = "/{id}/comment", method = RequestMethod.POST)
    public String addCommentToPost(
            @ModelAttribute Comment comment,
            @PathVariable Long id) {        
        Post post = postService.getPost(id);
        commentService.addCommentToPost(comment, post);
        return "redirect:/posts/" + id;
    }

    private void addTripsToList(String[] tripIds, List<Trip> trips) {
        if (tripIds != null) {
            for (String id : tripIds) {
                try {
                    Trip trip = tripService.getTrip(Long.parseLong(id));
                    if (trip != null) {
                        trips.add(trip);
                    }
                } catch (NumberFormatException ex) {
                    // TODO: Add logging code.
                    // This shouldn't happen unless someone bypasses the ui and posts malformed requests
                }
            }
        }
    }

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public String showSinglePost(@PathVariable Long id, Model model) {
        Post post = postService.getPost(id);        
        model.addAttribute("post", post);
        model.addAttribute("comments", post.getComments());
        return "post";
    }

}
