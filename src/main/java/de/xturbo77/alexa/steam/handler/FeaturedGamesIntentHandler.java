package de.xturbo77.alexa.steam.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.User;
import com.amazon.speech.speechlet.interfaces.system.SystemState;
import com.amazon.speech.ui.Image;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.StandardCard;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreFeaturedAppInfo;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreFeaturedApps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.xturbo77.alexa.steam.FormatterUtils;
import de.xturbo77.alexa.steam.SteamClient;
import de.xturbo77.alexa.steam.storage.SteamUserDynamoDbClient;

public class FeaturedGamesIntentHandler extends AbstractIntentHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FeaturedGamesIntentHandler.class);

	@Override
    public SpeechletResponse onIntent(Intent intent, User user, Session session, SteamUserDynamoDbClient dbClient,
        SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        SpeechletResponse response = null;
		try {
            SystemState systemState = getSystemState(requestEnvelope.getContext());
            String apiEndpoint = systemState.getApiEndpoint();
            // Dispatch a progressive response to engage the user while fetching events
            LOG.info("systemState: {}, apiEndpoint: {}", systemState, apiEndpoint);
            dispatchProgressiveResponse(requestEnvelope.getRequest().getRequestId(), "Einen Moment bitte...", systemState, apiEndpoint);
            response = getFeaturedApps(intent);
            response.setNullableShouldEndSession(Boolean.TRUE);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Ich kann dir momentan leider nicht helfen. Möglicherweise hat Steam gerade ein Problem.");
            response = SpeechletResponse.newTellResponse(outputSpeech);
        }
        return response;
    }
    
    private SpeechletResponse getFeaturedApps(Intent intent) {
        SteamClient sc = new SteamClient(0);
        StoreFeaturedApps featuredApps = sc.getFeaturedApps();
        StandardCard card = new StandardCard();
        card.setTitle("Neue und angesagte Spiele");
        Image img = null;
        StringBuilder sbSSML = new StringBuilder("<speak>");
        StringBuilder sbPlain = new StringBuilder();
        sbSSML.append("<s>Neue und angesagte Spiele sind:</s>");
        for (StoreFeaturedAppInfo info : featuredApps.getWindowsFeaturedGames()) {
            sbSSML.append(FormatterUtils.formatGame(info, true));
            sbPlain.append(FormatterUtils.formatGame(info, false));
            if (img == null) {
                img = new Image();
                img.setSmallImageUrl(info.getSmallCapsuleImageUrl().replace("http:", "https:"));
                img.setLargeImageUrl(info.getLargeCapsuleImageUrl().replace("http:", "https:"));
                card.setImage(img);
            }
        }
        sbSSML.append("</speak>");
        card.setText(sbPlain.toString());
        SsmlOutputSpeech ssmlOutputSpeech = new SsmlOutputSpeech();
        ssmlOutputSpeech.setSsml(sbSSML.toString());

        return newAskResponse(ssmlOutputSpeech, AbstractIntentHandler.REPROMT_TEXT, card);
    }

}