package tourGuide.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

@RestController
public class TourGuideController {

    /**
     * Asks Spring to inject a TourGuideService bean when TourGuideController is
     * created.
     */
    @Autowired
    TourGuideService tourGuideService;

    /**
     * HTML GET request that returns a welcome message.
     *
     * @return a String, the welcome message
     */
    @GetMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * HTML GET request that returns the location of the user who responds to
     * the given userName parameter.
     *
     * @param userName
     * @return a String
     */
    @GetMapping("/getLocation")
    public String getLocation(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService
                .getUserLocation(getUser(userName));
        return JsonStream.serialize(visitedLocation.location);
    }

    /**
     * HTML GET request that returns a List of nearby Attractions: attractions
     * localized in the proximity range defined by the RewardsService
     * attractionProximityRange attribute, initially set to 200 miles.
     *
     * @param userName
     * @return a String
     * @see RewardsService
     */
    @GetMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String userName) {
        // TODO: Change this method to no longer return a List of Attractions.
        // Instead: Get the closest five tourist attractions to the user - no
        // matter
        // how far away they are.
        // Return a new JSON object that contains:
        // Name of Tourist attraction,
        // Tourist attractions lat/long,
        // The user's location lat/long,
        // The distance in miles between the user's location and each of the
        // attractions.
        // The reward points for visiting each Attraction.
        // Note: Attraction reward points can be gathered from RewardsCentral
        VisitedLocation visitedLocation = tourGuideService
                .getUserLocation(getUser(userName));
        return JsonStream.serialize(
                tourGuideService.getNearByAttractions(visitedLocation));
    }

    /**
     * HTML GET request that returns a List of UserRewwards of the user who
     * responds to the given userName parameter.
     *
     * @param userName
     * @return a String the serialized List of UserRewwards
     */
    @GetMapping("/getRewards")
    public String getRewards(@RequestParam String userName) {
        return JsonStream
                .serialize(tourGuideService.getUserRewards(getUser(userName)));
    }

    /**
     * HTML GET request that returns a List of every user's most recent location
     * using user's current location stored in location history.
     *
     * @return a String
     */
    @GetMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        // TODO: Get a list of every user's most recent location as JSON
        // - Note: does not use gpsUtil to query for their current location,
        // but rather gathers the user's current location from their stored
        // location history.
        //
        // Return object should be the just a JSON mapping of userId to
        // Locations similar to:
        // {
        // "019b04a9-067a-4c76-8817-ee75088c3822":
        // {"longitude":-48.188821,"latitude":74.84371}
        // ...
        // }

        return JsonStream.serialize("");
    }

    /**
     * HTML GET request that returns a List of TripDeals suggested to the user
     * who responds to the given userName parameter.
     *
     * @param userName
     * @return a String
     */
    @GetMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService
                .getTripDeals(getUser(userName));
        return JsonStream.serialize(providers);
    }

    /**
     * Returns the User who responds to the given userName parameter. used by
     * previous HTML request. This private sub method is used by pevious html
     * requests.
     *
     * @param userName
     * @return
     */
    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }

}
