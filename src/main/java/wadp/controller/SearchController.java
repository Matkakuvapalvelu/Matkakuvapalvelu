package wadp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.service.TripService;
import wadp.service.UserService;

import java.util.Arrays;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private UserService userService;

    @Autowired
    private TripService tripService;

    @RequestMapping(method = RequestMethod.POST)
    public String searchByKeywords(String keyWords) {

        String [] keyWordList = keyWords.split(" ");
        tripService.searchTripsWithKeywords(Arrays.asList(keyWordList), userService.getAuthenticatedUser());



        return "redirect:searchresults";
    }

}
