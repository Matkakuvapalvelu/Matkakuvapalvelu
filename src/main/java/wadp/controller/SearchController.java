package wadp.controller;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wadp.domain.Trip;
import wadp.service.TripService;
import wadp.service.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private UserService userService;

    @Autowired
    private TripService tripService;

    @RequestMapping(method = RequestMethod.GET)
    public String showSearchView() {
        return "search";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String searchByKeywords(RedirectAttributes redirectAttributes, String keywords) {
        if (keywords.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("trips", new ArrayList<Trip>());
            return "redirect:search";
        }

        List<Trip> trips = tripService.searchTripsWithKeywords(Arrays.asList(keywords.split(" ")), userService.getAuthenticatedUser());

        // post is loaded lazily, but due to redirection, session is closed and we can no longer load the lazy objects
        // after redirection. Instead, we force the loading of the objects here
        for (Trip t : trips) {
            Hibernate.initialize(t.getPosts());
        }
        redirectAttributes.addFlashAttribute("trips", trips);
        return "redirect:search";
    }

}
