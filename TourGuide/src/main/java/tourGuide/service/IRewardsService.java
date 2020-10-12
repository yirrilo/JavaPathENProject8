package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import tourGuide.domain.User;

public interface IRewardsService {

    /**
     * Setter of proximityBuffer.
     * 
     * @param proximityBuffer
     */
    void setProximityBuffer(int proximityBuffer);

    /**
     * Set proximityBuffer with default value.
     */
    void setDefaultProximityBuffer();

    /**
     * Asynchronous method use to calculate user rewards.
     * 
     * @param user
     * @return a CompletableFuture<?>
     */
    void calculateRewards(User user);

    /**
     * This method checks if the distance between location and attraction and
     * return false if the distance is greater than the attractionProximityRange
     * (and true if not).
     *
     * @param attraction
     * @param location
     * @return a boolean
     */
    boolean isWithinAttractionProximity(Attraction attraction,
            Location location);

    /**
     * Calculation of the distance between two locations.
     *
     * @param loc1
     * @param loc2
     * @return a double: the distance in statute miles
     */
    double getDistance(Location loc1, Location loc2);

    int getRewardPoints(Attraction a, User user);

}