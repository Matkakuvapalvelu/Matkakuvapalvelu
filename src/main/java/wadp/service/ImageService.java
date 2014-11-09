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
    MetadataService metadataService;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    FileObjectRepository fileObjectRepository;

    public Image addImage(Image image, String mediatype, String name, byte[] content) throws IOException {
        if (!validateFormat(mediatype)) {
            throw new ImageValidationException("Could not validate image format");
        }

        FileObject original = new FileObject();
        original.setName(name);
        original.setContentType(mediatype);
        original.setContentLength(new Long(content.length));
        original.setContent(content);

        fileObjectRepository.save(original);

        image.setOriginal(original);
        setLocation(image);
        return imageRepository.save(image);
    }

    public Image setLocation(Image image) {
        Metadata metadata = new Metadata();

        metadata = metadataService.extractMetadata(image.getOriginal().getContent());
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

    public List getAllImages() {
        return imageRepository.findAll();
    }

    public boolean validateFormat(String mediaType) {
        return mediaType.startsWith("image/");
    }

    public Image getImage(Long id) {
        return imageRepository.findOne(id);
    }
}
