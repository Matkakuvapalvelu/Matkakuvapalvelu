package wadp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wadp.domain.FileObject;
import wadp.domain.User;
import wadp.repository.FileObjectRepository;
import wadp.repository.UserRepository;

/**
 *
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

    @Transactional
    public FileObject createProfilePic(String mediatype, String name, byte[] content, User user) {
        if (!mediatype.startsWith("/image")) {
            throw new ImageValidationException("Invalid image format!");
        }

        FileObject profilePic = new FileObject();
        profilePic.setName(name);
        profilePic.setContentType(mediatype);
        profilePic.setContentLength(new Long(content.length));
        profilePic.setContent(content);

        profilePic = thumbnailService.createProfileThumb(profilePic.getContent(), profilePic.getName());

        profilePic = fileObjectRepository.save(profilePic);
        userService.getUser(user.getId()).setProfilePicId(profilePic.getId());
        userRepository.save(user);
        return profilePic;
    }

    public FileObject getProfilePic(User user) {
        return fileObjectRepository.findOne(user.getProfilePicId());
    }
}
