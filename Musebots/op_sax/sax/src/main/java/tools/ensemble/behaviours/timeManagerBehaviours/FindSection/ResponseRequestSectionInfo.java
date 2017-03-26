package tools.ensemble.behaviours.timeManagerBehaviours.FindSection;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.ensemble.interfaces.DataStoreTimeManager;

import java.security.acl.Acl;


/**
 * Created by OscarAlfonso on 3/25/2017.
 */
public class ResponseRequestSectionInfo extends OneShotBehaviour implements DataStoreTimeManager {

     private int transition = 0;
    private int firtSolo = 0;
     MessageTemplate mt1 = MessageTemplate.and(
             MessageTemplate.MatchConversationId("request-Current-Section-to-Syn"),
             MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
     );

    public ResponseRequestSectionInfo(Agent a)
    {
       super(a);
    }

    public void action()
    {
        ACLMessage replyToComposer = myAgent.receive(mt1);
        if(replyToComposer != null)
        {
            getDataStore().put(INTERNAL_COMPOSER_IN_SYN,replyToComposer.getSender());
            firtSolo = Integer.parseInt(replyToComposer.getContent());

            if (firtSolo != 0)
            {
                //getDataStore().put(FIRST_TIME_SOLO_IN_SYN,firtSolo);
                transition = 1;
            }
            else
            {
                transition = 2;
            }

        }else
        {
            block();
        }
    }

    public int onEnd()
    {
        if (transition == 0)
        {
            block(500);
        }
        return transition;
    }
}
