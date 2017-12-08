package de.xturbo77.alexa.steam;

import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreFeaturedAppInfo;

/**
 *
 * @author schmidt
 */
public class FormatterUtils {

    private final static String GAMEINFO_PLAIN = "%s für %10.2f %s\n";
    private final static String GAMEINFO_SSML = "<p>%s für %10.2f %s</p>";

    public static String formatGame(StoreFeaturedAppInfo gameInfo, boolean ssml) {
        final String fmt;
        if (ssml) {
            fmt = GAMEINFO_SSML;
        } else {
            fmt = GAMEINFO_PLAIN;
        }
        return String.format(fmt,
            gameInfo.getName(),
            (double) gameInfo.getFinalPrice() / 100,
            gameInfo.getCurrency());
    }
}
