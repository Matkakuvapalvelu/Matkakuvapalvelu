package wadp.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

@Controller
@RequestMapping("/trips")
public class TripController {
    
    @Autowired
    TripService tripService;
        
    @Autowired
    CommentService commentService;
        
    @RequestMapping(method = RequestMethod.GET)
    public String view(Model model){
        model.addAttribute("trips", tripService.getAuthenticatedUserTrips());
        model.addAttribute("visibilities", new ArrayList<>(Arrays.asList(Trip.Visibility.values())));
        
        return "trips";
    }
    
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    @Transactional // postgresql REALLY does not like it if you fetch large fields (like images) outside transactions
    public String viewSingleTrip(Model model, @PathVariable("id") Long id){
        List<Post> posts = new ArrayList<>();        
        List<double[]> coordinates = new ArrayList<>();
        
        tripService.getTrip(id).getPosts()
                .stream().filter(x -> x.getImage().getLocation())
                .sorted((p1,  p2) -> p1.getPostDate()
                .compareTo(p2.getPostDate()))
                .forEach(p -> posts.add(p));
        
        posts.stream().forEach(p -> coordinates.add(new double[]{p.getImage().getLatitude(), p.getImage().getLongitude()}));
                
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
        tripService.createTrip(description, Trip.Visibility.valueOf(visibility));
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
