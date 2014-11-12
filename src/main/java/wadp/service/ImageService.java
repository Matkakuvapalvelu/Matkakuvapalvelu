package wadp.service;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.domain.FileObject;
import wadp.domain.Image;
import wadp.repository.FileObjectRepository;
import wadp.repository.ImageRepository;

@Service
public class ImageService {

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private FileObjectRepository fileObjectRepository;

    @Autowired
    private ThumbnailService thumbnailService;

    public Image addImage(Image image, String mediatype, String name, byte[] content) throws IOException {
        if (!validateFormat(mediatype)) {
            throw new ImageValidationException("Invalid image format!");
        }

        FileObject original = new FileObject();
        original.setName(name);
        original.setContentType(mediatype);
        original.setContentLength(new Long(content.length));
        original.setContent(content);

        fileObjectRepository.save(original);

        FileObject galleryThumb = fileObjectRepository.save(thumbnailService.createGalleryThumb(content, name));
        FileObject postThumb = fileObjectRepository.save(thumbnailService.createPostThumb(content, name));

        image.setGalleryThumbnail(galleryThumb);
        image.setPostThumbnail(postThumb);
        image.setOriginal(original);
        setLocation(image);
        return imageRepository.save(image);
    }

    public Image setLocation(Image image) {
        if (!validateFormat(image.getOriginal().getContentType())) {
            image.setLocation(false);
            return image;
        }
        Metadata metadata = metadataService.extractMetadata(image.getOriginal().getContent());
        if (metadata.hasErrors()) {
            return image;
        }
        if (metadata.getDirectory(GpsDirectory.class) == null) {
            image.setLocation(false);
            return image;
        }
        image.setLatitude(metadata.getDirectory(GpsDirectory.class).getGeoLocation().getLatitude());
        image.setLongitude(metadata.getDirectory(GpsDirectory.class).getGeoLocation().getLongitude());
        image.setLocation(true);
        return image;
    }

    public List findAllImages() {
        return imageRepository.findAll();
    }

    private boolean validateFormat(String mediaType) {
        return mediaType.startsWith("image/");
    }

    public Image getImage(Long id) {
        return imageRepository.findOne(id);
    }
}
