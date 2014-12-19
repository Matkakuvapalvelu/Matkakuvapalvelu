package wadp.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.domain.Post;
import wadp.domain.Trip;
import wadp.domain.User;
import wadp.service.PostService;
import wadp.service.TripService;
import wadp.service.UserService;

// Any request not handled by other controllers is redirected to index
@Controller
@RequestMapping("*")
public class IndexController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TripService tripService;
    
    @Autowired
    private PostService postService;

    @RequestMapping(method = RequestMethod.GET)
    public String showIndex(Model model) {

        Map<User, Integer> activeUserPostCounts = new LinkedHashMap<>();

        userService.getMostActiveUsers(5).forEach(user ->
                activeUserPostCounts.put(user, postService.getUserPosts(user).size()));


        model.addAttribute("activeUsers", activeUserPostCounts);

        List<double[]> coordinates = tripService.getStartpointCoordinatesOfTrips(null, null);  
        if (coordinates.size() > 0) {
            model.addAttribute("startPoint", coordinates.get(0));
        } else {
            model.addAttribute("startPoint", new double[]{0.00, 0.00});
        }
        model.addAttribute("coordinates", coordinates);
        model.addAttribute("isTripMap", false);
        model.addAttribute("mapHeight", 800);
                
        Map<Trip, List<Post>> tripMap = new LinkedHashMap(); 
        
        tripService.getNewestPublicTrips(3).forEach(trip ->
                tripMap.put(trip, postService.getNewestPosts(trip, 3)));
        
        if(tripMap.size() > 0){
            model.addAttribute("tripsInMap", tripMap);
        }      
        
        int i = 1;
        for (Map.Entry<Trip, List<Post>> entry : tripMap.entrySet())
        {
            List<double[]> coord = tripService.getTripImageCoordinates(entry.getKey().getId());

            if (coord.size() > 0) {
                model.addAttribute("startPoint" + i, coord.get(0));
            } else {
                model.addAttribute("startPoint" + i, new double[]{0.00, 0.00});
            }            
            model.addAttribute("coordinates" + i, coord);
            i++;
        }
        
        
        
        return "index";
    }
}
