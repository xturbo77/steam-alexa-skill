package de.xturbo77.alexa.steam.handler;

import com.amazon.speech.speechlet.Context;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.interfaces.system.SystemInterface;
import com.amazon.speech.speechlet.interfaces.system.SystemState;
import com.amazon.speech.speechlet.services.DirectiveEnvelope;
import com.amazon.speech.speechlet.services.DirectiveEnvelopeHeader;
import com.amazon.speech.speechlet.services.DirectiveService;
import com.amazon.speech.speechlet.services.DirectiveServiceClient;
import com.amazon.speech.speechlet.services.SpeakDirective;
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractHandler.class);

    public static final String REPROMT_TEXT = "Du kannst mich zum Beispiel fragen: "
        + "welche Spiele sind neu und angesagt?";

    private final DirectiveService directiveService;
    
    public AbstractHandler(){
        directiveService = new DirectiveServiceClient();
    }

    protected SpeechletResponse newAskResponse(OutputSpeech outputSpeech, String repromptText, Card card) {
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

    /**
     * Helper method that retrieves the system state from the request context.
     *
     * @param context request context.
     * @return SystemState the systemState
     */
    protected final SystemState getSystemState(Context context) {
        return context.getState(SystemInterface.class, SystemState.class);
    }

    /**
     * Dispatches a progressive response.
     *
     * @param requestId the unique request identifier
     * @param text the text of the progressive response to send
     * @param systemState the SystemState object
     * @param apiEndpoint the Alexa API endpoint
     */
    protected void dispatchProgressiveResponse(String requestId, String text, SystemState systemState, String apiEndpoint) {
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

    protected SpeechletResponse newAskResponse(OutputSpeech outputSpeech, String repromptText) {
        return newAskResponse(outputSpeech, repromptText, null);
    }

    protected SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(stringOutput);
        return newAskResponse(outputSpeech, repromptText);
    }

    protected SpeechletResponse newAskResponse(String stringOutput, String repromptText, Card card) {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(stringOutput);
        if(card != null) {
            return newAskResponse(outputSpeech, repromptText, card);
        } else {
            return newAskResponse(outputSpeech, repromptText);
        }
    }

    protected SpeechletResponse sayGoodbye() {
        SsmlOutputSpeech ssmlOutputSpeech = new SsmlOutputSpeech();
        ssmlOutputSpeech.setSsml("<speak><say-as interpret-as=\"interjection\">bis dann</say-as></speak>");
        return SpeechletResponse.newTellResponse(ssmlOutputSpeech);
    }

    protected SpeechletResponse getHelp() {
        String speechOutput
            = "Jetzt sollte ich dir wohl helfen... kann ich aber leider noch nicht.";
        return newAskResponse(speechOutput, REPROMT_TEXT);
    }
}