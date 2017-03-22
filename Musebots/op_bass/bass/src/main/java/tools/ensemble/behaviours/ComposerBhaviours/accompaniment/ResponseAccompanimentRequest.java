package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.ensemble.interfaces.DataStoreComposer;

/**
 * Created by OscarAlfonso on 3/6/2017.
 */
public class ResponseAccompanimentRequest extends OneShotBehaviour implements DataStoreComposer {

    private int transition = 0;
    private MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchConversationId("request-accompaniment-conversation-REQUEST"),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST));


    public ResponseAccompanimentRequest(Agent a)
    {
        super(a);
    }

    public void action()
    {
        ACLMessage replyRequest = myAgent.receive(mt1);
        if(replyRequest != null)
        {
            //Get the time left on the intro.
            long introTimeLeft = Long.parseLong(replyRequest.getContent());
            //Store it in the vector for share it with the next state.
            getDataStore().put(PLAY_TIME_LEFT,introTimeLeft);
            //Create the message for the reply to the musician.
            ACLMessage replyRequestToMusician = replyRequest.createReply();
            replyRequestToMusician.setConversationId("request-accompaniment-conversation-AGREE");
            replyRequestToMusician.setPerformative(ACLMessage.AGREE);
            replyRequestToMusician.setReplyWith(replyRequest.getSender().getLocalName()+System.currentTimeMillis());
            myAgent.send(replyRequestToMusician);
            if(getDataStore().containsKey(CURRENT_MESSAGE))
            {
                getDataStore().remove(CURRENT_MESSAGE);
                getDataStore().put(CURRENT_MESSAGE,replyRequestToMusician);
            }
            transition = 1;
        }
    }

    public int onEnd()
    {
        return transition;
    }
}
