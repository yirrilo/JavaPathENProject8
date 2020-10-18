package tourGuide.unitTests;

import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.domain.User;
import tourGuide.dto.AttractionsSuggestionDTO;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.ITourGuideService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tripPricer.Provider;

@SpringJUnitConfig(value = TourGuideService.class)
public class TestTourGuideService {

    @Mock
    GpsUtil gpsUtil;
    
    @Mock
    RewardsService rewardsService;
    
    @InjectMocks
    TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
    
    static List<Attraction> attractions = new ArrayList<>();
    static {
        Locale.setDefault(Locale.US);
        attractions.add(new Attraction("Tour Eiffel", "Paris", "France",
                48.858482d, 2.294426d));
        attractions.add(new Attraction("Futuroscope", "Chasseneuil-du-Poitou",
                "France", 46.669752d, 0.368955d));
        attractions.add(new Attraction("Notre Dame", "Paris", "France",
                48.853208d, 2.348640d));
        attractions.add(new Attraction("Musée Automobile", "Vernon", "France",
                46.441387, 0.475771));
        attractions.add(new Attraction("Clos Lucé", "Amboise", "France",
                47.410445, 0.991830));
        attractions.add(new Attraction("Eglise Saint-Jean-Baptiste",
                "Saint-Jean-de-Luz", "France", 47.410445, 0.991830));
        attractions.add(new Attraction("La Rhune", "Ascain", "France",
                43.309685, -1.635410));
        attractions.add(new Attraction("Grand place", "Aras", "France",
                50.292564, 2.781040));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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
        VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(),
                new Location(45d, 1d), new Date()
                 );

        //given(gpsUtil.getAttractions()).willReturn(attractions);
        //given(gpsUtil.getUserLocation(user.getUserId())).willReturn(visitedLocation);
        
        // WHEN
        List<Attraction> attractions = tourGuideService
                .getNearByAttractions(visitedLocation);

        tourGuideService.tracker.stopTracking();
        // THEN
        assertEquals(ITourGuideService.SIZE_OF_NEARBY_ATTRACTIONS_LIST,
                attractions.size());
    }

    @Test
    public void givenAUser_whenGetSuggestions_thenReturnSuggestions() {
        // GIVEN
        InternalTestHelper.setInternalUserNumber(0);

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
                .isEqualTo(ITourGuideService.SIZE_OF_NEARBY_ATTRACTIONS_LIST);
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
