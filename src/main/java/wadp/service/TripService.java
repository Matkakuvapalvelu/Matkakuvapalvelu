package wadp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import wadp.domain.Post;
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

    public List<Trip> getUserTripsInSortedOrder(User user) {    
        return tripRepository.findByCreator(user, new Sort(Sort.Direction.DESC, "creationDate"));
    }
    
    public List<Trip> getNewestTrips(User user, User requester, int top) {        
        List<Trip> trips = tripRepository.findByCreator(user, new PageRequest(0, top, Sort.Direction.DESC, "creationDate"));
 
        return trips.stream()
                .filter(t -> hasRightToSeeTrip(user, requester, t.getVisibility()))
                .collect(Collectors.toList());
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

    /**
     * Returns list of public trips
     * @return List of trips
     */
    public List<Trip> getPublicTrips() {
        return tripRepository.findByVisibility(Trip.Visibility.PUBLIC);
    }
    
    /**
     * Returns list of top x public trips sorted by value
     * @return List of trips
     */
    public List<Trip> getNewestPublicTrips(int top) {
        return tripRepository.findByVisibility(Trip.Visibility.PUBLIC, new PageRequest(0, top, Sort.Direction.DESC, "creationDate"));
    }
    
    /**
     * Creates a new trip with given description and visibility. Sets given user as the owner.
     *
     * @param description Trip description
     * @param visibility Trip visibility
     * @param creator Trip creator
     * @return Instance of trip after it has been saved to database
     */
    public Trip createTrip(String header, String description, Trip.Visibility visibility, User creator) {
        Trip trip = new Trip();
        trip.setHeader(header);
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

    public void updateTrip(Trip trip, User updater) {
        if (trip.getCreator().getId() != updater.getId()) {
            throw new IllegalArgumentException("Only trip creator has right to change trip details");
        }
        tripRepository.save(trip);
    }

    public void updateTripChanges(Long id, String header, String description, Trip.Visibility visibility, User updater) {
        Trip oldTrip = getTrip(id);
        oldTrip.setHeader(header);
        oldTrip.setDescription(description);
        oldTrip.setVisibility(visibility);
        updateTrip(oldTrip, updater);
    }

    /**
     * A method that determines if a user has right to see a given trip
     * @param tripId id of the trip
     * @param requester user who wants to see the trip
     * @return true if user has right to see the trip, false otherwise
     */
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

    /**
     * Returns list of image coordinates and image ids (latitude/longitude/id) sorted by capture date
     *
     * @param tripId trip id
     * @return List of double arrays with latitude/longitude/id as values
     */
    public List<double[]> getTripImageCoordinates(Long tripId) {

        List<Post> tripPosts= getTrip(tripId).getPosts();
        final List<double[]> coordinates = new ArrayList<>();

        tripPosts
            .stream()
            .filter(x -> x.getImage().getLocation())
            .sorted((p1, p2) -> p1.getImage().getCaptureDate().compareTo(p2.getImage().getCaptureDate()))
            .forEach(p -> coordinates.add(new double[]{p.getImage().getLatitude(), p.getImage().getLongitude(), p.getId()}));

        return coordinates;
    }
    
    /**
     * Returns list of trip start point coordinates and tripId (latitude/longitude/id) sorted by capture date
     * Get trips by user where requester has right to see them
     * If user is null, then get only public trips
     * @param user User whose trips will be returned
     * @param requester User who is requesting list of trips
     * @return List of double arrays with latitude/longitude/id as values
     */
    public List<double[]> getStartpointCoordinatesOfTrips(User user, User requester) {

        List<Trip> trips = getTrips(user, requester);
        
        if(user == null){
            trips = getPublicTrips();
        }
        
        final List<double[]> coordinates = new ArrayList<>();

        trips.stream()
                .forEach(trip -> {
                    if(trip.getPosts().size() > 0){
                        try {
                            Post p = trip.getPosts()
                                    .stream()
                                    .filter(i -> i.getImage().getLocation())
                                    .sorted((p1, p2) -> p1.getImage().getCaptureDate().compareTo(p2.getImage().getCaptureDate()))
                                    .findFirst()
                                    .get();

                            coordinates.add(new double[]{p.getImage().getLatitude(), p.getImage().getLongitude(), trip.getId()});
                        // quick & dirty fix for case where filter removes all entities (no images with location data) and following operations throw
                        } catch (NoSuchElementException ex) {
                            coordinates.add(new double[]{0.0, 0.0, trip.getId()});
                        }
                    }

                });
        
        return coordinates;
    }
    
    /**
     * Returns list of trips where either trip description, header or associated post description contains one or more keywords
     * Note: It would be nice if database would handle the whole operation, but the query complexity exceeds what I am
     * capable of writing right now, so we do sorting here instead
     *
     * @param keyWords List of keywords of which at least one must match trip or trip post
     * @param searcher Who is searching for information
     * @return List of trips that match the keywords and have correct visibility setting
     */
    public List<Trip> searchTripsWithKeywords(List<String> keyWords, User searcher) {

        List<Trip> trips = tripRepository.findAll();
        return trips.stream()
                .filter(trip -> {
                    boolean containsKeyWord = keyWords
                            .stream()
                            .anyMatch(string -> trip.getDescription().toLowerCase().contains(string.toLowerCase()) || 
                                    trip.getHeader().toLowerCase().contains(string.toLowerCase()));

                    // if trip description did not contain keyword, check if any post description contains keyword
                    if (!containsKeyWord) {
                        // I regret nothiiiiiiiing
                        containsKeyWord = trip
                                .getPosts()
                                .stream()
                                .anyMatch(post -> keyWords
                                        .stream()
                                        .anyMatch(string -> post
                                                .getImageText().toLowerCase()
                                                .contains(string.toLowerCase())));
                    }

                    return containsKeyWord && hasRightToSeeTrip(trip.getId(), searcher);

                })
                .collect(Collectors.toList());


    }
    
    public void deleteTrip(Long tripId, User deleter) {        
        if (tripRepository.findOne(tripId).getCreator().getId() != deleter.getId()) {
            throw new IllegalArgumentException("Only trip creator has right to delete trip");
        }
        throw new IllegalArgumentException("Not implemented yet!");
//        tripRepository.delete(tripId);
    }
}
