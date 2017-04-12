package tools.ensemble.behaviours.ComposerBhaviours.intro;


import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
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
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.ontologies.composer.ComposerOntology;
import tools.ensemble.ontologies.composer.vocabulary.concepts.IntroConcepts;

/**
 * Created by OscarAlfonso on 2/28/2017.
 */
public class ComposeIntro extends OneShotBehaviour implements DataStoreComposer, JMC {

    private int transition = 2;
    //int flag that check if is the first time on this state
    private int firstTimeState = 0;
    //message
    private ACLMessage messagePropose;
    //Composer Ontology
    private Ontology composerOntology = ComposerOntology.getInstance();
    //Agent instance
    private Agent agent;
    //message template
    private MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchConversationId("introInteraction-musician-composer-ACCEPTPROPOSAL"),
            MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL)
    );
    private MessageTemplate mt1andmt2;

    //intro instance object
    private IntroConcepts introInstanceConcepts;
    //The Score of the intro.
    Score introScore = new Score("The intro Score");
    Phrase introPhrase = new Phrase("The intro Phrase");
    Part pianoPart = new Part("the piano part",BASS,1);//Part(java.lang.String title, int instrument, int channel)

    //Instance to the behaviour


    public ComposeIntro(Agent a)
    {
        super(a);
        this.agent = a;

    }

    public void action()
    {
        //System.out.println("Get the current message");
           if(firstTimeState < 1)
        {
            System.out.println("Get the current message");
            if(getDataStore().containsKey(CURRENT_MESSAGE))
            {
                ACLMessage cm = (ACLMessage) getDataStore().get(CURRENT_MESSAGE);
                mt1andmt2 = MessageTemplate.and(mt1,MessageTemplate.MatchInReplyTo(cm.getReplyWith()));

            }
        }
      messagePropose = agent.receive(mt1andmt2);
        if(messagePropose != null)
        {
            if(getDataStore().containsKey(INTRO_COMPOSER_INSTANCE))
            {
                //assign
                introInstanceConcepts = (IntroConcepts) getDataStore().get(INTRO_COMPOSER_INSTANCE);
                introScore.setTempo(introInstanceConcepts.getIntroTempo());
                introScore.setNumerator(introInstanceConcepts.getIntroNumerator());
                introScore.setDenominator(introInstanceConcepts.getIntroDenominator());

            }
            ComposeIntro();
            getDataStore().put(INTRO_SCORE,introScore);
            ACLMessage replyAgree =  messagePropose.createReply();
            replyAgree.setPerformative(ACLMessage.AGREE);
            replyAgree.setConversationId("introInteraction-musician-composer-AGREE");
            if(getDataStore().containsKey(CURRENT_MESSAGE))
            {
                getDataStore().remove(CURRENT_MESSAGE);
                getDataStore().put(CURRENT_MESSAGE,replyAgree);
            }
            agent.send(replyAgree);

            transition = 3;
        } else{
            block();
        }

    }

    private void ComposeIntro()
    {
        int lenght = introInstanceConcepts.getIntroLength();
        int rootPitch = C1-24; //set start pitch to C
       // int pitch = C3; // variable to store the calculated pitch (initialized with a start pitch value)
        int numberOfNotes = lenght * introScore.getNumerator();
        // build the rhythms
        boolean ok = false;
        double[] rhythm1 = {0.34,0.66,0.34,0.66,0.34,0.66,0.34};
        double[] rhythm2 = {0.34,0.66,0.34,0.66,1.34};
        double[] rhythm3 = {1.0,0.34,0.66,0.34,1.0};
        double[] rhythm4 = {0.34,0.66,1.0,1.34};
        int[] mode = {0,4,5,7,9};
        int pitch = (int)Math.random()*12 + 65;
        int temp = 0;
        int rhythmNumb = (int)(Math.random() *4);
        int rhythmLength = 0;

        //choose a rhythm to use for the phrase
        if (rhythmNumb == 0) rhythmLength = rhythm1.length;
        if (rhythmNumb == 1) rhythmLength = rhythm2.length;
        if (rhythmNumb == 2) rhythmLength = rhythm3.length;
        if (rhythmNumb == 3) rhythmLength = rhythm4.length;

        for (int i = 0; i < lenght; i++)
        {
            introPhrase.addNote(new Note(REST, 0.66));
            for(int k=0;k<rhythmLength;k++) {
                while (ok == false) {
                    //get new interval
                    temp = (int)(Math.random() * 10) - 5;
                    //check to see if new note is in the mode
                    for(int j=0;j<mode.length;j++) {
                        if ((pitch + temp)%12 ==
                                (mode[j] + rootPitch)%12) {
                            pitch += temp;
                            ok = true;
                            break;
                        }
                    }
                }
                //add the next note to the phrase
                if (rhythmNumb == 0) introPhrase.addNote(
                        new Note(pitch-24, rhythm1[k]));
                if (rhythmNumb == 1) introPhrase.addNote(
                        new Note(pitch-24, rhythm2[k]));
                if (rhythmNumb == 2) introPhrase.addNote(
                        new Note(pitch-24, rhythm3[k]));
                if (rhythmNumb == 3) introPhrase.addNote(
                        new Note(pitch-24, rhythm4[k]));
                ok = false;
            }
        }
        pianoPart.addPhrase(introPhrase);
        introScore.addPart(pianoPart);
        //Calculate the lenght of the intro.
        double betPerMeasure = introScore.getNumerator();
        double numberOfMeasure = introScore.getEndTime()/betPerMeasure;
        double tempo = introScore.getTempo();
        double lengofIntro = (betPerMeasure*numberOfMeasure/tempo)*60*1000;
        introInstanceConcepts.setIntroDuration((float) lengofIntro);
    }

    public int onEnd()
    {
        firstTimeState++;
        if(transition == 2)
        {
            block(500);
        }
        return transition;
    }




}
