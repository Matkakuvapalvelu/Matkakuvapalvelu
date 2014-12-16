package wadp.controller;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
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

// Probably should be refactored. All is just written here in a hurry
@Controller
@RequestMapping("*")
public class DefaultController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TripService tripService;
    
    @Autowired
    private PostService postService;

    @RequestMapping(method = RequestMethod.GET)
    public String showIndex(Model model) {
        
        Map<User, Integer> userMap = new HashMap();        
        userService.getUsers().stream()                
                .forEach(user -> {
                    if(userMap.size() < 5){
                        int commentCount = user.getComments().size();                    
                        user.getTrips().forEach(trip -> {
                            int value = 0;
                            if(userMap.containsKey(user)){
                                value = userMap.get(user) + trip.getPosts().size();
                            } else{
                                value = trip.getPosts().size() + commentCount;
                            }             
                            userMap.put(user, value);                        
                        });                    
                    }                
                });
                     
        model.addAttribute("activeUsers", entriesSortedByValues(userMap));

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
        
        tripService.getNewestPublicTrips(3).forEach(trip -> {
            tripMap.put(trip, postService.getNewestPosts(trip, 3));
        });
        if(tripMap.size() > 0){
            model.addAttribute("tripsInMap", tripMap);
        }        
                
        return "index";
    }
    
    static <K,V extends Comparable<? super V>>
    SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<>(
            new Comparator<Map.Entry<K,V>>() {
                @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1;
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
}
