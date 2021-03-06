package tools.ensemble.behaviours.ComposerBhaviours.solo;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreComposer;

/**
 * Created by OscarAlfonso on 3/25/2017.
 */
public class ConfirmToMusician extends OneShotBehaviour implements DataStoreComposer {

    private int transition = 6;
    private int firstTimeHere = 0;
    private int holdComposition = 0;
    private AID internalMusician;
    public ConfirmToMusician(Agent a){
        super(a);
    }

    public void onStart()
    {
        System.out.println("confirm to musician in solo composer FSM");
        transition = 6;
        holdComposition = 0;
        internalMusician = (AID) getDataStore().get(INTERNAL_MUSICIAN_AID);
        System.out.println("the internal composer "+internalMusician);
        //from support to lead is the first time playing solo again
        if (Musician.getFromSupportTolead())
        {
            getDataStore().remove(FIRST_TIME_SOLO);
            getDataStore().put(FIRST_TIME_SOLO,1);
        }
    }

    public void action()
    {

        ACLMessage confirm = new ACLMessage(ACLMessage.INFORM);
        confirm.setConversationId("request-solo-to-composer-Inform");
        confirm.addReceiver(internalMusician);
        myAgent.send(confirm);
        System.out.println("inform send confirm");

        if(getDataStore().containsKey(HOLD_COMPOSITION))
        {
            getDataStore().remove(HOLD_COMPOSITION);
            getDataStore().put(HOLD_COMPOSITION,holdComposition);
        }
        else
        {
            getDataStore().put(HOLD_COMPOSITION,holdComposition);
        }
        transition = 7;
    }

    public int onEnd()
    {
      firstTimeHere++;
     return transition;
    }
}
