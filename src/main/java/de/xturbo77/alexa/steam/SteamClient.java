package de.xturbo77.alexa.steam;

import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.enums.VanityUrlType;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamPlayerService;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamStorefront;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamUser;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamFriend;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerProfile;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.SteamPlayerRecentPlayed;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreFeaturedApps;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schmidt
 */
public class SteamClient {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SteamClient.class);

    private final long steamId;

    public SteamClient(long steamid) {
        this.steamId = steamid;
    }

    public StoreFeaturedApps getFeaturedApps() {
        try (SteamWebApiClient c = new SteamWebApiClient(SteamApiToken.TOKEN)) {
            SteamStorefront store = new SteamStorefront(c);
            StoreFeaturedApps featuredApps = store.getFeaturedApps().join();
            return featuredApps;
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return null;
    }

    public Long getSteamIdFromVanityUrl(String userName) {
        try (SteamWebApiClient c = new SteamWebApiClient(SteamApiToken.TOKEN)) {
            SteamUser user = new SteamUser(c);
            return user.getSteamIdFromVanityUrl(userName, VanityUrlType.DEFAULT).join();
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return null;
    }

    public List<SteamFriend> getSteamFriends() {
        try (SteamWebApiClient c = new SteamWebApiClient(SteamApiToken.TOKEN)) {
            SteamUser user = new SteamUser(c);
            List<SteamFriend> friends = user.getFriendList(steamId, "friend").join();
            return friends;
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return new ArrayList<>();
    }

    public List<SteamPlayerProfile> getSteamFriendProfiles() {
        List<SteamPlayerProfile> users = new ArrayList<>();
        try (SteamWebApiClient c = new SteamWebApiClient(SteamApiToken.TOKEN)) {
            List<SteamFriend> friends = getSteamFriends();
            List<Long> steamIds = new ArrayList<>();
            friends.parallelStream()
                .forEach(friend -> steamIds.add(friend.getSteamId()));
            SteamUser user = new SteamUser(c);
            users = user.getPlayerProfiles(steamIds.toArray(new Long[steamIds.size()])).join();
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return users;
    }

    public List<SteamPlayerProfile> getOnlineSteamFriendProfiles() {
        List<SteamPlayerProfile> users = getSteamFriendProfiles();
        users = users.parallelStream()
            .filter(f -> f.getPersonaState() > 0)
            .collect(Collectors.toList());
        return users;
    }

    public List<SteamPlayerRecentPlayed> getRecentlyPlayedGames(Long steamId, int count) {
        try (SteamWebApiClient c = new SteamWebApiClient(SteamApiToken.TOKEN)) {
            SteamPlayerService playerService = new SteamPlayerService(c);
            return playerService.getRecentlyPlayedGames(steamId, count).join();
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return new ArrayList<>();
    }
}
