package wadp.controller;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import wadp.domain.Image;
import wadp.repository.ImageRepository;
import wadp.service.ImageService;

@Controller
@RequestMapping("/images")
public class ImageController {

    @Autowired
    ImageService imageService;

    @RequestMapping(method = RequestMethod.GET)
    public String getImages(Model model) {
        model.addAttribute("images", imageService.getAllImages());
        return "image";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String addImage(@RequestParam("file") MultipartFile file) throws IOException {
        Image image = new Image();

        imageService.addImage(image, file.getContentType(), file.getOriginalFilename(),
                file.getBytes());

        return "redirect:/images";
    }
}
