package tools.ensemble.agents;

/**
 * Created by OscarAlfonso on 1/16/2017.
 */
import jade.content.AgentAction;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import java.util.*;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.util.Play;
import jm.util.View;
import jm.util.Write;
import tools.ensemble.interfaces.Accompaniment;
import tools.ensemble.interfaces.Leader;
import tools.ensemble.interfaces.SongStructure;
import jade.lang.acl.ACLMessage;
import tools.ensemble.ontologies.musicelements.MusicElementsOntology;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ScoreElements;
import tools.ensemble.ontologies.musicians.MusicianOntology;
import tools.ensemble.ontologies.musicians.vocabulary.actions.PlayIntroAction;

public class Musician extends Agent implements Leader,SongStructure,Accompaniment, JMC {

    boolean leader = false;
    boolean acompaniement = true;
    Map<String, String> songStructure = new HashMap<String, String>();
    private Codec codec = new SLCodec();
    private Ontology ontology = MusicElementsOntology.getInstance();
    private Ontology musicianOnto = MusicianOntology.getInstance();


    //Elements of the Score
    public  int tempos;
    public  int timeSignatureNumerator;
    public  int timeSignatureDenominator;
    public static String tuneForm;
    public static int measures;

    protected void setup()
    {
        Play.midi(new Score(),false,false,1,0);
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP)
        );

        //REgister the language and ontology
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(musicianOnto);
        getContentManager().registerOntology(ontology);

        System.out.println(getAID().getLocalName());
        //Register the musician acompaniment service
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Musician");
        sd.setName(getLocalName()+"-musician");
        dfd.addServices(sd);
        try {
            DFService.register(this,dfd);
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }

       /* addBehaviour(new SimpleBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println("I recieve a message from SAX");
                    System.out.println("the message say "+msg.getContent());
                }
            }

            @Override
            public boolean done() {
                return false;
            }
        });*/

        addBehaviour(new ReceiveSongStructure(this));
        addBehaviour(new IntroResponder(this,template));
    }

    @Override
    protected void takeDown() {
        //Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }

    }

    private class ReceiveSongStructure extends CyclicBehaviour
    {
        public ReceiveSongStructure(Agent a)
        {
            super(a);

        }
        public void action()
        {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("score-elements"),MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            ACLMessage msg = receive(mt);
            if(msg == null){block(); return;}
            try
            {
                System.out.println("I got a message");
                ContentElement contenido = getContentManager().extractContent(msg);
                Concept concept = ((Action)contenido).getAction();
                if(concept instanceof ScoreElements)
                {
                    int tempo = ((ScoreElements) concept).getTempo();
                    int numerator = ((ScoreElements) concept).getNumerator();
                    int denominaor = ((ScoreElements) concept).getDenominator();
                    String form  = ((ScoreElements) concept).getForm();
                    tempos = tempo;
                    timeSignatureNumerator = numerator;
                    timeSignatureDenominator = denominaor;
                    tuneForm = form;
                    System.out.println("this is the Form "+tempos);
                    System.out.println("this is the tempo "+timeSignatureNumerator);
                    System.out.println("this is the time signature: " + timeSignatureDenominator);
                    ACLMessage msgConfirm = msg.createReply();
                    msgConfirm.setPerformative(ACLMessage.CONFIRM);
                    msgConfirm.setContent("I got the elements of the score");
                    send(msgConfirm);

                }

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class IntroResponder extends ContractNetResponder
    {
        private int lenght;
        private boolean now;
        private float duration;
        private PlayIntroAction pia;
        public IntroResponder(Agent a, MessageTemplate mt)
        {
            super(a,mt);
        }

        protected ACLMessage handleCfp (ACLMessage cfp) throws NotUnderstoodException, RefuseException
        {


            try {
                ContentElement content = getContentManager().extractContent(cfp);
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
            System.out.println("Agent "+getLocalName()+": CFP received from "+cfp.getSender().getName()+". Action is "+cfp.getContent());
            System.out.println("lenght: "+lenght);
            measures = lenght;
            System.out.println("now: "+now);
            System.out.println("duration: "+duration);
            int proposal = evaluateAction();
            if(proposal > 2)
            {   //we provide a proposal
                double t = tempos;
                double ts = timeSignatureNumerator;
                double meas = measures;
                pia = new PlayIntroAction();
                pia.setLenght(lenght);
                pia.setDuration(0);
                double s = (double) (((ts*meas)/t)*60*1000);
                System.out.println("S: "+s);
                pia.setDuration((float)s);
                pia.setNow(true);
                System.out.println(pia.getDuration()+" "+pia.getLenght());
                System.out.println("Agent "+getLocalName()+": Proposing "+proposal);
                ACLMessage propose = cfp.createReply();
                propose.setLanguage(codec.getName());
                propose.setOntology(musicianOnto.getName());
                try {
                    getContentManager().fillContent(propose,new Action(cfp.getSender(),pia));
                } catch (Codec.CodecException e) {
                    e.printStackTrace();
                } catch (OntologyException e) {
                    e.printStackTrace();
                }
                propose.setPerformative(ACLMessage.PROPOSE);

                return propose;
            }else {
                // We refuse to provide a proposal
                System.out.println("Agent "+getLocalName()+": Refuse");
                throw new RefuseException("evaluation-failed");
            }

        }

        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException
        {
            System.out.println("Agent "+getLocalName()+": Proposal accepted");
            if(performAction())
            {

                myAgent.addBehaviour(new PlaySomething());

                System.out.println("Agent "+getLocalName()+": Action successfully performed");
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }
            else
            {
                System.out.println("Agent "+getLocalName()+": Action execution failed");
                throw new FailureException("unexpected-error");
            }
        }
        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            System.out.println("Agent "+getLocalName()+": Proposal rejected");
        }
    }
    // if the musician is leading set the song structure.
    public void setSongStructure()
    {
        if(isLeader())
        {
            songStructure.put("form",form);
            songStructure.put("tempo",tempo);
            songStructure.put("numerator",numerator);
            songStructure.put("denominator",denominator);
        }

    }

    //Find out if the musician is leading the ensemble
    public boolean isLeader() {

        return leader;
    }

    private int evaluateAction() {
        // Simulate an evaluation by generating a random number
        return (int) (Math.random() * 10);
    }

    private boolean performAction() {
        // Simulate action execution by generating a random number
        return (Math.random() * 5) > 2;
    }
    public float calculateDuration()
    {
        //((beatPerMeasure*measures/tempo)*60*1000);
        float duration = ((timeSignatureNumerator*measures/tempos))*60*1000;
        System.out.println("duration: "+duration);
        return (float)8000;
    }
    private class PlaySomething extends OneShotBehaviour
    {
        public void action()
        {
            //doWait(500);
            Score modeScore = new Score("Drunk walk demo",tempos);
            modeScore.setNumerator(timeSignatureNumerator);
            modeScore.setDenominator(timeSignatureDenominator);

            Part inst = new Part("Bass", SYNTH_BASS, 0);
            Phrase phr = new Phrase();

            int pitch = C3;
            int numberofNotes = measures*timeSignatureNumerator;
            System.out.println("numberOfNotes: "+numberofNotes);
            for (int i = 0; i < numberofNotes; i++)
            {
               pitch++;
                phr.add(new Note(pitch,QUARTER_NOTE));
            }

            // add the phrase to an instrument and that to a score
            inst.addPhrase(phr);
            modeScore.addPart(inst);

            // create a MIDI file of the score
           // View.print(modeScore);
            //View.notate(modeScore);
            //View.show(modeScore);
            Play.midi(modeScore,false,false,1,1);
            //Write.midi(modeScore,"prueba.mid");
            //Play.midi(modeScore);
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
    }
    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public void setAcompaniment(boolean acompaniment) {
        this.acompaniement = acompaniment;
    }

    public boolean isAcompaniement() {
        return acompaniement;
    }

}
