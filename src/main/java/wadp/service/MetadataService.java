package wadp.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

@Service
public class MetadataService {

    public Metadata extractMetadata(byte[] content) {
        BufferedInputStream bIs = new BufferedInputStream(new ByteArrayInputStream(content));
        Metadata imageMetadata = new Metadata();
        try {
            imageMetadata = ImageMetadataReader.readMetadata(bIs, true);
        } catch (ImageProcessingException ex) {
            Logger.getLogger(MetadataService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MetadataService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return imageMetadata;
    }

}
