package tools.ensemble.behaviours.ComposerBhaviours.solo;

import com.sun.org.apache.bcel.internal.classfile.Code;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreComposer;

/**
 * Created by OscarAlfonso on 3/25/2017.
 */
public class RequestInfoSectionToSyn extends OneShotBehaviour implements DataStoreComposer {

    private int transition = 2;
    private int firstTimeHere = 0;
    private AID SynId;
    public RequestInfoSectionToSyn(Agent a)
    {
        super(a);

    }

    public void onStart()
    {
        transition = 2;
        System.out.println("REquest Section to syn in solo");
        ACLMessage requestInfo = new ACLMessage(ACLMessage.REQUEST);
        requestInfo.setConversationId("request-Current-Section-to-Syn");
        requestInfo.setReplyWith(myAgent.getLocalName() + System.currentTimeMillis());
        int firtimeSolo = (Integer) getDataStore().get(FIRST_TIME_SOLO);
        requestInfo.setContent(String.valueOf(firtimeSolo));
        if (getDataStore().containsKey(COMPOSER_MY_INTERNAL_SYNCHRONIZER)) {
            SynId = (AID) getDataStore().get(COMPOSER_MY_INTERNAL_SYNCHRONIZER);
        }

        requestInfo.addReceiver(SynId);
        getDataStore().put(CURRENT_MESSAGE_FOR_SYN, requestInfo);
        myAgent.send(requestInfo);
        transition = 3;
        System.out.println("I just requested transition = "+transition);
    }

    public void action() {

    }

    public int onEnd()
    {
        //firstTimeHere++;
        if(transition == 2)
        {
            block(500);
        }
        return transition;

    }
}
