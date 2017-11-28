package de.xturbo77.alexa.steam;

import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamStorefront;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreFeaturedAppInfo;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreFeaturedApps;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author schmidt
 */
public class SteamTester {

    public static void main(String[] args) {
        try (SteamWebApiClient c = new SteamWebApiClient()) {
            SteamStorefront store = new SteamStorefront(c);
            StoreFeaturedApps featuredApps = store.getFeaturedApps().join();
            System.out.println("Featured apps: " + featuredApps);
            StringBuilder sb = new StringBuilder("Neue und angesagte Spiele sind: ");
            for (StoreFeaturedAppInfo info : featuredApps.getWindowsFeaturedGames()) {
                sb.append(info.getName()).append(" für ")
                    .append((double) info.getFinalPrice() / 100)
                    .append(info.getCurrency())
                    .append(". ");
            }
            System.out.println(sb);
        } catch (IOException ex) {
            Logger.getLogger(SteamTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
