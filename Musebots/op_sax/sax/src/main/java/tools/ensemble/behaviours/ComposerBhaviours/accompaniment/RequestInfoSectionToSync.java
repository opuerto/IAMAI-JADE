package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import tools.ensemble.agents.Composer;
import tools.ensemble.interfaces.DataStoreComposer;

/**
 * Created by OscarAlfonso on 3/29/2017.
 */
public class RequestInfoSectionToSync extends OneShotBehaviour implements DataStoreComposer {

    private int transition = 14;
    private int firstTimeHere = 0;
    private AID SynId;
    public RequestInfoSectionToSync(Agent a)
    {
        super(a);
    }

    public void onStart()
    {
        ACLMessage requestInfo = new ACLMessage(ACLMessage.REQUEST);
        requestInfo.setConversationId("request-Current-Section-to-Syn");
        requestInfo.setReplyWith(myAgent.getLocalName() + System.currentTimeMillis());
        int firstimeAccompaniement = 1;
        requestInfo.setContent(String.valueOf(firstimeAccompaniement));
        if (getDataStore().containsKey(COMPOSER_MY_INTERNAL_SYNCHRONIZER)) {
            SynId = (AID) getDataStore().get(COMPOSER_MY_INTERNAL_SYNCHRONIZER);
            System.out.println(SynId);
        }

        requestInfo.addReceiver(SynId);
        getDataStore().put(CURRENT_MESSAGE_FOR_SYN, requestInfo);
        myAgent.send(requestInfo);
        transition = 14;
        System.out.println("receiver "+SynId);
    }
    public void action() {

    }

    public int onEnd()
    {
        //firstTimeHere++;
        //Composer.firstTime++;
        if(transition == 2)
        {
            block(1000);
        }
        return transition;

    }
}
