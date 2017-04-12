package tools.ensemble.behaviours.musicianBehaviours;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.util.Play;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.interfaces.DataStorteMusicians;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Intro;

import java.util.Date;
import java.util.Vector;

/**
 * Created by OscarAlfonso on 2/2/2017.
 */
public class AccompanientPlaySections extends OneShotBehaviour implements DataStoreTimeManager,DataStorteMusicians, JMC {

    //Create a Finite state machine for handle the task that this state will perform.
    FSMBehaviour fsmPlayHead;
    //States
    private static final String STATE_CHECK_INTRO_DATA = "stateCheckIntroDAta";
    //handle the option to which transition it should take
    int stateCheckIntroData = 0;
    private static final String STATE_GET_INFO = "stateGetInfo";
    //handle the option to which transition it should take
    int stateGetInfo = 0;
    //check if is the first time in the GET_INFO STATE
    int getInfoFirstTime = 0;
    //this will create an instance of the protocol get Info initiator each time that we need it
    GetInfoInitiator getInfoInitiator;
    //Clear the protocol object get info and retry to search the intro info until its find it
    private static final String STATE_CLEAR_RETRY = "stateClearRetry";
    //handle the option to which transition it should take
    int stateClearRetry = 0;
    private static final String STATE_COMPOSE_SECTION = "stateComposeSection";
    //handle the option to which transition it should take
    int stateComposeSections = 0;
    //End State
    private static final String STATE_END = "stateEnd";
    //save my agent
    private Agent agent;
    //Musician Language
    private Codec codec;
    //Musician Ontology
    private Ontology musicianOntology;
    //Time Manager Ontology
    private Ontology timeHandlerOntology;
    //Check if is the first time in the action method
    private int firstTimeAction = 0;
    //Save the transition
    private int transition;
    //handle the step in the swicht method
    private int step = 0;
    //save my TimeManager
    private AID myTimeManager;
    // Duration of the intro
    private int duration = 0;
    //intro started at
    private Date introStartedAt = null;
    //time When this behaviour started to operate
    private long alive = System.currentTimeMillis();

    //Only for now will help us to use the composer conversation simulation
    Score theScore = new Score("Drunk walk demo",Musician.tempo);
    Phrase thePhrase = new Phrase("the phrase");
    Part thePart = new Part("SAX", SAXOPHONE);

    public AccompanientPlaySections(Agent a, Codec language, Ontology musicianOntology, Ontology TimeHandler)
    {
        super(a);
        this.agent = a;
        this.codec = language;
        this.musicianOntology = musicianOntology;
        this.timeHandlerOntology = TimeHandler;
    }

    public void action()
    {


        //We don't want to create a new State Machine each time that we visit this state, but only the very first time
        if(firstTimeAction < 1)
        {
            myTimeManager = (getDataStore().containsKey(INTERNAL_TIME_MANAGER))? (AID) getDataStore().get(INTERNAL_TIME_MANAGER) :null;
            fsmPlayHead = new FSMBehaviour(agent);
            //create instance of the behaviour CheckIntroData
            StateCheckForIntroData checkIntroData = new StateCheckForIntroData();
            checkIntroData.setDataStore(getDataStore());
            fsmPlayHead.registerFirstState(checkIntroData,STATE_CHECK_INTRO_DATA);
            //create instance of the behaviour Compose
            StateCompose stateCompose = new StateCompose();
            stateCompose.setDataStore(getDataStore());
            fsmPlayHead.registerState(stateCompose, STATE_COMPOSE_SECTION);
            //create instance of the behaviour get intro data
            StateGetInfoLater getInfo = new StateGetInfoLater();
            getInfo.setDataStore(getDataStore());
            fsmPlayHead.registerState(getInfo,STATE_GET_INFO);
            //create instance of the behaviour clear and retry
            StateClearRetry clearAndRetry = new StateClearRetry();
            clearAndRetry.setDataStore(getDataStore());
            fsmPlayHead.registerState(clearAndRetry,STATE_CLEAR_RETRY);
            StateEnd stateEnd = new StateEnd();
            stateEnd.setDataStore(getDataStore());
            fsmPlayHead.registerLastState(stateEnd,STATE_END);

            //Register Transitions
            fsmPlayHead.registerTransition(STATE_CHECK_INTRO_DATA,STATE_CHECK_INTRO_DATA,0);
            fsmPlayHead.registerTransition(STATE_CHECK_INTRO_DATA,STATE_GET_INFO,1);
            fsmPlayHead.registerTransition(STATE_CHECK_INTRO_DATA, STATE_COMPOSE_SECTION,2);
            fsmPlayHead.registerTransition(STATE_CHECK_INTRO_DATA,STATE_END,3);
            fsmPlayHead.registerTransition(STATE_GET_INFO,STATE_GET_INFO,4);
            fsmPlayHead.registerTransition(STATE_GET_INFO, STATE_COMPOSE_SECTION,5);
            fsmPlayHead.registerTransition(STATE_GET_INFO,STATE_CLEAR_RETRY,8);
            fsmPlayHead.registerTransition(STATE_CLEAR_RETRY,STATE_GET_INFO,9);
            fsmPlayHead.registerTransition(STATE_COMPOSE_SECTION, STATE_COMPOSE_SECTION,6);
            fsmPlayHead.registerTransition(STATE_COMPOSE_SECTION,STATE_END,7);

            agent.addBehaviour(fsmPlayHead);
            System.out.println("Create FSM Play Head");
        }

        switch (step)
        {
            case 0:
                transition = 40;
                break;
            case 1:
                transition = 20;
        }


    }

