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
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.util.Play;
import sun.swing.plaf.synth.SynthIcon;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.interfaces.DataStorteMusicians;
import tools.ensemble.ontologies.musicians.vocabulary.actions.PlayIntroAction;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Intro;

import java.util.Date;

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
    SimulateComposeConversation compose;
    playBack play;
    IntroInteractionTimeManager passInfo;

    //we save myagent
    Agent agent;
    //we kept here the value for the transition to the next state
    int transition;
    //We keept the message template
    MessageTemplate mt;
    //We kept the language
    Codec codec;
    //We kept the musician ontology
    Ontology musicianOntology;
    //We kept the timehandler ontology
    Ontology timeHandlerOntology;
    //we kept the counter that will check if this is the first time in the behaviour
    int again = 0;
    //we kept the handler of the switch in the action method
    int step = 0;
    //check if we got a confirm from the time manager
    private boolean timeManagerGotInfo = false;
    //check if the music is playing
    private boolean agentPlaying = false;
    //kept the moment when the intro started to play
    private long startedIntroAt = -1;
    //kept the duration of the intro
    private float duration;
    //length of the intro
    private int lenght;


    //Only for now will help us to use the composer conversation simulation
    Score theScore = new Score("The Score");
    Phrase thePhrase = new Phrase("the phrase");
    Part thePart = new Part("the part");
    Note theNote;



    public AccompanientPlayIntro (Agent a,Codec codec,Ontology onto,Ontology timeHandler)
    {
        super(a);
        this.agent = a;
        this.codec = codec;
        this.musicianOntology = onto;
        this.timeHandlerOntology = timeHandler;
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
                compose = new SimulateComposeConversation(lenght);
                myAgent.addBehaviour(compose);
                play = new playBack();
                myAgent.addBehaviour(play);
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

            //myAgent.removeBehaviour(introNegotiationResponder);
            //introNegotiationResponder = null;
             //agent.removeBehaviour(compose);
            //compose = null;
             //agent.removeBehaviour(play);
            //play = null;
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
        public IntroNegotiationResponder(Agent agent, MessageTemplate mt)

        {
            super(agent,mt);
            this.agentHandleIntro = agent;
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

            myAgent.addBehaviour(new SimulateComposeConversation(lenght));
            myAgent.addBehaviour(new playBack());

              System.out.println("Agent "+agentHandleIntro.getLocalName()+": Action successfully performed");
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                stateStart = 1;
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
            long elapsedTime =  startedIntroAt - System.currentTimeMillis();
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
            return false;
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
           theScore.setTempo(Musician.tempo);
           theScore.setNumerator(Musician.timeSignatureNumerator);
           theScore.setDenominator(Musician.timeSignatureDenominator);
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
            startedIntroAt = System.currentTimeMillis();
            Play.midi(theScore,false,false,1,1);
            agentPlaying = true;

        }
    }




}
