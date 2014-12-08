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
import wadp.domain.FileObject;
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
        if (!file.isEmpty()) {
            

        imageService.addImage(file.getContentType(), file.getOriginalFilename(),
                file.getBytes());
        }

        return "redirect:/images";
    }


    @RequestMapping(value="/{id}/original", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getOriginalImage(@PathVariable Long id){
        // TODO: Check that user actually has right to see the image in question
        // TODO: Send 304 if browser has image cached


        Image image = imageService.getImage(id);
        return new ResponseEntity<>(image.getOriginal().getContent(), getImageHeaders(image.getOriginal()), HttpStatus.CREATED);
    }

    @RequestMapping(value="/{id}/postthumbnail", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getThumbnailImage(@PathVariable Long id){
        // TODO: Check that user actually has right to see the image in question
        // TODO: Send 304 if browser has image cached

        Image image = imageService.getImage(id);
        return new ResponseEntity<>(image.getPostThumbnail().getContent(), getImageHeaders(image.getPostThumbnail()), HttpStatus.CREATED);
    }

    @RequestMapping(value="/{id}/gallerythumbnail", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getGalleryThumbnailImage(@PathVariable Long id){
        // TODO: Check that user actually has right to see the image in question
        // TODO: Send 304 if browser has image cached

        Image image = imageService.getImage(id);
        return new ResponseEntity<>(image.getGalleryThumbnail().getContent(), getImageHeaders(image.getGalleryThumbnail()), HttpStatus.CREATED);
    }

    private HttpHeaders getImageHeaders(FileObject image) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(image.getContentType()));
        headers.setContentLength(image.getContentLength());
        headers.setCacheControl("public");
        headers.setExpires(Long.MAX_VALUE);
        headers.add("ETag", "\"" + image.getId() + "\"");
        return headers;
    }

}
