package de.xturbo77.alexa.steam;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import com.amazon.speech.speechlet.services.DirectiveServiceClient;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author schmidt
 */
public class SteamHelperSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

    private static final Set<String> supportedApplicationIds;

    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds = new HashSet<>();
        supportedApplicationIds.add("amzn1.ask.skill.25406815-eee2-423b-9ee1-477b1ee857ff");
    }

    public SteamHelperSpeechletRequestStreamHandler() {
        super(new SteamSpeechlet(new DirectiveServiceClient()), supportedApplicationIds);
    }
}
