package wadp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.domain.Trip;
import wadp.domain.User;
import wadp.repository.TripRepository;

@Service
public class TripService {
    
    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private FriendshipService friendshipService;

    public List<Trip> getUserTrips(User user) {
        return tripRepository.findByCreator(user);
    }


    /**
     * Returns list of trips by user if requester has right to see them
     * @param user User whose trips will be returned
     * @param requester User who is requesting list of trips
     * @return List of trips that requester has right to see
     */
    public List<Trip> getTrips(User user, User requester) {
        // probably could just write a single query that checks everything, but the query would likely be rather large
        // and the number of trips is likely fairly small for any user so we filter them here

        List<Trip> trips = tripRepository.findByCreator(user);

        return trips.stream()
                .filter(t -> hasRightToSeeTrip(user, requester, t.getVisibility()))
                .collect(Collectors.toList());


    }

    public Trip createTrip(String description, Trip.Visibility visibility, User creator) {
        Trip trip = new Trip();

        trip.setDescription(description);
        trip.setVisibility(visibility);
        trip.setCreator(creator);
        trip = tripRepository.save(trip);
        creator.getTrips().add(trip);
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

    public boolean hasRightToSeeTrip(Long tripId, User requester) {
       Trip trip = tripRepository.findOne(tripId);
        if (trip == null) {
            return false;
        }

        return hasRightToSeeTrip(trip.getCreator(), requester, trip.getVisibility());
    }

    private boolean hasRightToSeeTrip(User owner, User requester, Trip.Visibility visibility) {

        boolean isOwner = requester != null && requester.getId().equals(owner.getId());

        switch (visibility) {
            case PUBLIC:
                return true;
            case FRIENDS:
                return isOwner || friendshipService.areFriends(owner, requester);
            case PRIVATE:
                return isOwner;
            default:
                return false;
        }
    }
}
