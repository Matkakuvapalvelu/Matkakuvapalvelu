package wadp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

// Probably should be removed later on. Right now just redirects any unhandled addresses to index
@Controller
@RequestMapping("*")
public class DefaultController {
    public String showIndex() {
        return "index";
    }
}
