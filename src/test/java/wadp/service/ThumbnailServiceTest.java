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

/**
 *
 * @author Mikael Wide
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ThumbnailServiceTest {

    private byte[] data;
    private Image img;
    @Autowired
    private ThumbnailService thumbnailService;
    @Autowired
    private ImageService imageService;

    public ThumbnailServiceTest() {
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

        this.img = new Image();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void addedImageCreatesThumbnails() throws IOException {
        imageService.addImage(img, "image/", "foo", data);
        assertNotNull(img.getGalleryThumbnail());
        assertNotNull(img.getPostThumbnail());
    }

}
