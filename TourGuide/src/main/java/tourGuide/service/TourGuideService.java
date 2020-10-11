package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.domain.User;
import tourGuide.domain.UserReward;
import tourGuide.dto.AttractionsSuggestionDTO;
import tourGuide.dto.NearbyAttractionDTO;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tripPricer.Provider;
import tripPricer.TripPricer;

/**
 * This service layer class is used dealing with external GpsUtil.jar and
 * RewardCentral.jar libraries to manage user rewards.
 *
 * @author TripMaster
 * @author Thierry Schreiner
 */
@Service
public class TourGuideService {
    /**
     * Create a SLF4J/LOG4J LOGGER instance.
     */
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    /**
     * Create a GpsUtil object of the gpsUtil.jar library.
     */
    private final GpsUtil gpsUtil;
    /**
     * Create an instance of RewardsService.
     */
    private final RewardsService rewardsService;
    /**
     * Create and initialize an instance of TripPricer.
     */
    private final TripPricer tripPricer = new TripPricer();
    /**
     * Create an instance of Tracker.
     */
    public final Tracker tracker;
    /**
     * Create a testMode boolean instance initialized at true.
     */
    boolean testMode = true;
    public static final int SIZE_OF_NEARBY_ATTRACTIONS_LIST = 5;

    /**
     * Class constructor
     *
     * @param gpsUtil
     * @param rewardsService
     */
    @Autowired
    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    /**
     * This method calls the UserRewards getter of User class.
     *
     * @param user
     * @return a List<UserReward>
     */
    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    /**
     * This method calls the VisitedLocations getter of User class and then if
     * the list is not empty, get the latest VisitedLocation , else it calls the
     * trackUserLocation method.
     *
     * @param user
     * @return a VisitedLocation
     */
    public VisitedLocation getUserLocation(User user) {
        VisitedLocation visitedLocation = (user.getVisitedLocations()
                .size() > 0) ? user.getLastVisitedLocation()
                        : trackUserLocation(user);
        return visitedLocation;
    }

    /**
     * Get a user (of the internalUserMap) by his userName.
     *
     * @param userName
     * @return
     */
    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    /**
     * Get all users of the internalUserMap.
     *
     * @return
     */
    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    /**
     * This method is used to add a new user to the internalUserMap after
     * checking if he is not already registered.
     *
     * @param user
     */
    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    /**
     * This method use the TripPricer API to provide trip deals based on the
     * given user preferences.
     *
     * @param user
     * @return a List<Provider>
     */
    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream()
                .mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey,
                user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(),
                cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    /**
     * Generates a new VisitedLocation (based on User location) and calculates a
     * Reward for the given user.
     *
     * @param user
     * @return
     */
    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil
                .getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    /**
     * 
     *
     * @param user
     * @return
     */
    public AttractionsSuggestionDTO getAttractionsSuggestion(User user) {
        AttractionsSuggestionDTO suggestion = new AttractionsSuggestionDTO();
        suggestion.setUserLocation(user.getLastVisitedLocation().location);
        Map<String, NearbyAttractionDTO> suggestedAttractions = new HashMap<>();
        List<Attraction> attractionsList = getNearByAttractions(
                user.getLastVisitedLocation());
        attractionsList.stream()
                .sorted(Comparator.comparingDouble(a -> rewardsService
                        .getDistance(user.getLastVisitedLocation().location,
                                a)))
                .forEach(a -> {
                    suggestedAttractions.put(
                            a.attractionName, new NearbyAttractionDTO(
                                    new Location(a.latitude, a.longitude),
                                    rewardsService.getDistance(a,
                                            user.getLastVisitedLocation().location),
                                    rewardsService.getRewardPoints(a, user)));
                });
        suggestion.setSuggestedAttractions(suggestedAttractions);

        return suggestion;
    }

    /**
     * Get the list of the n closest attractions. The number n is defined by the
     * SIZE_OF_NEARBY_ATTRACTIONS_LIST constant.
     *
     * @param visitedLocation
     * @return a List<Attraction>
     */
    public List<Attraction> getNearByAttractions(
            VisitedLocation visitedLocation) {
        List<Attraction> attractions = gpsUtil.getAttractions();

        List<Attraction> nearbyFiveAttractions;

        nearbyFiveAttractions = attractions.stream()
                .sorted(Comparator.comparingDouble(a -> rewardsService
                        .getDistance(visitedLocation.location, a)))
                .limit(SIZE_OF_NEARBY_ATTRACTIONS_LIST)
                .collect(Collectors.toList());
        return nearbyFiveAttractions;
    }

    /**
     * Used to shut down the tracker thread.
     */
    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     * 
     * Methods Below: For Internal Testing
     * 
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing
    // purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    /**
     * This method creates users for tests.
     */
    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber())
                .forEach(i -> {
                    String userName = "internalUser" + i;
                    String phone = "000";
                    String email = userName + "@tourGuide.com";
                    User user = new User(UUID.randomUUID(), userName, phone,
                            email);
                    generateUserLocationHistory(user);
                    logger.debug("user = " + user.toString());
                    internalUserMap.put(userName, user);
                });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber()
                + " internal test users.");
    }

    /**
     * This method creates an history of 3 visited location for the given user,
     * calling 3 sub method to generate randomized latitude, longitude and time.
     *
     * @param user
     */
    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(
                    new VisitedLocation(user.getUserId(),
                            new Location(generateRandomLatitude(),
                                    generateRandomLongitude()),
                            getRandomTime()));
        });
    }

    /**
     * This method return a randomized valid longitude.
     *
     * @return a double
     */
    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    /**
     * This method return a randomized valid latitude.
     *
     * @return a double
     */
    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    /**
     * This method return a randomized LocalDateTime .
     *
     * @return a LocalDateTime
     */
    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now()
                .minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