    public int onEnd()
    {
        firstTimeAction++;
        if (transition == 9)
        {
            agent.removeBehaviour(fsmPlayHead);
            fsmPlayHead = null;
        }
        if(transition == 32)
        {
            block(500);
        }
        return transition;
    }

    private class StateCheckForIntroData extends OneShotBehaviour
    {
        private int counter = 0;
        private int transition;
        private ACLMessage msg;
        public StateCheckForIntroData()
        {
            super(agent);
        }

        public void action()
        {
            if (counter < 1)
            {
                System.out.println(counter);
                ACLMessage message = createMessage();
                if(message != null)
                {
                    CheckInfoInitiator checkInfo = new CheckInfoInitiator(agent,message);
                    checkInfo.setDataStore(getDataStore());
                    agent.addBehaviour(checkInfo);

                }else{System.out.println("message "+message); stateCheckIntroData = 3;}

            }

            switch (stateCheckIntroData)
            {
                case 0:
                    transition = 0;
                    break;

                case 1:
                    transition = 1;
                    break;
                case 2:
                    transition = 2;
                    break;
                case 3:
                    transition = 3;
                    break;
            }
        }


        public int onEnd()
        {
            counter++;
            if(transition == 0)
            {
                block(500);
            }
            return transition;
        }

        private ACLMessage createMessage()
        {
            if(myTimeManager != null)
            {
                msg = new ACLMessage(ACLMessage.QUERY_IF);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
                msg.setOntology(timeHandlerOntology.getName());
                msg.setLanguage(codec.getName());
                msg.setReplyByDate(new Date(System.currentTimeMillis()+3000));
                msg.setConversationId("check-intro-info-interaction");
                msg.addReceiver(myTimeManager);
            }
            else {msg = null;}
            return msg;

        }
    }




    private class StateGetInfoLater extends OneShotBehaviour
    {

        private int transition;
        private ACLMessage msg;
        public StateGetInfoLater()
        {
            super(agent);
        }

        public void action()
        {
            if (getInfoFirstTime < 1)
            {
                //System.out.println("Im in behaviour "+getBehaviourName());
                ACLMessage message = createMessage();
                getInfoInitiator = new GetInfoInitiator(agent,message);
                getInfoInitiator.setDataStore(getDataStore());
                agent.addBehaviour(getInfoInitiator);
            }

            switch (stateGetInfo)
            {
                case 0:
                    transition = 4;
                    break;
                case 1:
                    transition = 5;
                    break;
                case 2:
                    transition = 8;
                    break;
            }
        }

        public int onEnd()
        {
            getInfoFirstTime++;
            if (transition == 4)
            {
                block(500);
            }
            return transition;
        }

