package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.domain.User;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;

public class TestPerformance {

    static {
        Locale.setDefault(Locale.US);
    }
    /*
     * A note on performance improvements:
     * 
     * The number of users generated for the high volume tests can be easily
     * adjusted via this method:
     * 
     * InternalTestHelper.setInternalUserNumber(100000);
     * 
     * 
     * These tests can be modified to suit new solutions, just as long as the
     * performance metrics at the end of the tests remains consistent.
     * 
     * These are performance metrics that we are trying to hit:
     * 
     * highVolumeTrackLocation: 100,000 users within 15 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     * highVolumeGetRewards: 100,000 users within 20 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */

    // @Ignore
    @Test
    public void highVolumeTrackLocation() throws InterruptedException {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil,
                new RewardCentral());
        // Users should be incremented up to 100,000, and test finishes within
        // 15 minutes
        InternalTestHelper.setInternalUserNumber(10000);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil,
                rewardsService);

        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch countDownLatch = new CountDownLatch(allUsers.size());
        System.out.println(
                "countDownLatch.getCount() = " + countDownLatch.getCount());
        // WHEN
        allUsers.forEach(u -> executorService
                .submit(() -> {
                    tourGuideService.trackUserLocation(u);
                    System.out.println(
                            u.getLastVisitedLocation().timeVisited.toString());
                    countDownLatch.countDown();
                    System.out.println(countDownLatch.getCount());
                }));

        countDownLatch.await();
        executorService.shutdown();
        stopWatch.stop();

        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeTrackLocation: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
                + " seconds.");
        // THEN
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS
                .toSeconds(stopWatch.getTime()));
    }

    @Test
    public void highVolumeGetRewards() throws InterruptedException {
        // GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil,
                new RewardCentral());

        // Users should be incremented up to 100,000, and test finishes within
        // 20 minutes
        InternalTestHelper.setInternalUserNumber(10000);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        TourGuideService tourGuideService = new TourGuideService(gpsUtil,
                rewardsService);
        Attraction attraction = gpsUtil.getAttractions().get(0);
        List<User> allUsers = tourGuideService.getAllUsers();
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        CountDownLatch countDownLatch = new CountDownLatch(allUsers.size());
        System.out.println(
                "countDownLatch.getCount() = " + countDownLatch.getCount());
        // WHEN
        try {
            allUsers.forEach(u -> executorService.submit(() -> {
                u.addToVisitedLocations(
                        new VisitedLocation(u.getUserId(), attraction,
                                new Date()));
                rewardsService.calculateRewards(u);
                System.out.println(
                        u.getEmailAddress() + " : "
                                + u.getUserRewards().get(0));
                assertTrue(u.getUserRewards().size() > 0);
                countDownLatch.countDown();
                System.out.println(countDownLatch.getCount());
            }));
            countDownLatch.await();
        } catch (Exception e) {
            System.out.println("An exception occured : " + e.getMessage());
        }
        executorService.shutdown();
        stopWatch.stop();

        // THEN
        System.out.println("highVolumeGetRewards: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
                + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS
                .toSeconds(stopWatch.getTime()));
    }

}
