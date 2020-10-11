package tourGuide.dto;

import java.util.Map;

import gpsUtil.location.Location;

public class AttractionsSuggestionDTO {

    /**
     * The latest position of the user.
     */
    private Location userLocation;

    /**
     * A Map collection of nearest suggested attractions.
     */
    private Map<String, NearbyAttractionDTO> suggestedAttractions;

    /**
     * Getter of userLocation.
     *
     * @return a Location (latitude and longitude)
     */
    public Location getUserLocation() {
        return userLocation;
    }

    /**
     * Setter of userLocation.
     *
     * @param userLocation
     */
    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    /**
     * Getter of suggestedAttraction.
     *
     * @return a Map<String, NearbyAttractionsDTO> with the attraction name as
     *         key, and the NearbyAttractionsDTO (location, distance,
     *         userReward) as value.
     */
    public Map<String, NearbyAttractionDTO> getSuggestedAttraction() {
        return suggestedAttractions;
    }

    /**
     * Setter of suggestedAttraction.
     *
     * @param suggestedAttraction
     */
    public void setSuggestedAttractions(
            Map<String, NearbyAttractionDTO> suggestedAttractions) {
        this.suggestedAttractions = suggestedAttractions;
    }

}