        private ACLMessage createMessage()
        {
            if(myTimeManager != null)
            {

                msg = new ACLMessage(ACLMessage.QUERY_IF);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
                msg.setOntology(timeHandlerOntology.getName());
                msg.setLanguage(codec.getName());
                msg.setReplyByDate(new Date(System.currentTimeMillis()+3000));
                msg.setConversationId("check-intro-info-interaction");
                msg.addReceiver(myTimeManager);
            }
            else {msg = null;}
            return msg;

        }

    }

    private class StateClearRetry extends OneShotBehaviour
    {

        private int transition;

        public StateClearRetry()
        {
            super(agent);
        }

        public void action()
        {

            agent.removeBehaviour(getInfoInitiator);
            getInfoInitiator = null;
            getInfoFirstTime = 0;
            stateGetInfo = 0;
            transition = 9;
        }

        public int onEnd()
        {

            return transition;
        }


    }


    private class StateCompose extends OneShotBehaviour
    {

        private int counter = 0;
        private int transition;
        public StateCompose()
        {
            super(agent);
        }

        public void action()
        {
            if (counter < 1)
            {
                RequestAccompaniementFSM RAFSM = new RequestAccompaniementFSM();
                RAFSM.setDataStore(getDataStore());
                agent.addBehaviour(RAFSM);
            }




            switch (stateComposeSections)
            {
                case 0:
                    transition = 6;
                    break;
                case 1:
                    transition = 7;
                    break;
            }
        }

        public int onEnd()
        {
            counter++;
            if (transition == 6)
            {
                block();
            }
            return transition;
        }

        private long calculateTimeLeft()
        {
            long currentTimes = System.currentTimeMillis();
            System.out.println("current Time "+currentTimes);
            long transcurrentTime =  currentTimes - introStartedAt.getTime();
            System.out.println("transcurrent time :"+transcurrentTime);

            long timeLeft = duration - transcurrentTime;
            System.out.println("time left: "+timeLeft);
            return timeLeft;
        }
    }

    private class StateEnd extends OneShotBehaviour
    {

        public StateEnd()
        {
            super(agent);
        }

        public void action()
        {
            step = 1;
        }



    }

    private class CheckInfoInitiator extends AchieveREInitiator
    {
        private Agent agent;
        public CheckInfoInitiator(Agent a,ACLMessage msg)
        {
            super(a,msg);
            this.agent = a;
        }

        protected void handleAgree(ACLMessage agree) {
            System.out.println("The agent "+agree.getSender().getName() +" agree");
        }

        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("The agent "+refuse.getSender().getName() +" refuse");

            stateCheckIntroData = 1;

        }

        protected void handleNotUnderstood(ACLMessage notUnderstood) {
            System.out.println("The agent "+notUnderstood.getSender().getName() +"not Understood");
            stateCheckIntroData = 3;
        }

        protected void handleInform(ACLMessage inform) {
            System.out.println("The agent "+inform.getSender().getName() +" inform");
            try
            {
                ContentElement content = agent.getContentManager().extractContent(inform);
                Concept concept = ((Action)content).getAction();
                if(concept instanceof Intro)
                {
                    if(((Intro) concept).getIntroLenght() != 0 && ((Intro) concept).getIntroStartedAt().getTime() != 0)
                    {
                        duration = (int) ((Intro) concept).getIntroLenght();
                        introStartedAt = ((Intro) concept).getIntroStartedAt();
                    }

                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            stateCheckIntroData = 2;
        }

        protected void handleFailure(ACLMessage fail) {
            System.out.println(myAgent.getLocalName()+" : "+" Something went wrong");
            stateCheckIntroData = 3;
        }

        protected void handleAllResponses(Vector responses, Vector acceptances)
        {
            System.out.println(responses.size());
            if (responses.size() < 1) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing responses");
                stateCheckIntroData = 3;
            }
        }




    }

    //We will instanciate this class in the state get Info
    //This will be in case that our own time manager didn't have the intro data

    private class GetInfoInitiator extends AchieveREInitiator
    {
        private Agent agent;
        public GetInfoInitiator(Agent a,ACLMessage msg)
        {
            super(a,msg);
            this.agent = a;
            //System.out.println("Get Info initiator started");
        }

