package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.domain.User;
import tourGuide.domain.UserReward;

/**
 * This service layer class is used to deal with the external RewardCentral.jar
 * library to manage user rewards.
 *
 * @author TripMaster
 * @author Thierry Schreiner
 */
@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    /**
     * Class constructor.
     * 
     * @param gpsUtil
     * @param rewardCentral
     */
    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    /**
     * Setter of proximityBuffer.
     * 
     * @param proximityBuffer
     */
    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    /**
     * Set proximityBuffer with default value.
     */
    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * Asynchronous method use to calculate user rewards.
     * 
     * @param user
     * @return a CompletableFuture<?>
     */
    public CompletableFuture<?> calculateRewards(User user) {

        List<Attraction> attractions = gpsUtil.getAttractions();

        List<CompletableFuture<?>> futuresList = new ArrayList<>();
        futuresList.add(CompletableFuture
                .runAsync(() -> user.getVisitedLocations().forEach(vl -> {
                    attractions.stream()
                            .filter(a -> nearAttraction(vl, a))
                            .forEach(a -> {
                                if (user.getUserRewards().stream().noneMatch(
                                        r -> r.attraction.attractionName
                                                .equals(a.attractionName))) {
                                    System.out.println("noneMatch");
                                    user.addUserReward(new UserReward(vl, a,
                                            getRewardPoints(a, user)));
                                    System.out
                                            .println(user.getUserName() + vl + a
                                                    + user.getUserRewards()
                                                            .toArray()
                                                            .toString());
                                }
                            });
                })));

        return CompletableFuture
                .allOf(futuresList.stream().toArray(CompletableFuture[]::new));
    }

    /**
     * This method checks if the distance between location and attraction and
     * return false if the distance is greater than the attractionProximityRange
     * (and true if not).
     *
     * @param attraction
     * @param location
     * @return a boolean
     */
    public boolean isWithinAttractionProximity(Attraction attraction,
            Location location) {
        return !(getDistance(attraction, location) > attractionProximityRange);
    }

    /**
     * This method checks if the distance between visitedLocation and attraction
     * and return false if the distance is greater than the proximity buffer
     * (and true if not).
     *
     * @param visitedLocation
     * @param attraction
     * @return a boolean
     */
    private boolean nearAttraction(VisitedLocation visitedLocation,
            Attraction attraction) {
        return !(getDistance(attraction,
                visitedLocation.location) > proximityBuffer);
    }

    /**
     * This method calls a method of the RewardCentral.jar library in order to
     * get the attraction reward points for the given user.
     *
     * @param attraction
     * @param user
     * @return an int: the count of reward points
     */
    private int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId,
                user.getUserId());
    }

    /**
     * Calculation of the distance between two locations.
     *
     * @param loc1
     * @param loc2
     * @return a double: the distance in statute miles
     */
    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math
                .acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1)
                        * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }

}
