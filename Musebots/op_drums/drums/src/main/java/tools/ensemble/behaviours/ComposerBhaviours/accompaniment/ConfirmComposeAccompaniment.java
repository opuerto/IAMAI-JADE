package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.ensemble.agents.Composer;
import tools.ensemble.interfaces.DataStoreComposer;

/**
 * Created by OscarAlfonso on 3/23/2017.
 */
public class ConfirmComposeAccompaniment extends OneShotBehaviour implements DataStoreComposer {
    private int firstTimeHere = 0;
    private int transition = 8;
    private MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchConversationId("request-accompaniment-conversation-CONFIRM"),
            MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
    );

    public ConfirmComposeAccompaniment(Agent a)
    {
        super(a);
    }

    public void action()
    {
        ACLMessage replyConfirm = myAgent.receive(mt1);
        if(replyConfirm != null)
        {
            System.out.println("Agent confirm I will Compose");
            ACLMessage replyConfirmToMusician = replyConfirm.createReply();
            replyConfirmToMusician.setPerformative(ACLMessage.INFORM);
            replyConfirmToMusician.setConversationId("request-accompaniment-conversation-INFORM");
            replyConfirmToMusician.setReplyWith(replyConfirm.getSender().getLocalName()+System.currentTimeMillis());
            //System.out.println(replyConfirmToMusician);
            //myAgent.send(replyConfirmToMusician);
            //AccompanimentPianoPart.empty();
            //AccompanimentPhrase.empty();
            //AccompanimentScore.empty();
            //composeChordProgression();
            //getDataStore().put(ACCOMPANIMENT_SCORE,AccompanimentScore);
             myAgent.send(replyConfirmToMusician);

            if(getDataStore().containsKey(FROM_PLAY_TO_COMPOSE))
            {
                getDataStore().remove(FROM_PLAY_TO_COMPOSE);
                getDataStore().put(FROM_PLAY_TO_COMPOSE,true);
            }else
            {
                getDataStore().put(FROM_PLAY_TO_COMPOSE,true);
            }

            if(getDataStore().containsKey(HOLD_COMPOSITION))
            {
                getDataStore().remove(HOLD_COMPOSITION);
                int holdComposition = 0;
                getDataStore().put(HOLD_COMPOSITION,holdComposition);
            }
            else
            {
                int holdComposition = 0;
                getDataStore().put(HOLD_COMPOSITION,holdComposition);
            }

            transition = 1;

        }else {block(100);}

    }

    public int onEnd()
    {
        if(transition == 8 )
        {
            block(1000);
        }
        return transition;
    }
}
