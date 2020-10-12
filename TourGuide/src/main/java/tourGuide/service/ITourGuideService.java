package tourGuide.service;

import java.util.List;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import tourGuide.domain.User;
import tourGuide.domain.UserReward;
import tourGuide.dto.AttractionsSuggestionDTO;
import tripPricer.Provider;

public interface ITourGuideService {

    int SIZE_OF_NEARBY_ATTRACTIONS_LIST = 5;

    /**
     * This method calls the UserRewards getter of User class.
     *
     * @param user
     * @return a List<UserReward>
     */
    List<UserReward> getUserRewards(User user);

    /**
     * This method calls the VisitedLocations getter of User class and then if
     * the list is not empty, get the latest VisitedLocation , else it calls the
     * trackUserLocation method.
     *
     * @param user
     * @return a VisitedLocation
     */
    VisitedLocation getUserLocation(User user);

    /**
     * Get a user (of the internalUserMap) by his userName.
     *
     * @param userName
     * @return
     */
    User getUser(String userName);

    /**
     * Get all users of the internalUserMap.
     *
     * @return
     */
    List<User> getAllUsers();

    /**
     * This method is used to add a new user to the internalUserMap after
     * checking if he is not already registered.
     *
     * @param user
     */
    void addUser(User user);

    /**
     * This method use the TripPricer API to provide trip deals based on the
     * given user preferences.
     *
     * @param user
     * @return a List<Provider>
     */
    List<Provider> getTripDeals(User user);

    /**
     * Generates a new VisitedLocation (based on User location) and calculates a
     * Reward for the given user.
     *
     * @param user
     * @return
     */
    VisitedLocation trackUserLocation(User user);

    /**
     * 
     *
     * @param user
     * @return
     */
    AttractionsSuggestionDTO getAttractionsSuggestion(User user);

    /**
     * Get the list of the n closest attractions. The number n is defined by the
     * SIZE_OF_NEARBY_ATTRACTIONS_LIST constant.
     *
     * @param visitedLocation
     * @return a List<Attraction>
     */
    List<Attraction> getNearByAttractions(
            VisitedLocation visitedLocation);

}