        protected void handleAgree(ACLMessage agree) {
            //System.out.println("The agent "+agree.getSender().getName() +" agree on get info initiator");
        }

        protected void handleRefuse(ACLMessage refuse) {
            //System.out.println("The agent "+refuse.getSender().getName() +" refuse on get info initiator");
            stateGetInfo = 2;
        }

        protected void handleNotUnderstood(ACLMessage notUnderstood) {
            //System.out.println("The agent "+notUnderstood.getSender().getName() +"not Understood on get info initiator");
            stateGetInfo = 2;
        }

        protected void handleInform(ACLMessage inform) {
            // System.out.println("The agent "+inform.getSender().getName() +" inform on get info initiator");
            try
            {
                ContentElement content = agent.getContentManager().extractContent(inform);
                Concept concept = ((Action)content).getAction();
                if(concept instanceof Intro)
                {
                    if(((Intro) concept).getIntroLenght() != 0 && ((Intro) concept).getIntroStartedAt().getTime() != 0)
                    {
                        duration = (int) ((Intro) concept).getIntroLenght();
                        introStartedAt = ((Intro) concept).getIntroStartedAt();
                    }

                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            //System.out.println("Duration :"+duration);
            //System.out.println("intro started at :"+introStartedAt);
            stateGetInfo = 1;
        }

        protected void handleFailure(ACLMessage fail) {
            //System.out.println(myAgent.getLocalName()+" : "+" Something went wrong on get info initiator");
            stateGetInfo = 2;
        }

        protected void handleAllResponses(Vector responses, Vector acceptances)
        {
            //System.out.println(responses.size());
            if (responses.size() < 1) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing responses");
                stateGetInfo = 2;
            }
        }




    }

    private class RequestAccompaniementFSM extends SimpleBehaviour
    {

        private boolean goOut = false;
        private ACLMessage requestMessage = new ACLMessage(ACLMessage.REQUEST);
        private ACLMessage replyAgree = new ACLMessage(ACLMessage.CONFIRM);

        private AID internalComposer = null;
        private int c = 0;

        //States
        private static final String REQUEST_PLAY = "requestPlay";
        private static final String HANDLE_AGREE = "handleAgree";
        private static final String HANDLE_CONFIRM = "handleConfirm";


        public RequestAccompaniementFSM()
        {
            super(agent);
        }


        public void action()
        {
            if (c < 1)
            {

                if(getDataStore().containsKey(INTERNAL_COMPOSER))
                {
                    internalComposer = (AID) getDataStore().get(INTERNAL_COMPOSER);
                }
                FSMBehaviour requestAccompaniment = new FSMBehaviour(agent);

                //Register requestPlayBehaviour
                RequestPlayBehaviour RPB = new RequestPlayBehaviour();
                RPB.setDataStore(getDataStore());
                requestAccompaniment.registerFirstState(RPB,REQUEST_PLAY);

                handleAgreeBehaviour agreeBehaviour = new handleAgreeBehaviour();
                agreeBehaviour.setDataStore(getDataStore());
                requestAccompaniment.registerState(agreeBehaviour,HANDLE_AGREE);

                requestHandleConfirm handleConfirmBehavior = new requestHandleConfirm();
                handleConfirmBehavior.setDataStore(getDataStore());
                requestAccompaniment.registerLastState(handleConfirmBehavior,HANDLE_CONFIRM);

                //Transition

                requestAccompaniment.registerTransition(REQUEST_PLAY,HANDLE_AGREE,0);
                requestAccompaniment.registerTransition(HANDLE_AGREE,HANDLE_AGREE,2);
                requestAccompaniment.registerTransition(HANDLE_AGREE,HANDLE_CONFIRM,3);

                agent.addBehaviour(requestAccompaniment);

            }




        }

        public boolean done() {
            c++;
            if(goOut)
            {
                return true;
            }
            block(500);
            return false;
        }

        private class RequestPlayBehaviour extends OneShotBehaviour
        {
            int transition = 0;
            public RequestPlayBehaviour()
            {
                super(agent);

            }

