package wadp.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.repository.TripRepository;

@Service
public class TripService {
    
    @Autowired
    TripRepository tripRepository;

    @Autowired
    UserService userService;
    
    public List getTripsByOwner() {
        return tripRepository.findByCreator(userService.getAuthenticatedUser());
    }
    
}
