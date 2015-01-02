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
import wadp.domain.*;
import wadp.service.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    @Autowired
    private TripService tripService;
    
    @Autowired
    private CommentService commentService;

    @RequestMapping(method = RequestMethod.GET)
    public String showPosts(Model model) {
        model.addAttribute("posts", postService.getUserPosts(userService.getAuthenticatedUser()));
        model.addAttribute("trips", tripService.getUserTrips(userService.getAuthenticatedUser()));
        return "posts";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String createNewPost(
            @RequestParam("file") MultipartFile file,
            @RequestParam("image_text") String text,
            @RequestParam(value ="trip") Long tripId,
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
            model.addAttribute("error", "An internal error has occurred while processing the image");
            return "/posts";
        }

        User user = userService.getAuthenticatedUser();
        Post post = null;

        try {
            Trip t = getTrip(tripId, user);
            post = postService.createPost(image, text, t);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "/posts";
        }


        return "redirect:/posts/" + post.getId();
    }
    
    @RequestMapping(value = "/{id}/comment", method = RequestMethod.POST)
    public String addCommentToPost(
            @ModelAttribute Comment comment,
            @PathVariable Long id) {        
        Post post = postService.getPost(id);
        commentService.addCommentToPost(comment, post, userService.getAuthenticatedUser());
        return "redirect:/posts/" + id;
    }
    
    @RequestMapping(value = "/{pId}/comment/{cId}/delete", method = RequestMethod.POST)
    public String deleteCommentFromPost(@PathVariable("pId") Long pId, @PathVariable("cId") Long cId) {
        commentService.deleteCommentFromPost(postService.getPost(pId), userService.getAuthenticatedUser(), commentService.getComment(cId));
        return "redirect:/posts/" + pId;
    }


    private Trip getTrip(Long tripId, User postCreator) {
        if (tripId == null) {
            throw new IllegalArgumentException("Post must be associated with trip");
        }

        Trip trip = tripService.getTrip(tripId);
        if (trip != null) {
            if (!trip.getCreator().equals(postCreator)) {
                throw new IllegalArgumentException("Post must be added by the trip creator");
            }
        } else {
            throw new IllegalArgumentException("Must be associated with existing trip");
        }

        return trip;
    }

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public String showSinglePost(@PathVariable Long id, Model model) {
        Post post = postService.getPost(id);        
        model.addAttribute("post", post);
        model.addAttribute("comments", post.getComments());
        
        if(post.getImage().getLocation()){
            List<double[]> coordinates = new ArrayList<>();
            coordinates.add(new double[]{post.getImage().getLatitude(), post.getImage().getLongitude(), post.getId()});
            model.addAttribute("startPoint", coordinates.get(0));            
            model.addAttribute("coordinates", coordinates);
            model.addAttribute("isTripMap", false);
        }
        
        return "post";
    }

}
