package de.xturbo77.alexa.steam.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.User;

import de.xturbo77.alexa.steam.storage.SteamUserDynamoDbClient;

public class ExitIntentHandler extends AbstractIntentHandler {

	@Override
	public SpeechletResponse onIntent(Intent intent, User user, Session session, SteamUserDynamoDbClient dbClient,
		SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
		return sayGoodbye();
	}

}