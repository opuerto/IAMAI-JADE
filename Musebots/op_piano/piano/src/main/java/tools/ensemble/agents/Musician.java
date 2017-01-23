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
import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Phrase;
import jm.util.Play;
import tools.ensemble.interfaces.Acompaniment;
import tools.ensemble.interfaces.Leader;
import tools.ensemble.interfaces.SongStructure;
import tools.ensemble.ontologies.musicelements.MusicElementsOntology;
import tools.ensemble.ontologies.musicelements.PruebaOnto;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ScoreElements;
import tools.ensemble.ontologies.musicians.MusicianOntology;
import tools.ensemble.ontologies.musicians.vocabulary.actions.PlayIntroAction;

public class Musician extends Agent implements Leader,SongStructure,Acompaniment, JMC {
    boolean leader = false;
    boolean acompaniement = true;
    Map<String, String> songStructure = new HashMap<String, String>();

    private Codec codec = new SLCodec();
    private Ontology ontology = MusicElementsOntology.getInstance();
    private Ontology musicianOnto = MusicianOntology.getInstance();


    //Elements of the Score
    public static int tempos;
    public static int timeSignatureNumerator;
    public static int timeSignatureDenominator;
    public static String tuneForm;
    public static int measures;

    protected void setup()
    {
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

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP)
        );


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
                    System.out.println("this is the Form "+((ScoreElements) concept).getForm());
                    System.out.println("this is the tempo "+((ScoreElements) concept).getTempo());
                    System.out.println("this is the time signature: " + ((ScoreElements) concept).getNumerator()+ "/" + ((ScoreElements) concept).getDenominator());
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
                pia = new PlayIntroAction();
                pia.setLenght(lenght);
                pia.setDuration(calculateDuration());
                pia.setNow(true);
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

                Phrase p = new Phrase();
                p.setTempo(tempos);
                p.add(new Note(C4,QUARTER_NOTE));p.add(new Note(D4,QUARTER_NOTE));p.add(new Note(G4,QUARTER_NOTE));p.add(new Note(C4,QUARTER_NOTE));
                p.add(new Note(E4,QUARTER_NOTE));p.add(new Note(G4,QUARTER_NOTE));

                Play.midi(p);

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
