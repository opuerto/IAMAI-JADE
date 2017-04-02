package tools.ensemble.behaviours.ComposerBhaviours.solo;

import com.sun.org.apache.bcel.internal.classfile.Code;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.interfaces.DataStoreTimeManager;

/**
 * Created by OscarAlfonso on 3/24/2017.
 */
public class ResponseSoloRequest extends OneShotBehaviour implements DataStoreComposer,DataStoreTimeManager  {

    int transition = 0;
    int firstTimeHere = 0;
    int firsTimeSolo;
    Ontology SynOntology;
    Codec codec;
    MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchConversationId("Request-Solo-To-Composer"),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
    );
    AID internalTimeManager;
    public ResponseSoloRequest(Agent a, Ontology ontology, Codec codec)
    {
        super(a);
        this.SynOntology = ontology;
        this.codec = codec;

    }
    public void onStart()
    {
        transition = 0;
        System.out.println("Response to the solo request in composer");

    }
    public void action()
    {

        ACLMessage replyRequest = myAgent.receive(mt1);
        if(replyRequest != null)
        {
            System.out.println("We got the message in composer is "+replyRequest);
            getDataStore().put(INTERNAL_MUSICIAN_AID,replyRequest.getSender());
            if(getDataStore().containsKey(COMPOSER_MY_INTERNAL_SYNCHRONIZER))
            {
                internalTimeManager = (AID) getDataStore().get(COMPOSER_MY_INTERNAL_SYNCHRONIZER);
            }
            //Check if this is the first solo played in the song
            firsTimeSolo = Integer.parseInt(replyRequest.getContent());
            getDataStore().put(FIRST_TIME_SOLO,firsTimeSolo);
            ACLMessage replyToMusician = replyRequest.createReply();
            replyToMusician.setPerformative(ACLMessage.AGREE);
            replyToMusician.setConversationId("request-solo-to-composer-agree");
            replyToMusician.setReplyWith(replyRequest.getSender().getLocalName()+System.currentTimeMillis());
            myAgent.send(replyToMusician);
            if(getDataStore().containsKey(CURRENT_MESSAGE_FOR_MUSICIAN))
            {
                getDataStore().remove(CURRENT_MESSAGE_FOR_MUSICIAN);
                getDataStore().put(CURRENT_MESSAGE_FOR_MUSICIAN,replyRequest);
            }
            else
            {
                getDataStore().put(CURRENT_MESSAGE_FOR_MUSICIAN,replyRequest);
            }
            transition = 1;


        }else
        {
            block();
        }



    }

    public int onEnd()
    {
        firstTimeHere++;
        if (transition == 0)
        {
            block(500);
        }
        return transition;
    }
}
