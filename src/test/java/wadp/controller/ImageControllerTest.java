package wadp.controller;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import wadp.Application;
import wadp.controller.utility.MockMvcTesting;
import wadp.domain.FileObject;
import wadp.domain.Image;
import wadp.service.ImageService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ImageControllerTest {

    private final String URI = "/user_images";

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private FilterChainProxy springSecurityFilter;

    @Autowired
    private ImageService imageService;

    private MockMvcTesting mockMvcTesting;

    private Image img;

    private byte [] data;
    @Before
    public void setUp() throws IOException {
        mockMvcTesting = new MockMvcTesting(webAppContext, springSecurityFilter);
        File imageFile = new File("src/test/testimg.jpg");
        InputStream is = new FileInputStream(imageFile.getAbsoluteFile());

        this.data = IOUtils.toByteArray(is);
        is.close();

        img = imageService.addImage("image/jpg", "img1", data);
    }


    @Test
    public void requestForFullSizeImageContainsImageData() throws Exception {
        MvcResult res = mockMvcTesting.makeGetResponseBody(URI + "/" + img.getOriginalId(), new HashMap<>());

        FileObject imageData = imageService.getImageData(img.getOriginalId());
        assertTrue(Arrays.equals(imageData.getContent(), res.getResponse().getContentAsByteArray()));

    }

    @Test
    public void requestForGalleryThumbnailImageContainsImageData() throws Exception {
        MvcResult res = mockMvcTesting.makeGetResponseBody(URI + "/" + img.getGalleryThumbnailId(), new HashMap<>());
        FileObject imageData = imageService.getImageData(img.getGalleryThumbnailId());
        assertTrue(Arrays.equals(imageData.getContent(), res.getResponse().getContentAsByteArray()));
    }

    @Test
    public void requestForPostThumbnailImageContainsImageData() throws Exception {
        MvcResult res = mockMvcTesting.makeGetResponseBody(URI + "/" + img.getPostThumbnailId(), new HashMap<>());
        FileObject imageData = imageService.getImageData(img.getPostThumbnailId());
        assertTrue(Arrays.equals(imageData.getContent(), res.getResponse().getContentAsByteArray()));
    }

    @Test
    public void requestForNonExistentImageReturnsEmptyImage() throws Exception {
        MvcResult res = mockMvcTesting.makeGetResponseBody(URI + "/" + 12345, new HashMap<>());
        assertEquals(0, res.getResponse().getContentAsByteArray().length);
    }


    @Test
    public void repeatedRequestReturnsNotModified() throws Exception {
        MvcResult res = mockMvcTesting.makeGetResponseBody(URI + "/"  + img.getOriginalId(), new HashMap<>());

        Map<String, String> values = new HashMap<>();
        values.put("If-None-Match", res.getResponse().getHeader("ETag"));

        res = mockMvcTesting.makeGetResponseBody(URI + "/"  + img.getOriginalId(), values, status().is3xxRedirection());
        assertEquals(0, res.getResponse().getContentAsByteArray().length);
    }


}
