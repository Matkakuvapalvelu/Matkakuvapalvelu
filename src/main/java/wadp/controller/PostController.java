package wadp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import wadp.domain.Image;
import wadp.domain.Post;
import wadp.service.ImageService;
import wadp.service.PostService;
import wadp.service.UserService;

import java.io.IOException;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    PostService postService;

    @Autowired
    ImageService imageService;

    @Autowired
    UserService userService;

    /*
    * For now, shows list of posts made by user (this functionality should be considered as placeholder; feel free to
    * change if needed) and shows button where new post can be created
    * */
    @RequestMapping(method = RequestMethod.GET)
    public String showPosts() {
        // todo - add user posts to model



        return "posts";
    }

    @RequestMapping(value="/new", method = RequestMethod.GET)
    public String showNewPostCreation() {

        return "newpost";
    }


    @RequestMapping(method = RequestMethod.POST)
    public String createNewPost(@RequestParam("file") MultipartFile file, @RequestParam("image_text") String text) throws IOException {
        // TODO: Validate that image is not empty!

        Image image = new Image();

        // TODO: Catch ImageValidationException and give appropriate error message to user
        image = imageService.addImage(image, file.getContentType(), file.getOriginalFilename(),
                file.getBytes());

        postService.createPost(image, text, userService.getAuthenticatedUser());

        return "redirect:/posts";
    }

}
