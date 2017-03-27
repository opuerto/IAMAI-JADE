package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sun.swing.plaf.synth.SynthIcon;
import tools.ensemble.agents.Composer;
import tools.ensemble.interfaces.DataStoreComposer;

/**
 * Created by OscarAlfonso on 3/6/2017.
 */
public class ResponseAccompanimentRequest extends OneShotBehaviour implements DataStoreComposer {

    private int transition = 0;
    private MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchConversationId("request-accompaniment-conversation-REQUEST"),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
    FSMBehaviour intro;
    public ResponseAccompanimentRequest(Agent a, FSMBehaviour intro)
    {
        super(a);
        this.intro = intro;
    }

    public void action()
    {
        ACLMessage replyRequest = myAgent.receive(mt1);
        if(replyRequest != null)
        {
            getDataStore().put(INTERNAL_MUSICIAN_AID,replyRequest.getSender());
            //Get the time left on the intro.
            long introTimeLeft = Long.parseLong(replyRequest.getContent());
            //Store it in the vector for share it with the next state.
            //getDataStore().put(PLAY_TIME_LEFT,introTimeLeft);
            Composer.sectionPlayLeft = introTimeLeft;
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
            myAgent.removeBehaviour(intro);
            transition = 9;
        }else{block();}
    }

    public int onEnd()
    {
        if(transition == 0)
        {
            block(500);
        }
        return transition;
    }
}
