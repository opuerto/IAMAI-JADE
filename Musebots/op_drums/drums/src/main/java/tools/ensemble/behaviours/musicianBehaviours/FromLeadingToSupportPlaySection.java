package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.ensemble.interfaces.DataStorteMusicians;

/**
 * Created by OscarAlfonso on 3/28/2017.
 */
public class FromLeadingToSupportPlaySection extends OneShotBehaviour implements DataStorteMusicians {

    private int transitionParentBehaviour = 38;
    private int firstTimeHere = 0;
    private Agent agent;
    private  AID internalComposer;
    private FSMBehaviour requestAccompaniment;

    //Messages
    private ACLMessage requestMessage = new ACLMessage(ACLMessage.CFP);
    private ACLMessage replyAgree = new ACLMessage(ACLMessage.CONFIRM);

    //States
    private static final String REQUEST_PLAY = "requestPlay";
    private static final String HANDLE_AGREE = "handleAgree";
    private static final String HANDLE_CONFIRM = "handleConfirm";


    public FromLeadingToSupportPlaySection(Agent a)
    {

        super(a);
        agent = a;
    }

    public void onStart()
    {
        System.out.println("Im going to request the sections");
        transitionParentBehaviour = 38;
        requestAccompaniment = new FSMBehaviour(agent);
        RequestInternalStatePlayBehaviour requestPlayBehaviour = new RequestInternalStatePlayBehaviour();
        requestPlayBehaviour.setDataStore(getDataStore());
        requestAccompaniment.registerFirstState(requestPlayBehaviour,REQUEST_PLAY);
        HandleAgreeInternalStateBehaviour handleAgreeBehaviour = new HandleAgreeInternalStateBehaviour();
        handleAgreeBehaviour.setDataStore(getDataStore());
        requestAccompaniment.registerState(handleAgreeBehaviour,HANDLE_AGREE);

        requestInternalStateHandleConfirm HandleConfirm = new requestInternalStateHandleConfirm();
        HandleConfirm.setDataStore(getDataStore());
        requestAccompaniment.registerState(HandleConfirm,HANDLE_CONFIRM);
        requestAccompaniment.registerLastState(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("Last State ");
            }
        },"LastState");

        //Register the transitions
        requestAccompaniment.registerTransition(REQUEST_PLAY,HANDLE_AGREE,0);
        requestAccompaniment.registerTransition(HANDLE_AGREE,HANDLE_AGREE,2);
        requestAccompaniment.registerTransition(HANDLE_AGREE,HANDLE_CONFIRM,3);
        requestAccompaniment.registerTransition(HANDLE_CONFIRM,HANDLE_CONFIRM,4);
        requestAccompaniment.registerTransition(HANDLE_CONFIRM,"LastState",5);


        agent.addBehaviour(requestAccompaniment);
    }

    public void action()
    {

    }

    public int onEnd()
    {
        if(transitionParentBehaviour == 38)
        {
            block(500);
        }

        //firstTimeHere++;
        if (transitionParentBehaviour == 80)
        {
            myAgent.removeBehaviour(requestAccompaniment);
        }
        return transitionParentBehaviour;
    }

    private class RequestInternalStatePlayBehaviour extends OneShotBehaviour
    {
        int transition = 0;
        public RequestInternalStatePlayBehaviour()
        {
            super(agent);
        }

        public void action()
        {
            requestMessage.setConversationId("From-Leading-To-support-accompaniment-Request");
            requestMessage.setReplyWith(agent.getLocalName()+System.currentTimeMillis());
            if(getDataStore().containsKey(INTERNAL_COMPOSER))
            {
                internalComposer = (AID) getDataStore().get(INTERNAL_COMPOSER);
                System.out.println("The internal composer "+internalComposer.getLocalName());
            }

            System.out.println("The internal Composer "+internalComposer);
            requestMessage.addReceiver(internalComposer);
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

    private class HandleAgreeInternalStateBehaviour extends OneShotBehaviour
    {
        int transition = 2;
        int firstTimeHere = 0;
        private MessageTemplate mt1 = MessageTemplate.and(
                MessageTemplate.MatchConversationId("request-accompaniment-From-Leader-To-Support-AGREE"),
                MessageTemplate.MatchPerformative(ACLMessage.AGREE)
        );
        private MessageTemplate mt2andmt1;
        public HandleAgreeInternalStateBehaviour()
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
                System.out.println("The composer agree from leader to support ");
                ACLMessage replyAgreeToComposer = replyAgree.createReply();
                replyAgreeToComposer.setConversationId("From-Leader-To-Support-Confirm");
                replyAgreeToComposer.setPerformative(ACLMessage.CONFIRM);
                replyAgreeToComposer.setReplyWith(replyAgree.getSender().getLocalName()+System.currentTimeMillis());
                replyAgree.setReplyWith(replyAgreeToComposer.getReplyWith());
                agent.send(replyAgreeToComposer);
                transition = 3;
            }
            else
            {
                block();
            }

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

    private class requestInternalStateHandleConfirm extends OneShotBehaviour
    {

        private int transiton= 4;
        private MessageTemplate mt1 = MessageTemplate.and(MessageTemplate.MatchConversationId("From-leader-to-Support-Inform"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        private MessageTemplate mt1Andmt2;
        public requestInternalStateHandleConfirm()
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
                 transitionParentBehaviour = 80;
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




}
