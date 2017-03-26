package tools.ensemble.behaviours.ComposerBhaviours.solo;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import tools.ensemble.interfaces.DataStoreComposer;

/**
 * Created by OscarAlfonso on 3/25/2017.
 */
public class ConfirmToMusician extends OneShotBehaviour implements DataStoreComposer {

    private int transition = 6;
    private int firstTimeHere = 0;
    private AID internalMusician;
    public ConfirmToMusician(Agent a){
        super(a);
    }

    public void action()
    {
        if (firstTimeHere < 1)
        {
            internalMusician = (AID) getDataStore().get(INTERNAL_MUSICIAN_AID);
            System.out.println("the internal composer "+internalMusician);

        }
        ACLMessage confirm = new ACLMessage(ACLMessage.INFORM);
        confirm.setConversationId("request-solo-to-composer-Inform");
        confirm.addReceiver(internalMusician);
        myAgent.send(confirm);
        transition = 11;
    }

    public int onEnd()
    {
      firstTimeHere++;
     return transition;
    }
}
