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

    public Trip createTrip(String description, Trip.Visibility visibility) {
        Trip trip = new Trip();
        User user = userService.getAuthenticatedUser();        
        trip.setDescription(description);
        trip.setVisibility(visibility);
        trip.setCreator(user);        
        trip = tripRepository.save(trip);
        user.getTrips().add(trip);
        return trip;
    }

    public Trip getTrip(Long id) {
        return tripRepository.findOne(id);
    }

    public void updateTrip(Trip trip) {
        tripRepository.save(trip);
    }

    public void updateTripChanges(Long id, String description, Trip.Visibility visibility) {
        Trip oldTrip = getTrip(id);
        oldTrip.setDescription(description);
        oldTrip.setVisibility(visibility);
        updateTrip(oldTrip);
    }
}
