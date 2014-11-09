package wadp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.service.TripService;

@Controller
@RequestMapping("/trip")
public class TripController {
    
    @Autowired
    TripService tripService;
    
    @RequestMapping(method = RequestMethod.GET)
    public String getTrips(Model model){
        model.addAttribute("trips", tripService.getTripsByOwner());
        return "trip";
    }
}
