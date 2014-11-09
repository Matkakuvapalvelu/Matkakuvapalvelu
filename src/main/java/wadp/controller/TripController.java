package wadp.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
        model.addAttribute("trips", tripService.getTripsByOwner());
        
        Map<Trip.Visibility, String> visibilities = new HashMap<>();  
        visibilities.put(Trip.Visibility.PRIVATE, "Private");  
        visibilities.put(Trip.Visibility.FRIENDS, "Friends");
        visibilities.put(Trip.Visibility.PUBLIC, "Public");
        model.addAttribute("visibilities", visibilities);
        
        return "trips";
    }
    
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public String viewSingleTrip(Model model, @PathVariable Long id){
        model.addAttribute("trip", tripRepository.findOne(id));
        
        Map<Trip.Visibility, String> visibilities = new HashMap<>();  
        visibilities.put(Trip.Visibility.PRIVATE, "Private");  
        visibilities.put(Trip.Visibility.FRIENDS, "Friends");
        visibilities.put(Trip.Visibility.PUBLIC, "Public");
        model.addAttribute("visibilities", visibilities);
        
        return "trips";
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String createTrip(@ModelAttribute Trip trip){
        tripService.createTrip(trip);
        return "redirect:/trips";
    }
}
