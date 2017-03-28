package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.SynchList;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStorteMusicians;

/**
 * Created by OscarAlfonso on 3/23/2017.
 * This behaviour with request to the agent composer to perform a solo
 *
 */
public class LeaderRequestSoloToMyComposer extends OneShotBehaviour implements DataStorteMusicians{

    Agent agent;
    //Store the internal composer ID

    //Check if is the first time doing this action
    private int firstTimeHere = 0;
    private  int transitionParentBehaviour = 30;
    //Variables to calculate the time of intro
    private long introStartedAt;
    private long introDuration;
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
            RequestPlayBehaviour requestPlayBehaviour = new RequestPlayBehaviour();
            requestPlayBehaviour.setDataStore(getDataStore());
            requestSolo.registerFirstState(requestPlayBehaviour,REQUEST_PLAY);
            HandleAgreeBehaviour handleAgreeBehaviour = new HandleAgreeBehaviour();
            handleAgreeBehaviour.setDataStore(getDataStore());
            requestSolo.registerState(handleAgreeBehaviour,HANDLE_AGREE);

            requestHandleConfirm HandleConfirm = new requestHandleConfirm();
            HandleConfirm.setDataStore(getDataStore());
            requestSolo.registerState(HandleConfirm,HANDLE_CONFIRM);
            requestSolo.registerLastState(new OneShotBehaviour() {
                @Override
                public void action() {
                    System.out.println("Last State in requestSolo State machine");
                }
            },"LastState");

            //Register the transitions
            requestSolo.registerTransition(REQUEST_PLAY,HANDLE_AGREE,0);
            requestSolo.registerTransition(HANDLE_AGREE,HANDLE_AGREE,2);
            requestSolo.registerTransition(HANDLE_AGREE,HANDLE_CONFIRM,3);
            requestSolo.registerTransition(HANDLE_CONFIRM,HANDLE_CONFIRM,4);
            requestSolo.registerTransition(HANDLE_CONFIRM,"LastState",5);


            agent.addBehaviour(requestSolo);

        }
    }

    public int onEnd()
    {
        firstTimeHere++;
        if(transitionParentBehaviour == 30)
        {
            block(500);
        }
        return transitionParentBehaviour;
    }

    private class RequestPlayBehaviour extends OneShotBehaviour
    {
        int transition = 0;
        public RequestPlayBehaviour()
        {
            super(agent);
        }

        public void action()
        {
           requestMessage.setConversationId("Request-Solo-To-Composer");
           requestMessage.setReplyWith(agent.getLocalName()+System.currentTimeMillis());
            if(getDataStore().containsKey(INTERNAL_COMPOSER))
            {
                internalComposer = (AID) getDataStore().get(INTERNAL_COMPOSER);
                System.out.println("The internal composer "+internalComposer.getLocalName());
            }

            System.out.println("The internal Composer "+internalComposer);
            requestMessage.addReceiver(internalComposer);
            requestMessage.setContent(String.valueOf(firstTimeSolo));

            //Wait until the intro finished
            if (getDataStore().containsKey(INTRO_DURATION) && getDataStore().containsKey(INTRO_TIMESTAMP))
            {

                introDuration = (Long) getDataStore().get(INTRO_DURATION);
                introStartedAt = (Long) getDataStore().get(INTRO_TIMESTAMP);
                Long now = System.currentTimeMillis();
                long elapsedTime = introStartedAt - now;
                long timeLeft = introDuration - elapsedTime;
                System.out.println("time left "+timeLeft);
                //Wait until the intro has finish before request a solo
                agent.doWait(timeLeft);
            }

            agent.send(requestMessage);

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

    private class HandleAgreeBehaviour extends OneShotBehaviour
    {
        int transition = 2;
        int firstTimeHere = 0;
        private MessageTemplate mt1 = MessageTemplate.and(
                MessageTemplate.MatchConversationId("request-solo-to-composer-agree"),
                MessageTemplate.MatchPerformative(ACLMessage.AGREE)
        );
        private MessageTemplate mt2andmt1;
        public HandleAgreeBehaviour()
        {
            super(agent);
        }

        public void action()
        {
            if (firstTimeHere < 0)
            {
                mt2andmt1 = MessageTemplate.and(mt1,MessageTemplate.MatchInReplyTo(requestMessage.getReplyWith()));
            }

            replyAgree = agent.receive(mt2andmt1);
            if(replyAgree != null)
            {
                System.out.println("The composer agree");
                ACLMessage replyAgreeToComposer = replyAgree.createReply();
                replyAgreeToComposer.setConversationId("request-solo-to-composer-Confirm");
                replyAgreeToComposer.setPerformative(ACLMessage.CONFIRM);
                replyAgreeToComposer.setReplyWith(replyAgree.getSender().getLocalName()+System.currentTimeMillis());
                replyAgree.setReplyWith(replyAgreeToComposer.getReplyWith());
                agent.send(replyAgreeToComposer);
                transition = 3;
            }
            else {block();}

        }

        public int onEnd()
        {
            firstTimeHere++;
            if (transition == 2)
            {
                block(500);
            }
            return transition;
        }
    }

    private class requestHandleConfirm extends OneShotBehaviour
    {

        private int transiton= 4;
        private MessageTemplate mt1 = MessageTemplate.and(MessageTemplate.MatchConversationId("request-solo-to-composer-Inform"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        private MessageTemplate mt1Andmt2;
        public requestHandleConfirm()
        {
            super(agent);
        }

        public void action()
        {

            //mt1Andmt2 = MessageTemplate.and(mt1,MessageTemplate.MatchInReplyTo(replyAgree.getReplyWith()));
            //System.out.println("reply with "+replyAgree.getReplyWith());
            ACLMessage inform = agent.receive(mt1);
            if(inform != null)
            {
                System.out.println("The agent informed it will play the solo");
                //Send the state compose of the FSM inside the accompanimentPlaySection to the end state.
                transitionParentBehaviour = 12;
                //Stop the simple behaviour that is the parent of this fsm

            }else{block();}



        }

        public int onEnd()
        {
            if(transiton == 4)
            {
                block(500);
            }
            return transiton;
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
