package wadp.controller;

import java.util.ArrayList;
import java.util.Arrays;
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
        model.addAttribute("trips", tripService.getUserTripsInSortedOrder(userService.getAuthenticatedUser()));
        model.addAttribute("show_edit", true);
        model.addAttribute("visibilities", new ArrayList<>(Arrays.asList(Trip.Visibility.values())));
        
        return "trips";
    }

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    @Transactional // postgresql REALLY does not like it if you fetch large fields (like images) outside transactions
    public String viewSingleTrip(Model model, @PathVariable("id") Long id){

        if (!tripService.hasRightToSeeTrip(id, userService.getAuthenticatedUser())) {
            return "trips";
        }

        List<double[]> coordinates = tripService.getTripImageCoordinates(id);

        if(coordinates.size() > 0){
            model.addAttribute("startPoint", coordinates.get(0));            
        } else {
            model.addAttribute("startPoint", new double[]{0.00, 0.00});
        }
        
        Trip trip = tripService.getTrip(id);
        model.addAttribute("trip", trip);
        model.addAttribute("coordinates", coordinates);
        model.addAttribute("isTripMap", true);
        model.addAttribute("comments", trip.getComments());
        
        return "trip";
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String createTrip(@RequestParam("header") String header, @RequestParam("description") String description, @RequestParam("visibility") String visibility){
        tripService.createTrip(header, description, Trip.Visibility.valueOf(visibility), userService.getAuthenticatedUser());
        return "redirect:/trips/";
    }

    @RequestMapping(value="/{id}/edit", method = RequestMethod.GET)
    public String showEditTripView(@PathVariable("id") Long id, Model model) {
        model.addAttribute("trip", tripService.getTrip(id));
        model.addAttribute("visibilities", new ArrayList<>(Arrays.asList(Trip.Visibility.values())));
        return "tripedit";
    }
    
    @RequestMapping(value="/{id}/edit", method = RequestMethod.POST)
    public String editTrip(@RequestParam(required = true, value="header") String header, 
            @RequestParam(required = false, value="description") String description, 
            @RequestParam(required = false, value="visibility") String visibility, @PathVariable("id") Long id){
        
        if(header != null && !header.isEmpty()){
            tripService.updateTripChanges(id, header, description, Trip.Visibility.valueOf(visibility), userService.getAuthenticatedUser());
        }

        return "redirect:/trips/";
    }
    
    @RequestMapping(value="/{id}/delete", method = RequestMethod.POST)
    public String deleteTrip(@PathVariable("id") Long id){
        tripService.deleteTrip(id, userService.getAuthenticatedUser());
        return "redirect:/trips/";
    }
    
    @RequestMapping(value = "/{id}/comment", method = RequestMethod.POST)
    public String addCommentToTrip(@ModelAttribute Comment comment, @PathVariable Long id) {
        commentService.addCommentToTrip(comment, tripService.getTrip(id), userService.getAuthenticatedUser());
        return "redirect:/trips/" + id;
    }
}
