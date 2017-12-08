package de.xturbo77.alexa.steam;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Context;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.User;
import com.amazon.speech.speechlet.interfaces.system.SystemInterface;
import com.amazon.speech.speechlet.interfaces.system.SystemState;
import com.amazon.speech.speechlet.services.DirectiveEnvelope;
import com.amazon.speech.speechlet.services.DirectiveEnvelopeHeader;
import com.amazon.speech.speechlet.services.DirectiveService;
import com.amazon.speech.speechlet.services.SpeakDirective;
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.Image;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.StandardCard;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreFeaturedAppInfo;
import com.ibasco.agql.protocols.valve.steam.webapi.pojos.StoreFeaturedApps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schmidt
 */
public class SteamSpeechlet implements SpeechletV2 {

    private static final Logger LOG = LoggerFactory.getLogger(SteamSpeechlet.class);
    private static final String REPROMT_TEXT = "Du kannst mich zum Beispiel fragen: "
        + "welche Spiele sind neu und angesagt?";

    private final DirectiveService directiveService;

    public SteamSpeechlet(DirectiveService directiveService) {
        this.directiveService = directiveService;
    }

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        LOG.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
            requestEnvelope.getSession().getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        LOG.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
            requestEnvelope.getSession().getSessionId());

        String speechOutput = "Willkommen zum Steam Helper. " + REPROMT_TEXT;
        String repromptText = "Wenn du Hilfe benötigst sag einfach, hilf mir.";

        return newAskResponse(speechOutput, repromptText);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        User user = requestEnvelope.getSession().getUser();

        LOG.info("onIntent requestId={}, userId={} sessionId={}", request.getRequestId(), user.getUserId(), requestEnvelope.getSession().getSessionId());

        SpeechletResponse response;
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if (null == intentName) {
            String errorSpeech = "Keine Ahnung was du meinst.... vielleicht bin ich zu blöd.";
            response = newAskResponse(errorSpeech, errorSpeech);
            response.setNullableShouldEndSession(Boolean.TRUE);
        } else {
            switch (intentName) {
                case Intents.FEATURED_APP: {
                    try {
                        SystemState systemState = getSystemState(requestEnvelope.getContext());
                        String apiEndpoint = systemState.getApiEndpoint();
                        // Dispatch a progressive response to engage the user while fetching events
                        LOG.info("systemState: {}, apiEndpoint: {}", systemState, apiEndpoint);
                        dispatchProgressiveResponse(request.getRequestId(), "Einen Moment bitte...", systemState, apiEndpoint);
                        response = getFeaturedApps(intent);
                        response.setNullableShouldEndSession(Boolean.TRUE);
                    } catch (Exception ex) {
                        LOG.error(ex.getMessage(), ex);
                        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
                        outputSpeech.setText("Ich kann dir momentan leider nicht helfen. Möglicherweise hat steam gerade ein Problem.");
                        response = SpeechletResponse.newTellResponse(outputSpeech);
                    }
                    break;
                }
                case Intents.AMAZON_HELP: {
                    response = getHelp();
                    break;
                }
                case Intents.AMAZON_STOP: {
                    response = sayGoodbye();
                    break;
                }
                case Intents.AMAZON_CANCEL: {
                    response = sayGoodbye();
                    break;
                }
                default: {
                    String errorSpeech = "Keine Ahnung was du meinst.... vielleicht bin ich zu blöd.";
                    response = newAskResponse(errorSpeech, errorSpeech);
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
                img.setSmallImageUrl(info.getSmallCapsuleImageUrl());
                img.setLargeImageUrl(info.getLargeCapsuleImageUrl());
                card.setImage(img);
            }
        }
        sbSSML.append("</speak>");
        card.setText(sbPlain.toString());
        SsmlOutputSpeech ssmlOutputSpeech = new SsmlOutputSpeech();
        ssmlOutputSpeech.setSsml(sbSSML.toString());

        return newAskResponse(ssmlOutputSpeech, REPROMT_TEXT, card);
    }

    private SpeechletResponse sayGoodbye() {
        SsmlOutputSpeech ssmlOutputSpeech = new SsmlOutputSpeech();
        ssmlOutputSpeech.setSsml("<speak><say-as interpret-as=\"interjection\">bis dann</say-as></speak>");
        return SpeechletResponse.newTellResponse(ssmlOutputSpeech);
    }

    private SpeechletResponse getHelp() {
        String speechOutput
            = "Jetzt sollte ich dir wohl helfen... kann ich aber leider noch nicht.";
        return newAskResponse(speechOutput, REPROMT_TEXT);
    }

    private SpeechletResponse newAskResponse(OutputSpeech outputSpeech, String repromptText, Card card) {
        PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
        repromptOutputSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        if (card != null) {
            return SpeechletResponse.newAskResponse(outputSpeech, reprompt, card);
        } else {
            return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
        }
    }

    private SpeechletResponse newAskResponse(OutputSpeech outputSpeech, String repromptText) {
        return newAskResponse(outputSpeech, repromptText, null);
    }

    private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(stringOutput);
        return newAskResponse(outputSpeech, repromptText);
    }

    /**
     * Dispatches a progressive response.
     *
     * @param requestId the unique request identifier
     * @param text the text of the progressive response to send
     * @param systemState the SystemState object
     * @param apiEndpoint the Alexa API endpoint
     */
    private void dispatchProgressiveResponse(String requestId, String text, SystemState systemState, String apiEndpoint) {
        DirectiveEnvelopeHeader header = DirectiveEnvelopeHeader.builder().withRequestId(requestId).build();
        SpeakDirective directive = SpeakDirective.builder().withSpeech(text).build();
        DirectiveEnvelope directiveEnvelope = DirectiveEnvelope.builder()
            .withHeader(header).withDirective(directive).build();

        if (systemState.getApiAccessToken() != null && !systemState.getApiAccessToken().isEmpty()) {
            String token = systemState.getApiAccessToken();
            try {
                LOG.info("enqueue progressive response with token: {}", token);
                directiveService.enqueue(directiveEnvelope, apiEndpoint, token);
            } catch (Exception e) {
                LOG.error("Failed to dispatch a progressive response", e);
            }
        }
    }

    /**
     * Helper method that retrieves the system state from the request context.
     *
     * @param context request context.
     * @return SystemState the systemState
     */
    private SystemState getSystemState(Context context) {
        return context.getState(SystemInterface.class, SystemState.class);
    }
}
