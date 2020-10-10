package tourGuide.unitTests;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import org.mockito.Mock;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.domain.User;
import tourGuide.service.TourGuideService;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class TestTourGuideController {

    @LocalServerPort
    private int port;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mvc;

    @Mock
    TourGuideService tourGuideService;

    @Mock
    User userMocked;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    static {
        Locale.setDefault(Locale.US);
    }

    // GetMapping("/getLocation")
    @Test
    public void when_thenReturnLocation() throws Exception {
        // GIVEN
        User givenUser = new User(null, "Name", "06.07.08.09.10",
                "user@mail.tourguide.com");
        List<VisitedLocation> visitedLocations = new ArrayList<VisitedLocation>();
        VisitedLocation visitedLocation = new VisitedLocation(
                givenUser.getUserId(), new Location(20.04058776650764d,
                        51.29758469836821d),
                Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
        visitedLocations.add(visitedLocation);
        given(tourGuideService.getUser("Name"))
                .willReturn(givenUser);
        given(userMocked.getVisitedLocations())
                .willReturn(visitedLocations);
        given(userMocked.getLastVisitedLocation())
                .willReturn(visitedLocation);
        given(tourGuideService.trackUserLocation(givenUser))
                .willReturn(visitedLocation);
        given(tourGuideService.getUserLocation(givenUser))
                .willReturn(visitedLocation);
        // WHEN
        mvc.perform(MockMvcRequestBuilders.get("/getLocation/?userName=Name"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        // THEN
        verify(tourGuideService).getUserLocation(givenUser);

    }
}
