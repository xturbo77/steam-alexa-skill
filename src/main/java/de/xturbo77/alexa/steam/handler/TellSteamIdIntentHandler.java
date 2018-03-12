package de.xturbo77.alexa.steam.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.User;
import com.amazon.speech.ui.PlainTextOutputSpeech;

import de.xturbo77.alexa.steam.storage.SteamUser;
import de.xturbo77.alexa.steam.storage.SteamUserDynamoDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TellSteamIdIntentHandler extends AbstractIntentHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TellSteamIdIntentHandler.class);

    public SpeechletResponse onIntent(Intent intent, User user, Session session, SteamUserDynamoDbClient dbClient,
        SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        Slot id01 = intent.getSlot("idOne");
        Slot id02 = intent.getSlot("idTwo");
        Slot id03 = intent.getSlot("idThree");
        Slot id04 = intent.getSlot("idFour");
        Slot id05 = intent.getSlot("idFive");
        Slot id06 = intent.getSlot("idSix");
        Slot id07 = intent.getSlot("idSeven");
        Slot id08 = intent.getSlot("idEight");
        Slot id09 = intent.getSlot("idNine");
        Slot id10 = intent.getSlot("idTen");
        Slot id11 = intent.getSlot("idEleven");
        Slot id12 = intent.getSlot("idTwelve");
        Slot id13 = intent.getSlot("idThirteen");
        Slot id14 = intent.getSlot("idFourteen");
        Slot id15 = intent.getSlot("idFifteen");
        Slot id16 = intent.getSlot("idSixteen");
        Slot id17 = intent.getSlot("idSeventeen");

        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        try {
            String steamId = id01.getValue() + id02.getValue() + id03.getValue() + id04.getValue() + id05.getValue()
                + id06.getValue() + id07.getValue() + id08.getValue() + id09.getValue() + id10.getValue() + id11.getValue()
                + id12.getValue() + id13.getValue() + id14.getValue() + id15.getValue() + id16.getValue() + id17.getValue();
            if(steamId != null) {
                SteamUser steamUser = new SteamUser(user.getUserId());
                steamUser.setSteamId(steamId);
                dbClient.saveItem(steamUser);
                outputSpeech.setText("Ok, ich habe deine Steam Account Nummer gespeichert");
            }
            return SpeechletResponse.newTellResponse(outputSpeech);
        } catch(NullPointerException ex) {
            LOG.warn("Missing slot(s) for SteamID", ex);
            String text = "Das scheint keine gültige Steam ID zu sein. Deine Steam ID muss eine 17-stellige Nummer sein.";
            return newAskResponse(text, text);
        }
    }
}