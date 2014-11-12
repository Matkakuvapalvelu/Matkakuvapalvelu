package wadp.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.domain.Trip;
import wadp.domain.User;
import wadp.repository.TripRepository;

@Service
public class TripService {
    
    @Autowired
    TripRepository tripRepository;

    @Autowired
    UserService userService;
    
    public List<Trip> getAuthenticatedUserTrips() {
        return tripRepository.findByCreator(userService.getAuthenticatedUser());
    }

    public void createTrip(Trip trip) {
        User user = userService.getAuthenticatedUser();
        trip.setCreator(user);
        trip = tripRepository.save(trip);
        user.getTrips().add(trip);        
    }

    public Trip getTrip(Long id) {
        return tripRepository.findOne(id);
    }
}
