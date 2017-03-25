package tools.ensemble.behaviours.musicianBehaviours;

import jade.content.AgentAction;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;
import jade.tools.sniffer.Message;
import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.util.Play;
import sun.rmi.server.Activation$ActivationSystemImpl_Stub;
import sun.swing.plaf.synth.SynthIcon;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.interfaces.DataStorteMusicians;
import tools.ensemble.ontologies.composer.vocabulary.concepts.IntroConcepts;
import tools.ensemble.ontologies.musicians.vocabulary.actions.PlayIntroAction;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Intro;

import java.security.acl.Acl;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by OscarAlfonso on 1/31/2017.
 * The ide in this behaviour is to have a finite state machine with 5 states
 * the start State will wait until the leader open a negotiation with this agent then it will go to the next state
 * which will simulate the composition and the playback of the intro. this state will set a the startedIntroAt variable
 *the next state will begin an interaction with the Time Manager and it will pass along the duration of the intro and the time when
 * this has started to play. this information will be used later in the next state of the musician accompanient
 * If the agent reject play the intro it will go to the rejected Intro state and it will remove the interaction protocol from its behaviour
 *
 *
 */
public class AccompanientPlayIntro extends OneShotBehaviour implements DataStoreTimeManager,DataStorteMusicians, JMC {

    //Finite state machine that handle the tasks in this state Accompanient Play Into
    FSMBehaviour fsmBehaviour;
    //List of the states in the internal finite state machine

    private final static String STATE_INTRO_NEGOTIATION = "stateIntroNegotiation";
    //this variable will handle the swicth inside the start state
    private int stateStart = 0;
    private final static String STATE_PASS_INTRO_DATA = "statePassIntroData";
    private int statePassInfo = 0;
    private final static String STATE_EXTEND_INTRO = "stateExtendIntro";
    private int stateExtendIntro = 0;
    private final static String STATE_REJECTED_INTRO = "stateRejectedIntro";
    private boolean stateRejectedIntro;
    private final static String STATE_END = "stateEnd";
    //Behaviours
    IntroNegotiationResponder introNegotiationResponder;
    IntroInteractionTimeManager passInfo;

