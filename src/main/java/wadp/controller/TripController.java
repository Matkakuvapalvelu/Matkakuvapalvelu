package wadp.controller;

import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import wadp.domain.Trip;
import wadp.repository.TripRepository;
import wadp.service.TripService;

@Controller
@RequestMapping("/trips")
public class TripController {
    
    @Autowired
    TripService tripService;
        
    @Autowired
    TripRepository tripRepository;
        
    @RequestMapping(method = RequestMethod.GET)
    public String view(Model model){
        model.addAttribute("trips", tripService.getAuthenticatedUserTrips());
        model.addAttribute("visibilities", new ArrayList<>(Arrays.asList(Trip.Visibility.values())));
        
        return "trips";
    }
    
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public String viewSingleTrip(Model model, @PathVariable("id") Long id){
        model.addAttribute("trip", tripRepository.findOne(id));
        model.addAttribute("posts", tripRepository.findOne(id).getPosts());
        
        return "trip";
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String createTrip(@RequestParam("description") String description, @RequestParam("visibility") String visibility){
        tripService.createTrip(description, Trip.Visibility.valueOf(visibility));
        return "redirect:/trips/";
    }
    
    @RequestMapping(value="/{id}/edit")
    public String editTrip(@RequestParam(required = false, value="description") String description, 
            @RequestParam(required = false, value="visibility") String visibility, @PathVariable("id") Long id, Model model){  
        
        if(description != null && !description.isEmpty()){
            tripService.updateTripChanges(id, description, Trip.Visibility.valueOf(visibility));
            return "redirect:/trips/";
        }
        
        model.addAttribute("trip", tripRepository.findOne(id));
        model.addAttribute("visibilities", new ArrayList<>(Arrays.asList(Trip.Visibility.values())));
        
        return "tripedit";
    }
}
