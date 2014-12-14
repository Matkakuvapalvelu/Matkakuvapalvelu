package wadp.controller;

import java.io.File;
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
@RequestMapping("/user_images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long id,
            @RequestHeader(value="If-None-Match", defaultValue="") String userAgent){

        FileObject fileObject = imageService.getImageData(id);

        if (userAgent.isEmpty()) {
            if (fileObject == null) {
                fileObject = new FileObject();
                fileObject.setContent(new byte[0]);
                fileObject.setContentLength(0l);
                fileObject.setContentType("image/png");
                fileObject.setName("No image");
            }
            return imageResponseEntity(fileObject);
        } else {
            return notModifiedResponseEntity(id);
        }

    }

    private ResponseEntity<byte[]> imageResponseEntity(FileObject image) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(image.getContentType()));
        headers.setContentLength(image.getContentLength());
        headers.setCacheControl("public");
        headers.setExpires(Long.MAX_VALUE);
        headers.add("ETag", "\"" + image.getId() + "\"");

        return new ResponseEntity<>(image.getContent(), headers, HttpStatus.CREATED);
    }

    private ResponseEntity<byte[]> notModifiedResponseEntity(Long id) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("ETag", "\"" + id + "\"");
        return new ResponseEntity<>(null, headers, HttpStatus.NOT_MODIFIED);
    }

}
