package de.xturbo77.alexa.steam;

import java.util.HashMap;
import java.util.Map;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.User;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.xturbo77.alexa.steam.handler.AbstractIntentHandler;
import de.xturbo77.alexa.steam.handler.DefaultIntentHandler;
import de.xturbo77.alexa.steam.handler.ExitIntentHandler;
import de.xturbo77.alexa.steam.handler.FeaturedGamesIntentHandler;
import de.xturbo77.alexa.steam.handler.HelpIntentHandler;
import de.xturbo77.alexa.steam.handler.OnLaunchHandler;
import de.xturbo77.alexa.steam.handler.TellSteamIdIntentHandler;
import de.xturbo77.alexa.steam.storage.SteamUser;
import de.xturbo77.alexa.steam.storage.SteamUserDynamoDbClient;

/**
 *
 * @author schmidt
 */
public class SteamSpeechlet implements SpeechletV2 {

    private static final Logger LOG = LoggerFactory.getLogger(SteamSpeechlet.class);

    private SteamUserDynamoDbClient dbClient;
    private SteamUser steamUser;

    private Map<String, AbstractIntentHandler> intentHandlers;

    public SteamSpeechlet() {
    }

    private void initializeComponents() {
        if (dbClient == null) {
            AmazonDynamoDBClient amazonDynamoDBClient = new AmazonDynamoDBClient();
            dbClient = new SteamUserDynamoDbClient(amazonDynamoDBClient);
        }

        intentHandlers = new HashMap<>();
        intentHandlers.put(Intents.TELL_STEAMID, new TellSteamIdIntentHandler());
        intentHandlers.put(Intents.FEATURED_APP, new FeaturedGamesIntentHandler());
        intentHandlers.put(Intents.AMAZON_HELP, new HelpIntentHandler());
        AbstractIntentHandler exitHandler = new ExitIntentHandler();
        intentHandlers.put(Intents.AMAZON_CANCEL, exitHandler);
        intentHandlers.put(Intents.AMAZON_STOP, exitHandler);
    }

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        LOG.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
            requestEnvelope.getSession().getSessionId());

        initializeComponents();

        User user = requestEnvelope.getSession().getUser();
        steamUser = new SteamUser(user.getUserId());
        steamUser = dbClient.loadItem(steamUser);
        LOG.info("loaded steamuser from dynamodb: {}", steamUser);
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        OnLaunchHandler onLaunchHandler = new OnLaunchHandler();
        return onLaunchHandler.onLaunch(requestEnvelope, steamUser);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        User user = requestEnvelope.getSession().getUser();
        Session session = requestEnvelope.getSession();

        LOG.info("onIntent requestId={}, userId={} sessionId={}", request.getRequestId(), user.getUserId(), session.getSessionId());

        initializeComponents();

        SpeechletResponse response = null;
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        AbstractIntentHandler defaultHandler = new DefaultIntentHandler();
        if (null == intentName) {
            response = defaultHandler.onIntent(intent, user, session, dbClient, requestEnvelope);
        } else {
            LOG.info("Intent: {}", intentName);
            switch (intentName) {
                case Intents.FEATURED_APP:
                case Intents.TELL_STEAMID: 
                case Intents.AMAZON_HELP:
                case Intents.AMAZON_STOP:
                case Intents.AMAZON_CANCEL: {
                    AbstractIntentHandler handler = intentHandlers.get(intentName);
                    if(handler != null) {
                        response = handler.onIntent(intent, user, session, dbClient, requestEnvelope);
                    }
                    break;
                }
                default: {
                    response = defaultHandler.onIntent(intent, user, session, dbClient, requestEnvelope);
                    break;
                }
            }
        }
        return response;
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        LOG.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
            requestEnvelope.getSession().getSessionId());
    }
    
}
