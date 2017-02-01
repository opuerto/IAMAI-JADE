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
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.interfaces.DataStorteMusicians;
import tools.ensemble.ontologies.musicians.vocabulary.actions.PlayIntroAction;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Intro;

/**
 * Created by OscarAlfonso on 1/31/2017.
 */
public class AccompanientPlayIntroBackup extends OneShotBehaviour implements DataStoreTimeManager,DataStorteMusicians, JMC {

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
    //Only for now will help us to use the composer conversation simulation
    Score theScore = new Score("The Score");
    Phrase thePhrase = new Phrase("the phrase");
    Part thePart = new Part("the part");
    Note theNote;


    public AccompanientPlayIntroBackup (Agent a,Codec codec,Ontology onto,Ontology timeHandler)
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
            IntroNegotiationResponder introNegotiationResponder = new IntroNegotiationResponder(myAgent,mt);
            introNegotiationResponder.setDataStore(getDataStore());
            myAgent.addBehaviour(introNegotiationResponder);
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
        return transition;
    }

    private class IntroNegotiationResponder extends ContractNetResponder
    {
        private int lenght;
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
                    System.out.println("length: "+lenght);
                    System.out.println("duration: "+duration);
                    System.out.println("now: "+now);
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
            System.out.println("duration calculada: "+calculateDuration);
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
            myAgent.addBehaviour(new IntroInteractionTimeManager(duration));


            if(startedIntroAt > 0)
            {
                System.out.println("Agent "+agentHandleIntro.getLocalName()+": Action successfully performed");
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                step = 1;
                return inform;
            }else
            {
                System.out.println("Agent "+agentHandleIntro.getLocalName()+": Action execution failed");
                throw new FailureException("unexpected-error");
            }

        }

        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            System.out.println("Agent "+agentHandleIntro.getLocalName()+": Proposal rejected");
            step =1;

        }

    }

    private class IntroInteractionTimeManager extends Behaviour
    {
        int step = 0;
        private float length;
        private AID receiver = null;
        private MessageTemplate getProposalTemplate;
        private MessageTemplate getConfirmTemplate;
        private boolean exit;
        public IntroInteractionTimeManager(float length)
        {

            this.length = length;
            System.out.println("interaction whit time manager started ");
        }
        public void action()
        {
            if(startedIntroAt >0) {
                //System.out.println("started at inside if "+startedIntroAt);
                switch (step) {
                    case 0:
                        if (getDataStore().containsKey(INTERNAL_TIME_MANAGER)) {
                            receiver = (AID) getDataStore().get(INTERNAL_TIME_MANAGER);
                        }

                        if (receiver != null) {
                            ACLMessage sendInfoMSG = new ACLMessage(ACLMessage.INFORM);
                            //set the receiver of the message
                            sendInfoMSG.setLanguage(codec.getName());
                            sendInfoMSG.setOntology(timeHandlerOntology.getName());
                            //Set the data on the Intro Object
                            long sta = System.currentTimeMillis();
                            Intro intro = new Intro();
                            intro.setIntroLenght(length);
                            intro.setIntroStartedAt(sta);

                            try {
                                myAgent.getContentManager().fillContent(sendInfoMSG, new Action(receiver, intro));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            sendInfoMSG.addReceiver(receiver);
                            sendInfoMSG.setConversationId("intro-interaction-ITM");
                            sendInfoMSG.setReplyWith("CFP" + System.currentTimeMillis());
                            myAgent.send(sendInfoMSG);
                            //prepare the template to get the confirmation

                            getProposalTemplate = MessageTemplate.and(
                                    MessageTemplate.MatchConversationId("intro-interaction-ITM"),
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

        }

        public boolean done()
        {
            if (exit)
            {
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

        }
    }
    private class PlaySomething extends Behaviour
    {
        private int measures;
        private long startedToPlay;
        private boolean exit=false;
        public PlaySomething(int measures)
        {
            this.measures = measures;
        }
        public void action()
        {

            //doWait(500);

            Score modeScore = new Score("Drunk walk demo",Musician.tempo);
            modeScore.setNumerator(Musician.timeSignatureNumerator);

            modeScore.setDenominator(Musician.timeSignatureDenominator);
            Part inst = new Part("SAX", SAXOPHONE);
            Phrase phr = new Phrase();

            int pitch = C3; // variable to store the calculated pitch (initialized with a start pitch value)
            int numberOfNotes = measures * Musician.timeSignatureNumerator;
            System.out.println("numberOfNotes: "+numberOfNotes);
            double pitches[] = {E5,G5,C6,F5};
            for (int i = 0; i < numberOfNotes; i++)
            {
                int  x = (int)(Math.random()*4);
                phr.add(new Note(pitches[x],QUARTER_NOTE));
            }

            // add the phrase to an instrument and that to a score
            inst.addPhrase(phr);
            modeScore.addPart(inst);

            // create a MIDI file of the score
            //View.notate(modeScore);
            startedIntroAt = System.currentTimeMillis();
            if(startedIntroAt >0)
            {
                startedIntroAt = System.currentTimeMillis();
                Play.midi(modeScore,false,false,1,1);
                agentPlaying = true;
                exit = true;
            }



            //Write.midi(modeScore,"prueba.mid");
            //Play.midi(phr);
            /*double endtime = modeScore.getEndTime();
            int numerator = modeScore.getNumerator();
            double temp = modeScore.getTempo();
            double numMeasure = endtime/numerator;
            double t = (numerator*numMeasure/temp)*60*1000;
            double getPhraseendtime = phr.getEndTime();
            System.out.println("phrase end time: "+getPhraseendtime);
             System.out.println("time signature: "+modeScore.getTimeSignature());
            System.out.println("end time: "+endtime+ " numerator: "+numerator+" temp: "+temp+" numMeasure: "+numMeasure);
            System.out.println("espere " + (int)t );*/
        }

        public long getStartedToPlay()
        {
            return this.startedToPlay;
        }

        public boolean done()
        {
            if (exit)
            {
                return true;
            }
            return false;
        }
    }





}
