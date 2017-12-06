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
import com.amazon.speech.speechlet.interfaces.system.SystemInterface;
import com.amazon.speech.speechlet.interfaces.system.SystemState;
import com.amazon.speech.speechlet.services.DirectiveEnvelope;
import com.amazon.speech.speechlet.services.DirectiveEnvelopeHeader;
import com.amazon.speech.speechlet.services.DirectiveService;
import com.amazon.speech.speechlet.services.SpeakDirective;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.ibasco.agql.protocols.valve.steam.webapi.SteamWebApiClient;
import com.ibasco.agql.protocols.valve.steam.webapi.interfaces.SteamStorefront;
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

    private final SteamWebApiClient steamClient = new SteamWebApiClient();
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
        LOG.info("onIntent requestId={}, sessionId={}", request.getRequestId(), requestEnvelope.getSession().getSessionId());

        SpeechletResponse response;
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if (null == intentName) {
            String errorSpeech = "Keine Ahnung was du meinst.... vielleicht bin ich zu blöd.";
            response = newAskResponse(errorSpeech, errorSpeech);
        } else
            switch (intentName) {
                case "FeaturedAppIntent":
                    try {
                        SystemState systemState = getSystemState(requestEnvelope.getContext());
                        String apiEndpoint = systemState.getApiEndpoint();
                        // Dispatch a progressive response to engage the user while fetching events
                        LOG.info("systemState: {}, apiEndpoint: {}", systemState, apiEndpoint);
                        dispatchProgressiveResponse(request.getRequestId(), "Einen Moment bitte...", systemState, apiEndpoint);
                        response = getFeaturedApps(intent);
                    } catch (Exception ex) {
                        LOG.error(ex.getMessage(), ex);
                        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
                        outputSpeech.setText("Ich kann dir momentan leider nicht helfen. Möglicherweise hat steam gerade ein Problem.");
                        response = SpeechletResponse.newTellResponse(outputSpeech);
                    }
                    break;
                case "AMAZON.HelpIntent":
                    response = getHelp();
                    break;
                case "AMAZON.StopIntent": {
                    PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
                    outputSpeech.setText("Tschüss");
                    response = SpeechletResponse.newTellResponse(outputSpeech);
                    break;
                }
                case "AMAZON.CancelIntent": {
                    PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
                    outputSpeech.setText("Tschüss");
                    response = SpeechletResponse.newTellResponse(outputSpeech);
                    break;
                }
                default:
                    String errorSpeech = "Keine Ahnung was du meinst.... vielleicht bin ich zu blöd.";
                    response = newAskResponse(errorSpeech, errorSpeech);
                    break;
            }
        return response;
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        LOG.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
            requestEnvelope.getSession().getSessionId());
    }

    private SpeechletResponse getFeaturedApps(Intent intent) {
        SteamStorefront store = new SteamStorefront(steamClient);
        StoreFeaturedApps featuredApps = store.getFeaturedApps().join();
        StringBuilder sb = new StringBuilder("<speak>");
        sb.append("<s>Neue und angesagte Spiele sind:</s>");
        for (StoreFeaturedAppInfo info : featuredApps.getWindowsFeaturedGames()) {
            sb.append("<p>").append(info.getName()).append(" für ")
                .append((double) info.getFinalPrice() / 100)
                .append(info.getCurrency())
                .append(".</p>");
        }
        sb.append("</speak>");
        SsmlOutputSpeech ssmlOutputSpeech = new SsmlOutputSpeech();
        ssmlOutputSpeech.setSsml(sb.toString());
        return newAskResponse(ssmlOutputSpeech, REPROMT_TEXT);
    }

    private SpeechletResponse getHelp() {
        String speechOutput
            = "Jetzt sollte ich dir wohl helfen... kann ich aber leider noch nicht.";
        return newAskResponse(speechOutput, REPROMT_TEXT);
    }

    private SpeechletResponse newAskResponse(OutputSpeech outputSpeech, String repromptText) {
        PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
        repromptOutputSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);

        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
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