    //we save myagent
    Agent agent;
    //we kept here the value for the transition to the next state
    int transition;
    //We keept the message template
    MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
            MessageTemplate.MatchPerformative(ACLMessage.CFP)
    );
    //We kept the language
    Codec codec;
    //We kept the musician ontology
    Ontology musicianOntology;
    //We kept the timehandler ontology
    Ontology timeHandlerOntology;
    //We kept the Composer ontology
    Ontology composerOntology;
    //we kept the counter that will check if this is the first time in the behaviour
    int again = 0;
    //we kept the handler of the switch in the action method
    int step = 0;
    //check if we got a confirm from the time manager
    private boolean timeManagerGotInfo = false;
    //check if the music is playing
    private boolean agentPlaying = false;
    //kept the moment when the intro started to play
    private Date startedIntroAt = null;
    //kept the duration of the intro
    private float duration;
    //length of the intro
    private int lenght;


    //Only for now will help us to use the composer conversation simulation
    Score theScore = new Score("The Score");
    Phrase thePhrase = new Phrase("the phrase");
    Part thePart = new Part("the part");
    Note theNote;



    public AccompanientPlayIntro (Agent a,Codec codec,Ontology onto,Ontology timeHandler,Ontology composerOntology)
    {
        super(a);
        this.agent = a;
        this.codec = codec;
        this.musicianOntology = onto;
        this.timeHandlerOntology = timeHandler;
        this.composerOntology = composerOntology;
    }

    public void action()
    {
        if(again < 1)
        {
            System.out.println("Play intro fsm");
            fsmBehaviour = new FSMBehaviour(agent);
            //register the states
            StateStart stateStart = new StateStart();
            stateStart.setDataStore(getDataStore());
            fsmBehaviour.registerFirstState(stateStart,STATE_INTRO_NEGOTIATION);
            fsmBehaviour.registerState(new StateRejectedIntro(),STATE_REJECTED_INTRO);
            StatePassInfo statePassInfo = new StatePassInfo();
            statePassInfo.setDataStore(getDataStore());
            fsmBehaviour.registerState(statePassInfo,STATE_PASS_INTRO_DATA);
            ExtendIntro extend = new ExtendIntro();
            extend.setDataStore(getDataStore());
            fsmBehaviour.registerState(extend,STATE_EXTEND_INTRO);
            StateEnd end = new StateEnd();
            end.setDataStore(getDataStore());
            fsmBehaviour.registerLastState(end,STATE_END);
            fsmBehaviour.registerTransition(STATE_INTRO_NEGOTIATION,STATE_INTRO_NEGOTIATION,0);
            fsmBehaviour.registerTransition(STATE_INTRO_NEGOTIATION,STATE_REJECTED_INTRO,7);
            fsmBehaviour.registerDefaultTransition(STATE_REJECTED_INTRO,STATE_END);
            fsmBehaviour.registerTransition(STATE_INTRO_NEGOTIATION,STATE_PASS_INTRO_DATA,1);
            fsmBehaviour.registerTransition(STATE_PASS_INTRO_DATA,STATE_PASS_INTRO_DATA,2);
            fsmBehaviour.registerTransition(STATE_PASS_INTRO_DATA,STATE_END,3);
            fsmBehaviour.registerTransition(STATE_PASS_INTRO_DATA,STATE_EXTEND_INTRO,4);
            fsmBehaviour.registerTransition(STATE_EXTEND_INTRO,STATE_EXTEND_INTRO,5);
            fsmBehaviour.registerTransition(STATE_EXTEND_INTRO,STATE_PASS_INTRO_DATA,6);


            agent.addBehaviour(fsmBehaviour);

        }

        switch (step)
        {
            case 0:
                transition = 8;
                break;
            case 1:

                transition = 7;
                break;
        }
    }

    public int onEnd()
    {
        again++;
        if(transition == 7)
        {
            agent.removeBehaviour(fsmBehaviour);
            fsmBehaviour = null;
        }
        return transition;
    }



    private class StateStart extends OneShotBehaviour
    {
        public StateStart()
        {
            super(agent);
        }
        int exit;
        int counter = 0;

        public void action ()
        {

            if(counter < 1)
            {
                introNegotiationResponder = new IntroNegotiationResponder(myAgent,mt);
                introNegotiationResponder.setDataStore(getDataStore());
                myAgent.addBehaviour(introNegotiationResponder);
                System.out.println("start State");
            }
            switch (stateStart)
            {
                case 0:
                    exit = 0;
                    break;
                case 1:

                    exit = 1;

                    break;
                case 2 :
                    stateRejectedIntro = true;
                    exit = 7;
                    break;
            }

        }

        public int onEnd()
        {
            counter++;
            return  exit;
        }
    }

    private class StateRejectedIntro extends  OneShotBehaviour
    {
        public StateRejectedIntro()
        {
            super(agent);
        }
        int exit;

        public void action()
        {
            agent.removeBehaviour(introNegotiationResponder);
            introNegotiationResponder = null;
        }

        public int onEnd()
        {
            return exit;
        }
    }
    private class StatePassInfo extends OneShotBehaviour
    {

        public StatePassInfo()
        {
            super(agent);
        }
        int exit;
        int counter=0;
        public void action()
        {
            if(counter < 1)
            {
                passInfo = new IntroInteractionTimeManager(duration);
                passInfo.setDataStore(getDataStore());
                myAgent.addBehaviour(passInfo);
            }
            switch (statePassInfo)
            {
                case 0:
                    exit = 2;
                    break;
                case 1:
                    exit = 3;
                    break;
                case 2:
                    exit = 4;
            }

        }
        public int onEnd()
        {
            counter++;
            return exit;
        }
    }

    private class ExtendIntro extends OneShotBehaviour
    {
        public ExtendIntro()
        {
            super(agent);
        }
        int exit;
        //int counter=0;
        public void action()
        {
            System.out.println("extend intro");

            exit = 6;

        }

        public int onEnd()
        {

            return exit;
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

            if (!stateRejectedIntro)
            {
                agent.removeBehaviour(passInfo);
                passInfo = null;
            }

            System.out.println("end behaviour");
            step = 1;

        }
    }







    private class IntroNegotiationResponder extends ContractNetResponder
    {

        private boolean now;
        private Agent agentHandleIntro;
        private PlayIntroAction playIntroActionObject;
        private AID agentComposer;
        public IntroNegotiationResponder(Agent agent, MessageTemplate mt)

        {
            super(agent,mt);
            this.agentHandleIntro = agent;
        }

        private ACLMessage messageToComposer()
        {
            agentComposer = (AID) getDataStore().get(INTERNAL_COMPOSER);
            IntroConcepts introData = new IntroConcepts();
            introData.setIntroTempo(Musician.tempo);
            introData.setIntroNumerator(Musician.timeSignatureNumerator);
            introData.setIntroDenominator(Musician.timeSignatureDenominator);
            introData.setIntroLength(lenght);
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);
            msg.setLanguage(codec.getName());
            msg.setOntology(composerOntology.getName());
            msg.setConversationId("introInteraction-musician-composer-CFP");
            msg.setReplyWith(agentComposer.getLocalName().toString()+System.currentTimeMillis());
            try
            {
                //fill the content using the Ontology concept
                myAgent.getContentManager().fillContent(msg,new Action(agentComposer,introData));
            }catch (Exception ex) { ex.printStackTrace(); }
            msg.addReceiver(agentComposer);

            return msg;
        }

        protected ACLMessage handleCfp (ACLMessage cfp) throws NotUnderstoodException, RefuseException
        {

            try {
                ContentElement content = agentHandleIntro.getContentManager().extractContent(cfp);
                AgentAction action = (AgentAction) ((Action)content).getAction();
                if(action instanceof PlayIntroAction)
                {
                    lenght = ((PlayIntroAction) action).getLenght();
                    now = ((PlayIntroAction) action).getNow();
                    duration = ((PlayIntroAction) action).getDuration();

                }
            } catch (Codec.CodecException e) {
                e.printStackTrace();
            } catch (OntologyException e) {
                e.printStackTrace();
            }
            System.out.println("Agent "+agentHandleIntro.getLocalName()+": CFP received from "+cfp.getSender().getName()+". Action is "+cfp.getContent());

            playIntroActionObject = new PlayIntroAction();
            //Calculate the duration of the intro based on the song structure and the lenght get from the leader
            double calculateDuration = ((((double) Musician.timeSignatureNumerator*(double) lenght)/(double) Musician.tempo)*60*1000);

            duration = (float) calculateDuration;
            playIntroActionObject.setLenght(lenght);
            playIntroActionObject.setDuration((float)duration);
            playIntroActionObject.setNow(true);
            ACLMessage propose = cfp.createReply();
            propose.setLanguage(codec.getName());
            propose.setOntology(musicianOntology.getName());
            try {
                agentHandleIntro.getContentManager().fillContent(propose,new Action(cfp.getSender(),playIntroActionObject));
            } catch (Codec.CodecException e) {
                e.printStackTrace();
            } catch (OntologyException e) {
                e.printStackTrace();
            }
            propose.setPerformative(ACLMessage.PROPOSE);
            return propose;
        }

        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException
        {
            System.out.println("Agent "+agentHandleIntro.getLocalName()+": Proposal accepted");

            ACLMessage informMusicianLater = accept.createReply();
            informMusicianLater.setPerformative(ACLMessage.INFORM);
            ACLMessage messageComposer = messageToComposer();
            RequestIntroFSM requestIntroFSM = new RequestIntroFSM(informMusicianLater,messageComposer);
            requestIntroFSM.setDataStore(getDataStore());
            myAgent.addBehaviour(requestIntroFSM);
            System.out.println("Agent "+agentHandleIntro.getLocalName()+": Action successfully performed");
            ACLMessage inform = accept.createReply();
            //This is a hack we set PROPAGATE then we are able to send the INFORM message later.
            inform.setPerformative(ACLMessage.PROPAGATE);
            return inform;


        }

        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            System.out.println("Agent "+agentHandleIntro.getLocalName()+": Proposal rejected");
            stateStart = 2;

        }

    }

    private class IntroInteractionTimeManager extends Behaviour
    {
        int step = 0;
        private float length;
        private AID receiver = null;
        private MessageTemplate getProposalTemplate;
        private boolean exit;
        private long wakeupTime;
        public IntroInteractionTimeManager(float length)
        {

            this.length = length;
            System.out.println("interaction whit time manager started ");

        }

        public void onStart() {

            long elapsedTime =  startedIntroAt.getTime() - System.currentTimeMillis();
            long leftTime = (long)duration - elapsedTime;
            wakeupTime = System.currentTimeMillis() + leftTime;
        }

        public void action()
        {

            long dt = wakeupTime - System.currentTimeMillis();

            if (dt >= 0)
            {


                switch (step) {
                    case 0:
                        myAgent.removeBehaviour(introNegotiationResponder);
                        introNegotiationResponder = null;

                        if (getDataStore().containsKey(INTERNAL_TIME_MANAGER)) {
                            receiver = (AID) getDataStore().get(INTERNAL_TIME_MANAGER);

                        }

                        if (receiver != null) {

                            ACLMessage sendInfoMSG = new ACLMessage(ACLMessage.INFORM);
                            //set the receiver of the message
                            sendInfoMSG.setLanguage(codec.getName());
                            sendInfoMSG.setOntology(timeHandlerOntology.getName());
                            //Set the data on the Intro Object
                            Intro intro = new Intro();
                            intro.setIntroLenght(length);
                            intro.setIntroStartedAt(startedIntroAt);

                            try {
                                myAgent.getContentManager().fillContent(sendInfoMSG, new Action(receiver, intro));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            sendInfoMSG.addReceiver(receiver);
                            sendInfoMSG.setConversationId("intro-interaction-ITM-1");
                            sendInfoMSG.setReplyWith("IntroInfo" + System.currentTimeMillis());
                            sendInfoMSG.setReplyByDate(new Date(System.currentTimeMillis() + dt));
                            myAgent.send(sendInfoMSG);
                            //prepare the template to get the confirmation

                            getProposalTemplate = MessageTemplate.and(
                                    MessageTemplate.MatchConversationId("intro-interaction-ITM-1"),
                                    MessageTemplate.MatchInReplyTo(sendInfoMSG.getReplyWith())
                            );
                            step = 1;
                        }
                        break;
                    case 1:

                        ACLMessage reply = myAgent.receive(getProposalTemplate);
                        if (reply != null) {
                            if (reply.getPerformative() == ACLMessage.CONFIRM) {
                                //Let the contractNet that we passed the information.
                                timeManagerGotInfo = true;
                                //finish this behaviour.


                                exit = true;

                            }
                        } else {

                            block();

                        }

                        break;

                }
            }
            else
            {

                statePassInfo = 2;
            }



        }

        public boolean done()
        {
            if (exit)
            {
                statePassInfo = 1;
                return true;
            }
            block(500);
            return false;
        }
    }


    private class RequestIntroFSM extends SimpleBehaviour
    {
        private boolean goOut = false;
        private ACLMessage informMusician;
        private ACLMessage message;
        private ACLMessage replyPropose = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        private ACLMessage replyAgree = new ACLMessage(ACLMessage.CONFIRM);
        private ACLMessage getMessageInform = new ACLMessage(ACLMessage.INFORM);

        //States
        private static final String CALL_FOR_PROPOSAL = "callForProposal";
        private static final String ACCEPT_PROPOSAL = "acceptProposal";
        private static final String CONFIRM = "confirm";
        private static final String  HANDLE_INFORM = "handleInform";
        private static final String  END_CONVERSATION_WITH_COMPOSER = "endConversationWithComposer";
        private int count = 0;


        public RequestIntroFSM(ACLMessage informMusicianLeader,ACLMessage message)
        {
            super(agent);
            this.informMusician = informMusicianLeader;
            this.message = message;
        }

        public void action()
        {
            if(count < 1) {
                System.out.println("Run the simple behaviour " + count);
                FSMBehaviour RIFSM = new FSMBehaviour(myAgent);
                //Register the estates
                CFP cfpB = new CFP();
                cfpB.setDataStore(RIFSM.getDataStore());
                RIFSM.registerFirstState(cfpB, CALL_FOR_PROPOSAL);

                AcceptProposalBehaviour acceptProposalBehaviour = new AcceptProposalBehaviour();
                acceptProposalBehaviour.setDataStore(RIFSM.getDataStore());
                RIFSM.registerState(acceptProposalBehaviour, ACCEPT_PROPOSAL);

                ConfirmBehaviour confirmBehaviour = new ConfirmBehaviour();
                confirmBehaviour.setDataStore(RIFSM.getDataStore());
                RIFSM.registerState(confirmBehaviour, CONFIRM);

                HandleInformBehaviour handleInformBehaviour = new HandleInformBehaviour();
                handleInformBehaviour.setDataStore(RIFSM.getDataStore());
                RIFSM.registerState(handleInformBehaviour, HANDLE_INFORM);

                RIFSM.registerLastState(new EndConversationWithComposer(), END_CONVERSATION_WITH_COMPOSER);

                //register transitions
                RIFSM.registerTransition(CALL_FOR_PROPOSAL, CALL_FOR_PROPOSAL, 0);
                RIFSM.registerTransition(CALL_FOR_PROPOSAL, ACCEPT_PROPOSAL, 1);
                RIFSM.registerTransition(ACCEPT_PROPOSAL, ACCEPT_PROPOSAL, 2);
                RIFSM.registerTransition(ACCEPT_PROPOSAL, CONFIRM, 3);
                RIFSM.registerTransition(CONFIRM, CONFIRM, 4);
                RIFSM.registerTransition(CONFIRM, HANDLE_INFORM, 5);
                RIFSM.registerTransition(HANDLE_INFORM, HANDLE_INFORM, 6);
                RIFSM.registerTransition(HANDLE_INFORM, END_CONVERSATION_WITH_COMPOSER, 7);
                RIFSM.registerTransition(CALL_FOR_PROPOSAL, CONFIRM, 10);

                agent.addBehaviour(RIFSM);


            }

        }

        public boolean done() {

            count++;
            if (goOut)
            {
                return true;
            }

            return true;
        }



        private class CFP extends OneShotBehaviour
        {


            public CFP ()
            {
                super(agent);

            }
            public void action()
            {
                //Just send call for proposal message
                agent.send(message);
            }
            public int onEnd()
            {

                return 1;
            }

        }

        private class AcceptProposalBehaviour extends OneShotBehaviour
        {

            private int transition = 2 ;
            private int firstTime = 0;
            private MessageTemplate mt1;
            private MessageTemplate mt1andmt2;
            public AcceptProposalBehaviour()
            {
                super(agent);

            }

            public void action()
            {
                if(firstTime < 1)
                {
                    mt1 =  MessageTemplate.and(
                            MessageTemplate.MatchConversationId("introInteraction-musician-composer-PROPOSE"),
                            MessageTemplate.MatchInReplyTo(message.getReplyWith()));
                    mt1andmt2 = MessageTemplate.and(mt1,
                            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
                }

                replyPropose = agent.receive(mt1andmt2);
                if (replyPropose != null)
                {
                    System.out.println("Agent "+replyPropose.getSender().getName()+" proposed ");
                     ACLMessage replayProposeToComposer = replyPropose.createReply();
                    replayProposeToComposer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    replayProposeToComposer.setReplyWith(replyPropose.getSender().getLocalName()+System.currentTimeMillis());
                    replayProposeToComposer.setConversationId("introInteraction-musician-composer-ACCEPTPROPOSAL");
                    agent.send(replayProposeToComposer);
                    transition = 3;

                }else{block();}

            }

            public int onEnd()
           {
                firstTime++;
               if (transition == 2)
               {
                   block(500);
               }
                return transition;
            }
        }

        private class ConfirmBehaviour extends OneShotBehaviour
        {

           private int confirmTransition = 4;
           private int firstTimeHere =0;
            private MessageTemplate mt1;
            private MessageTemplate mt1andmt2;
            public ConfirmBehaviour()
            {
                super(agent);
            }
            public void action()
            {
                if(firstTimeHere < 0)
                {
                    mt1 =  MessageTemplate.and(
                            MessageTemplate.MatchConversationId("introInteraction-musician-composer-AGREE"),
                            MessageTemplate.MatchInReplyTo(replyPropose.getReplyWith()));
                    mt1andmt2 = MessageTemplate.and(mt1,
                            MessageTemplate.MatchPerformative(ACLMessage.AGREE));
                }
                replyAgree = agent.receive(mt1andmt2);
                if(replyAgree != null)
                {
                    System.out.println("Agent "+replyAgree.getSender().getName()+" Agreed ");
                    ACLMessage replayAgreeToComposer = replyAgree.createReply();
                    replayAgreeToComposer.setPerformative(ACLMessage.CONFIRM);
                    replayAgreeToComposer.setConversationId("introInteraction-musician-composer-CONFIRM");
                    replayAgreeToComposer.setReplyWith(replyAgree.getSender().getLocalName()+System.currentTimeMillis());
                    agent.send(replayAgreeToComposer);
                    confirmTransition = 5;

                }else{block();}

            }
            public int onEnd()
            {
                firstTimeHere++;
                if (confirmTransition == 4)
                {
                    block(500);
                }
                return confirmTransition;
            }

        }

        private class HandleInformBehaviour extends OneShotBehaviour
        {

            private int transition = 6;
            private int firstTimeHere = 0;
            private MessageTemplate mt1;
            private MessageTemplate mt1andmt2;
            public HandleInformBehaviour()
            {
                super(agent);
            }
            public void action()
            {
                 if(firstTimeHere < 0)
                 {
                     mt1 =  MessageTemplate.and(
                             MessageTemplate.MatchConversationId("introInteraction-musician-composer-INFORM"),
                             MessageTemplate.MatchInReplyTo(replyAgree.getReplyWith()));

                     mt1andmt2 = MessageTemplate.and(mt1,
                             MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                 }
                getMessageInform = agent.receive(mt1andmt2);
                if (getMessageInform != null)
                {
                    System.out.println("Agent "+getMessageInform.getSender().getName()+" Informed ");
                    //The intro is playing then send the inform message to the musician leader
                    agent.send(informMusician);
                    try
                    {
                        ContentElement content = agent.getContentManager().extractContent(getMessageInform);
                        Concept action = ((Action)content).getAction();
                        //Get the duration from the responder and save it into the variable
                        if (action instanceof IntroConcepts){
                            startedIntroAt = ((IntroConcepts) action).getIntroStartedAt();
                        }

                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    //set this to 1 so will direct the transition to the pass intro data state.
                    stateStart = 1;
                    transition = 7;
                }
                else {block();}



            }
            public int onEnd()
            {
                if (transition == 6)
                {
                    block(500);
                }
                return transition;
            }
        }

        private class EndConversationWithComposer extends OneShotBehaviour
        {
            public EndConversationWithComposer()
            {
                super(agent);
            }

            public void action()
            {
                goOut = true;
            }
        }

    }


}
