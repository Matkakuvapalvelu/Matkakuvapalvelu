package wadp.controller;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wadp.domain.Image;
import wadp.service.ImageService;


@Controller
@RequestMapping("/images")
public class ImageController {

    @Autowired
    ImageService imageService;

    @RequestMapping(method = RequestMethod.GET)
    public String getImages(Model model) {
        model.addAttribute("images", imageService.findAllImages());
        return "image";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String addImage(@RequestParam("file") MultipartFile file) throws IOException {
        Image image = new Image();

        imageService.addImage(image, file.getContentType(), file.getOriginalFilename(),
                file.getBytes());

        return "redirect:/images";
    }


    @RequestMapping(value="/{id}/original", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable Long id){
        // TODO: Check that user actually has right to see the image in question
        // TODO: Send 304 if browser has image cached

        final HttpHeaders headers = new HttpHeaders();

        Image image = imageService.getImage(id);

        headers.setContentType(MediaType.parseMediaType(image.getOriginal().getContentType()));
        headers.setContentLength(image.getOriginal().getContentLength());
        headers.setCacheControl("public");
        headers.setExpires(Long.MAX_VALUE);
        headers.add("ETag", "\"" + image.getId() + "\"");

        return new ResponseEntity<>(image.getOriginal().getContent(), headers, HttpStatus.CREATED);
    }
}
