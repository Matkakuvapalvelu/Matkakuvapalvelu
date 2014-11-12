package wadp.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import wadp.domain.FileObject;

/**
 *
 * @ThumbnailerService is for creating the thumbnail for both post and gallery
 * PostThumbnail = 640x360 px, GalleryThumbnail = 180x180 px
 *
 */
@Service
public class ThumbnailService {

    public FileObject createGalleryThumb(byte[] content, String filename) {
        return createThumbnail(content, filename, 180, 180);
    }

    public FileObject createPostThumb(byte[] content, String filename) {
        return createThumbnail(content, filename, 640, 640);
    }

    private FileObject createThumbnail(byte[] content, String filename, int width, int height) {
        BufferedImage thumbnail;
        try {
            thumbnail = Scalr.resize(ImageIO.read(new ByteArrayInputStream(content)),
                    Scalr.Method.QUALITY,
                    Scalr.Mode.FIT_TO_WIDTH,
                    width, height, Scalr.OP_ANTIALIAS);
        } catch (IOException ex) {
            Logger.getLogger(ThumbnailService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(thumbnail, "png", baos);
        } catch (IOException ex) {
            Logger.getLogger(ThumbnailService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        byte[] bytes = baos.toByteArray();

        FileObject fo = new FileObject();
        fo.setContent(bytes);
        fo.setContentLength(new Long(bytes.length));
        fo.setContentType("image/png");
        fo.setName(filename + "-thumb.png");

        return fo;
    }

}
