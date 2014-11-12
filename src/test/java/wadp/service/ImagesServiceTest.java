package wadp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    private byte[] data;
    private byte[] data2;

    public ImagesServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws FileNotFoundException, IOException {
        File imageFile = new File("src/test/testimg.jpg");
        InputStream is = new FileInputStream(imageFile.getAbsoluteFile());
        this.data = IOUtils.toByteArray(is);

        File imageFile2 = new File("src/test/testimg2.jpg");
        InputStream is2 = new FileInputStream(imageFile2.getAbsoluteFile());
        this.data2 = IOUtils.toByteArray(is2);

    }

    @After
    public void tearDown() {
    }

    @Test
    public void addImage() throws IOException {
        Image img = new Image();
        imageService.addImage(img, "image/", "foo", this.data);

        assertEquals(imageService.getImage(img.getId()), img);
    }

    @Test(expected = ImageValidationException.class)
    public void wrongFormatThrowsException() throws IOException {
        Image img = new Image();
        imageService.addImage(img, "file/exe", "joku", new byte[1]);
    }

    @Test
    public void multipleImages() throws IOException {
        Image img = new Image();
        imageService.addImage(img, "image/", "img1", this.data);

        Image img2 = new Image();
        imageService.addImage(img2, "image/", "img2", this.data);

        Image img3 = new Image();
        imageService.addImage(img3, "image/", "img3", this.data);

        assertEquals(imageService.findAllImages().get(0), img);
        assertEquals(imageService.findAllImages().get(1), img2);
        assertEquals(imageService.findAllImages().get(2), img3);
    }

    @Test
    public void imageWithoutLocation() throws IOException {
        Image img = new Image();
        imageService.addImage(img, "image/", "img1", this.data2);

        assertNotNull(imageService.getImage(img.getId()));
        assertFalse(imageService.getImage(img.getId()).getLocation());
    }

    @Test
    public void imageWithLocationIsExtracted() throws IOException {
        Image img = new Image();
        imageService.addImage(img, "image/", "foo", this.data);

        assertTrue(imageService.getImage(img.getId()).getLocation());
    }
}
