package wadp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

// Probably should be removed later on. Right now just redirects any unhandled addresses to index
@Controller
@RequestMapping("/index")
public class DefaultController {


    @RequestMapping(method = RequestMethod.GET)
    public String showIndex() {
        return "index";
    }
}
