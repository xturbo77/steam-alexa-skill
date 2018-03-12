package de.xturbo77.alexa.steam.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.StandardCard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.xturbo77.alexa.steam.storage.SteamUser;

public class OnLaunchHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OnLaunchHandler.class);

    public OnLaunchHandler() {
        super();
    }

    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope, SteamUser steamUser) {
        Session session = requestEnvelope.getSession();
        LOG.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
            session.getSessionId());

        StandardCard card = new StandardCard();
        String speechOutput = "Willkommen zum Steam Helper. ";
        card.setTitle("Steam Helper");
        if (steamUser != null) {
            speechOutput = speechOutput + AbstractIntentHandler.REPROMT_TEXT;
        } else {
            speechOutput = speechOutput + "Ich kenne deine Steam ID noch nicht. "
                + "Wenn du sie mir sagen möchtest sage einfach: 'Alexa, sag steam meine account nummer lautet: Gefolgt von deiner 17 stelligen Steam ID'. "
                + "Wenn du deine Steam ID nicht kennst, besuche die Seite https://steamidfinder.com/.";
        }
        card.setText(speechOutput);
        String repromptText = "Wenn du Hilfe benötigst sag einfach, hilf mir.";

        return newAskResponse(speechOutput, repromptText, card);
    }

}