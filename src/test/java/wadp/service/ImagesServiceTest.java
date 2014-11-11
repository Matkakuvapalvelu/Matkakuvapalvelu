package wadp.service;

import com.drew.metadata.Metadata;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import wadp.Application;
import wadp.domain.FileObject;
import wadp.domain.Image;
import wadp.repository.ImageRepository;

/**
 *
 * @author Mikael Wide
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ImagesServiceTest {

    @Autowired
    private ImageService imageService;
    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private MetadataService metadataService;

    public ImagesServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void addImage() throws IOException {
        boolean thrown = false;
        Image img = new Image();
        img.setOriginal(new FileObject());
        try {
            imageService.addImage(img, "image/", "joku", new byte[1]);
        } catch (ImageValidationException e) {
            thrown = true;
        }
        assertFalse(thrown);
        assertEquals(imageService.getImage(img.getId()), img);
    }

    @Test(expected = ImageValidationException.class)
    public void wrongFormatThrowsException() throws IOException {
        Image img = new Image();
        img.setOriginal(new FileObject());
        imageService.addImage(img, "file/exe", "joku", new byte[1]);
    }

    @Test
    public void multipleImages() throws IOException {
        Image img = new Image();
        img.setOriginal(new FileObject());
        imageService.addImage(img, "image/", "img1", new byte[1]);

        Image img2 = new Image();
        img.setOriginal(new FileObject());
        imageService.addImage(img2, "image/", "img2", new byte[1]);

        Image img3 = new Image();
        img.setOriginal(new FileObject());
        imageService.addImage(img3, "image/", "img3", new byte[1]);

        assertEquals(imageService.findAllImages().get(0), img);
        assertEquals(imageService.findAllImages().get(1), img2);
        assertEquals(imageService.findAllImages().get(2), img3);
    }

    @Test
    public void imageWithoutLocation() throws IOException {
        Image img = new Image();
        img.setOriginal(new FileObject());
        imageService.addImage(img, "image/", "img1", new byte[1]);

        imageService.setLocation(img);
        assertFalse(img.getLocation());
    }

    @Test
    public void imageWithLocationIsExtracted() throws IOException {
        File imageFile = new File("src/test/testimg.jpg");
        InputStream is = new FileInputStream(imageFile.getAbsoluteFile());
        byte[] data = IOUtils.toByteArray(is);
        
        Image img = new Image();
        imageService.addImage(img, "image/", "foo", data);
        
        assertTrue(imageService.getImage(img.getId()).getLocation());
    }
}
