package wadp.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import wadp.domain.Comment;
import wadp.domain.Post;
import wadp.domain.Trip;
import wadp.service.CommentService;
import wadp.service.TripService;
import wadp.service.UserService;

@Controller
@RequestMapping("/trips")
public class TripController {
    
    @Autowired
    TripService tripService;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;
        
    @RequestMapping(method = RequestMethod.GET)
    public String view(Model model){
        model.addAttribute("trips", tripService.getUserTrips(userService.getAuthenticatedUser()));
        model.addAttribute("visibilities", new ArrayList<>(Arrays.asList(Trip.Visibility.values())));
        
        return "trips";
    }

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    @Transactional // postgresql REALLY does not like it if you fetch large fields (like images) outside transactions
    public String viewSingleTrip(Model model, @PathVariable("id") Long id){

        if (!tripService.hasRightToSeeTrip(id, userService.getAuthenticatedUser())) {
            return "trips";
        }

        List<Post> posts = new ArrayList<>();        
        List<double[]> coordinates = new ArrayList<>();
        // Dirty hack for now, so that trips don't crash if capturedate is not found
        tripService.getTrip(id).getPosts()
                .stream()
                .filter(x -> x.getImage().getLocation())
                .sorted((p1,  p2) -> { Date p1Date = p1.getImage().getCaptureDate(); Date p2Date = p2.getImage().getCaptureDate(); 
                if( p1Date == null){
                    p1Date = p1.getPostDate();
                }
                if( p2Date == null){
                    p2Date = p2.getPostDate();
                }
                return p1Date.compareTo(p2Date); } )
                .forEach(p -> posts.add(p));
        
        posts.stream().forEach(p -> coordinates.add(new double[]{p.getImage().getLatitude(), p.getImage().getLongitude(), p.getId()}));
                
        if(coordinates.size() > 0){
            model.addAttribute("startPoint", coordinates.get(0));            
        }else {
            model.addAttribute("startPoint", new double[]{0.00, 0.00});
        }
            
        model.addAttribute("trip", tripService.getTrip(id));
        model.addAttribute("coordinates", coordinates);
        
        return "trip";
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String createTrip(@RequestParam("description") String description, @RequestParam("visibility") String visibility){
        tripService.createTrip(description, Trip.Visibility.valueOf(visibility), userService.getAuthenticatedUser());
        return "redirect:/trips/";
    }
    
    @RequestMapping(value="/{id}/edit", method = { RequestMethod.GET, RequestMethod.POST })
    public String editTrip(@RequestParam(required = false, value="description") String description, 
            @RequestParam(required = false, value="visibility") String visibility, @PathVariable("id") Long id, Model model){  
        
        if(description != null && !description.isEmpty()){
            tripService.updateTripChanges(id, description, Trip.Visibility.valueOf(visibility));
            return "redirect:/trips/";
        }
        
        model.addAttribute("trip", tripService.getTrip(id));
        model.addAttribute("visibilities", new ArrayList<>(Arrays.asList(Trip.Visibility.values())));
        
        return "tripedit";
    }
    
    @RequestMapping(value = "/{id}/comment", method = RequestMethod.POST)
    public String addCommentToTrip(@ModelAttribute Comment comment, @PathVariable Long id) {
        commentService.addCommentToTrip(comment, tripService.getTrip(id));
        return "redirect:/trips/" + id;
    }
}
