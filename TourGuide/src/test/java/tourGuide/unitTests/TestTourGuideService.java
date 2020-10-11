package tourGuide.unitTests;

import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.domain.User;
import tourGuide.dto.AttractionsSuggestionDTO;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tripPricer.Provider;

@SpringJUnitConfig(value = TourGuideService.class)
public class TestTourGuideService {

    static {
        Locale.setDefault(Locale.US);
    }

    @Test
    public void givenAUser_whenGetUserLocation_thenReturnsHisVisitedLocation() {
        // GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil,
                new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil,
                rewardsService);
        User user = new User(UUID.randomUUID(), "jon", "000",
                "jon@tourGuide.com");
        // WHEN
        VisitedLocation visitedLocation = tourGuideService
                .trackUserLocation(user);
        tourGuideService.tracker.stopTracking();
        // THEN
        assertThat(visitedLocation.userId).isEqualTo(user.getUserId());
    }

    @Test
    public void givenAUser_whenAddUser_thenUserIsRegistred() {
        // GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil,
                new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil,
                rewardsService);
        User user = new User(UUID.randomUUID(), "jon", "000",
                "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000",
                "jon2@tourGuide.com");
        // WHEN
        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);
        tourGuideService.tracker.stopTracking();
        // THEN
        User retrivedUser = tourGuideService.getUser(user.getUserName());
        User retrivedUser2 = tourGuideService.getUser(user2.getUserName());
        assertThat(retrivedUser).isEqualTo(user);
        assertThat(retrivedUser2).isEqualTo(user2);
    }

    @Test
    public void givenFewUsers_whenGetAllUsers_thenReturnsAllUsersList() {
        // GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil,
                new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil,
                rewardsService);
        User user = new User(UUID.randomUUID(), "jon", "000",
                "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000",
                "jon2@tourGuide.com");
        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);
        // WHEN
        List<User> allUsers = tourGuideService.getAllUsers();
        tourGuideService.tracker.stopTracking();
        // THEN
        assertThat(allUsers).containsExactly(user, user2);
    }

    @Test
    public void givenAUser_whenTrackUser_thenReturnsHisVisitedLocation() {
        // GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil,
                new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil,
                rewardsService);
        User user = new User(UUID.randomUUID(), "jon", "000",
                "jon@tourGuide.com");
        // WHEN
        VisitedLocation visitedLocation = tourGuideService
                .trackUserLocation(user);
        tourGuideService.tracker.stopTracking();
        // THEN
        assertThat(visitedLocation.userId).isEqualTo(user.getUserId());
        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    @Test
    public void givenAUser_whenGetNearbyAttractions_thenReturnAListOfNAttractions() {
        // GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil,
                new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil,
                rewardsService);
        User user = new User(UUID.randomUUID(), "jon", "000",
                "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService
                .trackUserLocation(user);
        // WHEN
        List<Attraction> attractions = tourGuideService
                .getNearByAttractions(visitedLocation);

        tourGuideService.tracker.stopTracking();
        // THEN
        assertEquals(TourGuideService.SIZE_OF_NEARBY_ATTRACTIONS_LIST,
                attractions.size());
    }

    @Test
    public void givenAUser_whenGetSuggestions_thenReturnSuggestions() {
        // GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil,
                new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil,
                rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000",
                "jon@tourGuide.com");
        tourGuideService.trackUserLocation(user);
        // WHEN
        AttractionsSuggestionDTO suggestion = tourGuideService
                .getAttractionsSuggestion(user);

        tourGuideService.tracker.stopTracking();
        // THEN
        assertThat(suggestion.getUserLocation())
                .isEqualTo(user.getLastVisitedLocation().location);
        assertThat(suggestion.getSuggestedAttraction().size())
                .isEqualTo(TourGuideService.SIZE_OF_NEARBY_ATTRACTIONS_LIST);
    }

    public void getTripDeals() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil,
                new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil,
                rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000",
                "jon@tourGuide.com");

        List<Provider> providers = tourGuideService.getTripDeals(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(10, providers.size());
    }

}
