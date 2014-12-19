package wadp.service;

import org.apache.commons.io.IOUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import wadp.Application;
import wadp.domain.FileObject;
import wadp.domain.User;
import wadp.repository.FileObjectRepository;

import java.io.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProfilePicServiceTest {

    private byte[] data;
    private User user;

    @Autowired
    private ProfilePicService profilePicService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileObjectRepository fileObjectRepository;

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
        is.close();

        user = userService.createUser("user", "pword");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void addedProfilePicExists() {
        FileObject profPic = profilePicService.createProfilePic("image/", "testimg", data, user);
        Assert.assertTrue(fileObjectRepository.exists(profPic.getId()));
    }

    @Test
    @Transactional
    public void userHasAddedProfilePic() {
        FileObject profPic = profilePicService.createProfilePic("image/", "testimg", data, user);
        Assert.assertEquals(profPic.getId(), userService.getUser(user.getId()).getProfilePicId());
    }

    @Test
    @Transactional
    public void switchingProfilePic() {
        FileObject profPic = profilePicService.createProfilePic("image/", "testimg", data, user);
        Assert.assertEquals(profPic.getId(), userService.getUser(user.getId()).getProfilePicId());

        FileObject newPic = profilePicService.createProfilePic("image/", "newpicture", data, user);
        Assert.assertEquals(newPic.getId(), userService.getUser(user.getId()).getProfilePicId());
    }

    @Test
    @Transactional
    public void removeProfilePic() {
        FileObject profPic = profilePicService.createProfilePic("image/", "testimg", data, user);
        profilePicService.removeCurrentProfilePic(user);
        Assert.assertNull(userService.getUser(user.getId()).getProfilePicId());
    }

    @Test
    @Transactional
    public void addedProfilePicMatches() {
        FileObject profPic = profilePicService.createProfilePic("image/", "testimg", data, user);
        Assert.assertEquals(profilePicService.getProfilePic(user), profPic);
    }

    @Test(expected = ImageValidationException.class)
    @Transactional
    public void invalidImageThrowsException() {
        FileObject profPic = profilePicService.createProfilePic("foomage/", "testimg", data, user);
    }

}
