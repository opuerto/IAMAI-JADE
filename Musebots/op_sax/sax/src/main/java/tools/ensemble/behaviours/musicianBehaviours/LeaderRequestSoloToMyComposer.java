package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import tools.ensemble.interfaces.DataStorteMusicians;

/**
 * Created by OscarAlfonso on 3/23/2017.
 * This behaviour with request to the agent composer to perform a solo
 *
 */
public class LeaderRequestSoloToMyComposer extends OneShotBehaviour implements DataStorteMusicians{

    Agent agent;
    //Check if is the first time doing this action
    private int firstTimeHere = 0;
    private  int transition = 30;
    //Check if is the first solo to be played in the song, if so, Wait until the end of the head.
    private int firstTimeSolo;
    //Messages
    private ACLMessage requestMessage = new ACLMessage(ACLMessage.REQUEST);
    private ACLMessage replyAgree = new ACLMessage(ACLMessage.CONFIRM);

    private AID internalComposer = null;

    //States
    private static final String REQUEST_PLAY = "requestPlay";
    private static final String HANDLE_AGREE = "handleAgree";
    private static final String HANDLE_CONFIRM = "handleConfirm";


    public LeaderRequestSoloToMyComposer(Agent a)
    {
         super(a);
        this.agent = a;
    }

    public void action()
    {
        if(firstTimeHere < 1)
        {
            if(getDataStore().containsKey(FIRST_SOLO))
            {
                firstTimeSolo = 1;
            }
            else
            {
                firstTimeSolo = 0;
            }

            FSMBehaviour requestSolo = new FSMBehaviour(agent);
            requestSolo.registerFirstState(new tempClass(),REQUEST_PLAY);
            requestSolo.registerState(new tempClass(),HANDLE_AGREE);
            requestSolo.registerLastState(new tempClass(),HANDLE_CONFIRM);

            //Register the transitions
            requestSolo.registerTransition(REQUEST_PLAY,HANDLE_AGREE,0);
            requestSolo.registerTransition(HANDLE_AGREE,HANDLE_AGREE,2);
            requestSolo.registerTransition(HANDLE_AGREE,HANDLE_CONFIRM,3);

            agent.addBehaviour(requestSolo);

        }
    }

    public int onEnd()
    {
        firstTimeHere++;
        return transition;
    }

    private class RequestPlayBehaviour extends OneShotBehaviour
    {
        int transition;
        public RequestPlayBehaviour()
        {
            super(agent);
        }

        public void action()
        {
            //Todo: Implementar la conversacion con el composer pasar the flag first time solo

        }

        public int onEnd()
        {
            return transition;
        }
    }

    private class tempClass extends OneShotBehaviour
    {
        public void action()
        {
            System.out.println("Action");
        }
    }
}
