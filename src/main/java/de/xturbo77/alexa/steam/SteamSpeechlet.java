package de.xturbo77.alexa.steam;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamStorefront;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreFeaturedAppInfo;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreFeaturedApps;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schmidt
 */
public class SteamSpeechlet implements SpeechletV2 {

    private static final Logger LOG = LoggerFactory.getLogger(SteamSpeechlet.class);
    private static final String STEAM_CLIENT = "SteamClient";
    private static final String REPROMT_TEXT = "Du kannst mich zum Beispiel fragen: "
        + "welche Spiele sind neu und angesagt?";

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        final SteamWebApiClient steamClient = new SteamWebApiClient();
        requestEnvelope.getSession().setAttribute(STEAM_CLIENT, steamClient);
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        LOG.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
            requestEnvelope.getSession().getSessionId());

        String speechOutput
            = "Willkommen zum Steam Helper. " + REPROMT_TEXT;
        String repromptText = "Wenn du Hilfe benötigst sag einfach, hilf mir.";

        return newAskResponse(speechOutput, repromptText);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        LOG.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
            requestEnvelope.getSession().getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("RecipeIntent".equals(intentName)) {
            SteamWebApiClient steamClient = (SteamWebApiClient) requestEnvelope.getSession().getAttribute(STEAM_CLIENT);
            return getFeaturedApps(intent, steamClient);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelp();
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");
            return SpeechletResponse.newTellResponse(outputSpeech);
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Tschüss");
            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            String errorSpeech = "Keine Ahnung was du meinst.... vielleicht bin ich zu blöd.";
            return newAskResponse(errorSpeech, errorSpeech);
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        SteamWebApiClient steamClient = (SteamWebApiClient) requestEnvelope.getSession().getAttribute(STEAM_CLIENT);
        if (steamClient != null) {
            try {
                steamClient.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

    private SpeechletResponse getFeaturedApps(Intent intent, SteamWebApiClient steamClient) {
        SteamStorefront store = new SteamStorefront(steamClient);
        StoreFeaturedApps featuredApps = store.getFeaturedApps().join();
        StringBuilder sb = new StringBuilder("Neue und angesagte Spiele sind: ");
        for (StoreFeaturedAppInfo info : featuredApps.getWindowsFeaturedGames()) {
            sb.append(info.getName()).append(" für ")
                .append((double) info.getFinalPrice() / 100)
                .append(info.getCurrency())
                .append(". ");
        }
        return newAskResponse(sb.toString(), REPROMT_TEXT);
    }

    private SpeechletResponse getHelp() {
        String speechOutput
            = "Jetzt sollte ich dir wohl helfen... kann ich aber leider noch nicht.";
        return newAskResponse(speechOutput, REPROMT_TEXT);
    }

    /**
     * Wrapper for creating the Ask response. The OutputSpeech and {@link Reprompt} objects are created from the input strings.
     *
     * @param stringOutput the output to be spoken
     * @param repromptText the reprompt for if the user doesn't reply or is misunderstood.
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(stringOutput);

        PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
        repromptOutputSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);

        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

}