            private long calculateTimeLeft()
            {
                long currentTimes = System.currentTimeMillis();
                System.out.println("current Time "+currentTimes);
                long transcurrentTime =  currentTimes - introStartedAt.getTime();
                System.out.println("transcurrent time :"+transcurrentTime);

                long timeLeft = duration - transcurrentTime;
                System.out.println("time left: "+timeLeft);
                return timeLeft;
            }


            public void action()
            {
                requestMessage.setConversationId("request-accompaniment-conversation-REQUEST");
                requestMessage.setReplyWith(internalComposer.getLocalName()+System.currentTimeMillis());
                requestMessage.addReceiver(internalComposer);
                requestMessage.setContent(String.valueOf(calculateTimeLeft()));


                agent.send(requestMessage);

            }

            public int onEnd()
            {
                return transition;
            }
        }

        private class handleAgreeBehaviour extends OneShotBehaviour
        {
            private int transition = 2;
            private int firstTimeHere=0;
            private MessageTemplate mt1 = MessageTemplate.and(
                    MessageTemplate.MatchConversationId("request-accompaniment-conversation-AGREE"),
                    MessageTemplate.MatchPerformative(ACLMessage.AGREE)
            );
            private MessageTemplate mt2andmt1;
            public handleAgreeBehaviour()
            {
                super(agent);
            }

            public void action()
            {
                if(firstTimeHere < 1)
                {
                    mt2andmt1 = MessageTemplate.and(mt1,MessageTemplate.MatchInReplyTo(requestMessage.getReplyWith()));
                }
                replyAgree = agent.receive(mt2andmt1);
                if(replyAgree != null)
                {
                    ACLMessage replyAgreeToComposer = replyAgree.createReply();
                    replyAgreeToComposer.setConversationId("request-accompaniment-conversation-CONFIRM");
                    replyAgreeToComposer.setPerformative(ACLMessage.CONFIRM);
                    replyAgreeToComposer.setReplyWith(replyAgree.getSender().getLocalName()+System.currentTimeMillis());
                    replyAgree.setReplyWith(replyAgreeToComposer.getReplyWith());
                    agent.send(replyAgreeToComposer);
                    transition = 3;
                }else{block();}
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

            private MessageTemplate mt1 = MessageTemplate.and(MessageTemplate.MatchConversationId("request-accompaniment-conversation-INFORM"),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM)
            );
            private MessageTemplate mt1Andmt2;
            public requestHandleConfirm()
            {
                super(agent);
            }

            public void action()
            {
                mt1Andmt2 = MessageTemplate.and(mt1,MessageTemplate.MatchInReplyTo(replyAgree.getReplyWith()));
                ACLMessage inform = agent.receive(mt1Andmt2);
                if(inform != null)
                {
                    System.out.println("The agent informed it will play");
                    //Send the state compose of the FSM inside the accompanimentPlaySection to the end state.
                    stateComposeSections = 1;
                    //Stop the simple behaviour that is the parent of this fsm
                    goOut = true;
                }else{block();}



            }


        }
        private class temporalBehaviour extends OneShotBehaviour
        {
            public temporalBehaviour()
            {
                super(agent);
            }

            public void action(){
                System.out.println("I'm in behaviour "+getBehaviourName());
            }
        }

    }



    private class SimulateComposeConversation extends OneShotBehaviour
    {
        private int measures;
        public SimulateComposeConversation (int measures)
        {this.measures = measures;}
        public void action()
        {
            System.out.println("simulate composer");
            int pitch = C3; // variable to store the calculated pitch (initialized with a start pitch value)
            int numberOfNotes = measures * Musician.timeSignatureNumerator;
            System.out.println("numberOfNotes: "+numberOfNotes);
            double pitches[] = {E5,G5,C6,F5};
            for (int i = 0; i < numberOfNotes; i++)
            {
                int  x = (int)(Math.random()*4);
                thePhrase.add(new Note(pitches[x],QUARTER_NOTE));
            }
            thePart.add(thePhrase);
            theScore.addPart(thePart);
        }
    }

    private class playBack extends OneShotBehaviour
    {
        public void action()
        {
            System.out.println("play back");

            //Play.midi(theScore,false,false,1,1);
            Play.midi(theScore,false,false,3,0);

            //stateComposeSections = 1;

        }
    }




}
