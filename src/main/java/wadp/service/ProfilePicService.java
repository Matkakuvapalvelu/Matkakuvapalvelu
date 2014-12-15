package wadp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wadp.domain.FileObject;
import wadp.domain.User;
import wadp.repository.FileObjectRepository;
import wadp.repository.UserRepository;

/**
 * Service for handling profilepics for users
 */


@Service
public class ProfilePicService {

    @Autowired
    private ThumbnailService thumbnailService;

    @Autowired
    private FileObjectRepository fileObjectRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Transactional  // @Transactional has to be used when directly handling FileObjects
    public FileObject createProfilePic(String mediatype, String name, byte[] content, User user) {
        if (!mediatype.startsWith("/image")) {
            throw new ImageValidationException("Invalid image format!");
        }

        FileObject profilePic = new FileObject();
        profilePic.setName(name);
        profilePic.setContentType(mediatype);
        profilePic.setContentLength(new Long(content.length));

        profilePic.setContent(thumbnailService.createProfileThumb(content));
        profilePic = fileObjectRepository.save(profilePic);

        userService.getUser(user.getId()).setProfilePicId(profilePic.getId());
        userRepository.save(user);
        return profilePic;
    }

    /**
     * Returns the fileobject referenced in User.ProfilePicId
     */
    @Transactional
    public FileObject getProfilePic(User user) {
        return fileObjectRepository.findOne(user.getProfilePicId());
    }

    /**
     * Only removes the reference to a users profilepic
     */
    public void removeCurrentProfilePic(User user) {
        userService.getUser(user.getId()).setProfilePicId(null);
        userRepository.save(user);
    }
}
