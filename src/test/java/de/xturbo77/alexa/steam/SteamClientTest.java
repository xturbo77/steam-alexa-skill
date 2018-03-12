package de.xturbo77.alexa.steam;

import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamStorefront;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamFriend;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerRecentPlayed;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreAppDetails;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreFeaturedApps;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author schmidt
 */
public class SteamClientTest {

    private SteamClient steamClient;
    private final long steamId = 76561198012408213L;

    @BeforeEach
    public void setup() {
        steamClient = new SteamClient(steamId);
    }

    @Test
    public void testGetFeaturedApps() {
        StoreFeaturedApps featuredApps = steamClient.getFeaturedApps();
        Assert.assertNotNull(featuredApps);
    }

    @Test
    public void testGetAppDetails() {
        try (SteamWebApiClient c = new SteamWebApiClient()) {
            SteamStorefront store = new SteamStorefront(c);
            StoreAppDetails storeAppDetails = store.getAppDetails(550).join();
            Assert.assertNotNull(storeAppDetails);
            System.out.println("AppDetails: " + storeAppDetails);
            System.out.println("PriceOverview: " + storeAppDetails.getPriceOverview());
        } catch (IOException ex) {
            Logger.getLogger(SteamClientTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testSteamId() {
        Long id = steamClient.getSteamIdFromVanityUrl("necr0sys");
        Assert.assertNotNull(id);
        Assert.assertEquals((Long) steamId, id);
    }

    @Test
    public void testGetSteamFriends() {
        List<SteamFriend> friends = steamClient.getSteamFriends();
        Assert.assertNotNull(friends);
        Assert.assertTrue(friends.size() > 0);
    }

    @Test
    public void testGetSteamFriendProfiles() {
        List<SteamPlayerProfile> friends = steamClient.getSteamFriendProfiles();
        Assert.assertNotNull(friends);
        Assert.assertTrue(friends.size() > 0);
        for (SteamPlayerProfile f : friends) {
            System.out.println("Friend: " + f + " state:" + f.getPersonaState() + " stateFlags:" + f.getPersonaStateFlags());
        }
    }

    @Test
    public void testGetOnlineSteamFriendProfiles() {
        List<SteamPlayerProfile> friends = steamClient.getOnlineSteamFriendProfiles();
        Assert.assertNotNull(friends);
        for (SteamPlayerProfile f : friends) {
            System.out.println("Online Friend: " + f + " state:" + f.getPersonaState());
            List<SteamPlayerRecentPlayed> played = steamClient.getRecentlyPlayedGames(Long.parseLong(f.getSteamId()), 10);
            for (SteamPlayerRecentPlayed p : played) {
                System.out.println("played: " + p.getName() + " appid:" + p.getAppId());
            }
        }
    }
}
