package tools.ensemble.agents;

/**
 * Created by OscarAlfonso on 1/15/2017.
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
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import jade.wrapper.ControllerException;
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
import tools.ensemble.ontologies.musicelements.MusicElementsOntology;
import tools.ensemble.ontologies.musicelements.PruebaOnto;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ScoreElements;
import tools.ensemble.ontologies.musicians.MusicianOntology;
import tools.ensemble.ontologies.musicians.vocabulary.actions.PlayAccompaniementAction;
import tools.ensemble.ontologies.musicians.vocabulary.actions.PlayIntroAction;

public class Musician extends Agent implements Leader,SongStructure,Accompaniment, JMC {
    boolean leader = false;
    boolean acompaniement = true;
    Map<String, String> songStructure = new HashMap<String, String>();

    private Codec codec = new SLCodec();
    private Ontology ontology = MusicElementsOntology.getInstance();
    private Ontology musicianOnto = MusicianOntology.getInstance();

    private AID internalTimeManager = new AID();
    //Elements of the Score
    public  int tempos;
    public  int timeSignatureNumerator;
    public  int timeSignatureDenominator;
    public static String tuneForm;
    public  int measures;

    protected void setup()
    {

        Play.midi(new Score(),false,false,1,0);
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
        ServiceDescription sd3 = new ServiceDescription();
        sd3.setType("interact-internal-time-manager");
        sd3.setName(getName());
        try {
            sd3.setOwnership(getContainerController().getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }
        dfd.addServices(sd);
        dfd.addServices(sd3);

        try {
            DFService.register(this,dfd);
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP)
        );

        //Find the internal TIMEManager
        DFAgentDescription templateService = new DFAgentDescription();
        ServiceDescription sd2 = new ServiceDescription();
        sd2.setType("InternalTimeManager");
        try {
            sd2.setOwnership(getContainerController().getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }
        templateService.addServices(sd2);
        try
        {
            DFAgentDescription[] resultTimeManager = DFService.search(this,templateService);
            for(int i =  0; i<resultTimeManager.length; i++)
            {
                internalTimeManager = resultTimeManager[i].getName();
                System.out.println("internal TimeManager: "+internalTimeManager);
            }
        }catch (FIPAException fe)
        {
            fe.printStackTrace();
        }

        /*addBehaviour(new SimpleBehaviour() {
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

        //addBehaviour(new ReceiveMessage(this));
        addBehaviour(new ReceiveSongStructure(this));
        addBehaviour(new IntroResponder(this,template));
        addBehaviour(new HandleRequestAccompaniement());


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

    class ReceiveMessage extends CyclicBehaviour
    {
        public ReceiveMessage(Agent a)
        {
            super(a);
        }

        public void action()
        {
            ACLMessage msg = receive();
            if(msg == null){block(); return;}
            try
            {
               System.out.println("extract the conten");
                ContentElement content = getContentManager().extractContent(msg);
                Concept action = ((Action)content).getAction();
                System.out.println(action);
                System.out.println("performative " +msg.getPerformative());
                switch (msg.getPerformative())
                {
                    case(16):
                        if(action instanceof ScoreElements)
                        {
                            System.out.println("Enter to the if");
                            addBehaviour(new HandleOperation(myAgent,msg));
                        }

                }
                /*Object content = msg.getContentObject();
                switch (msg.getPerformative())
                {
                    case(ACLMessage.REQUEST):
                        if(content instanceof PruebaOnto)
                            addBehaviour(new HandleOperation(myAgent,msg));
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class HandleOperation extends OneShotBehaviour
    {
        private ACLMessage r;
        public HandleOperation(Agent a,ACLMessage request)
        {
            super(a);
            this.r = request;

        }
        public void action()
        {
            try {
                System.out.println("handle operation run");
                ContentElement content = getContentManager().extractContent(r);
                ScoreElements se = (ScoreElements)((Action)content).getAction();
                int tempo = se.getTempo();
                int numerator = se.getNumerator();
                int denominaor = se.getDenominator();
                String form  = se.getForm();
                 tempos = tempo;
                 timeSignatureNumerator = numerator;
                 timeSignatureDenominator = denominaor;
                 tuneForm = form;
                System.out.println("This is the tempo: "+tempo);
                System.out.println("This is the time signature numeraor: "+numerator);
                System.out.println("This is the time signature denominator: "+denominaor);
                System.out.println("This is the Form : "+form);

                /*System.out.println("This is the object");
                PruebaOnto element = (PruebaOnto) r.getContentObject();
                String name = element.getName();
                int age = element.getAge();
                System.out.println(name+" tiene "+age);*/
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                ContentElement content = getContentManager().extractContent(msg);
                Concept concept = ((Action)content).getAction();
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
                    System.out.println("this is the tempo "+tempos);
                    System.out.println("this is the timesignature "+timeSignatureNumerator);
                    System.out.println("this is the denominator " + timeSignatureDenominator);
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

    private class HandleRequestAccompaniement extends CyclicBehaviour
    {
        private long getTimeLeft;
        public void action()
         {
             MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("request-accompaniement"),MessageTemplate.MatchPerformative(ACLMessage.CFP));
             ACLMessage msg = receive(mt);
             if(msg == null){block(); return;}
             try
             {
                 ContentElement content = getContentManager().extractContent(msg);
                 AgentAction action = (AgentAction) ((Action)content).getAction();
                 if(action instanceof PlayAccompaniementAction)
                 {
                     getTimeLeft = (long) ((PlayAccompaniementAction) action).getTimeLeft();
                     System.out.println("The time left is: "+getTimeLeft);

                     Score s = new Score("s",tempos);
                     Phrase p = new Phrase("p");
                     int x;
                     double[] pattern = new double[5];
                     double[] pattern0 = {1.0, 0.5, 0.5, 1.5, 0.5};
                     double[] pattern1 = {0.5, 0.5, 1.5, 0.5, 1.0};
                     double[] pattern2 = {2.0, 0.5, 0.5, 0.5, 0.5};
                     double[] pattern3 = {1.5, 0.5, 1.0, 0.5, 0.5};
                     for(int i=0;i<8;i++){
                         // choose one of the patterns at random
                         x = (int)(Math.random()*4);
                         System.out.println("x = " + x);

                         switch (x) {

                             case 0:
                                 pattern = pattern0;
                                 break;
                             case 1:
                                 pattern = pattern1;
                                 break;
                             case 2:
                                 pattern = pattern2;
                                 break;
                             case 3:
                                 pattern = pattern3;
                                 break;
                             default:
                                 System.out.println("Random number out of range");
                                 System.exit(0); // end the program now
                         }
                         // create notes for the chosen pattern to the phrase
                         for (short j=0; j<pattern.length; j++) {
                             Note note = new Note(38, pattern[j]);
                             p.addNote(note);
                         }
                     }
                     // finish with a crash cymbal
                     Note note = new Note(49, 4.0);
                     p.addNote(note);
                     /*int pitch = F4;
                     for(int i =0; i<10; i++)
                     {
                         pitch++;
                         p.add(new Note(pitch,QUARTER_NOTE));
                     }*/
                     Part par = new Part("Snare", 0, 9);
                     par.add(p);
                     s.addPart(par);
                     doWait(getTimeLeft);
                     System.out.println("Playing the accompaniement");
                     Play.midi(s,false,false,1,1);
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
        private Agent agent;
        public IntroResponder(Agent a, MessageTemplate mt)
        {
            super(a,mt); this.agent = a;
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
            {   double t = tempos;
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

                agent.addBehaviour(new PlaySomething());
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
    public float calculateDuration()
    {
        //((beatPerMeasure*measures/tempo)*60*1000);
        float duration = ((timeSignatureNumerator*measures/tempos))*60*1000;
        System.out.println("duration: "+duration);
        return (float) 8000;
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

    private class PlaySomething extends OneShotBehaviour
    {
        public void action()
        {

            //doWait(500);
            Score modeScore = new Score("Drunk walk demo",tempos);
            modeScore.setNumerator(timeSignatureNumerator);

            modeScore.setDenominator(timeSignatureDenominator);
            Part inst = new Part("Piano", PIANO);
            Phrase phr = new Phrase();

            int pitch = C3; // variable to store the calculated pitch (initialized with a start pitch value)
            int numberOfNotes = measures * timeSignatureNumerator;
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
            Play.midi(modeScore,false,false,1,1);
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
