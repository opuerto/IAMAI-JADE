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
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ChordsAttributes;

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
    private int rootPitch;
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
        int sizeAsection = Musician.getSectionAchords().size();
        ChordsAttributes chordAttribute = (ChordsAttributes) Musician.getSectionAchords().get((int)Math.random()*sizeAsection);
        rootPitch = chordAttribute.getRootPitch()-24;
        int numberOfNotes = lenght * introScore.getNumerator();
        String noteType = chordAttribute.getMajorOrMinor();
        int extension = chordAttribute.getExtension();
        // build the rhythms
        double[] rhythm1 = {0.30,0.70,0.30,0.70,0.30,0.70,0.30};
        double[] rhythm2 = {0.30,0.70,0.30,0.70,1.34};
        double[] rhythm3 = {1.0,0.30,0.70,0.30,1.0};
        double[] rhythm4 = {0.30,0.70,1.0,1.34};
        int temp = 0;
        boolean ok = false;
        int rhythmNumb = (int)(Math.random() *4);
        int rhythmLength = 0;
        int intervalNumb = 0;
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
                    int intervalLenght = getNewInterval(rootPitch,extension,noteType).length;
                    int[] p = new int[intervalLenght];
                    p =  getNewInterval(rootPitch,extension,noteType);
                    intervalNumb = (int)(Math.random()*intervalLenght);
                    if (temp != p[intervalNumb])
                    {
                        temp = p[intervalNumb];
                        ok = true;
                        break;
                    }

                }
                //add the next note to the phrase
                if (rhythmNumb == 0) introPhrase.addNote(
                        new Note(temp, rhythm1[k]));
                if (rhythmNumb == 1) introPhrase.addNote(
                        new Note(temp, rhythm2[k]));
                if (rhythmNumb == 2) introPhrase.addNote(
                        new Note(temp, rhythm3[k]));
                if (rhythmNumb == 3) introPhrase.addNote(
                        new Note(temp, rhythm4[k]));
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

    private int[] getNewInterval(int pitch, int extension, String type)
    {
        int[] pitchArray = new int[3];
        if(type.equals("m") && extension == 7)
        {
            int[] pitchArrayMinor7 = new int[4];
            pitchArrayMinor7[0] = pitch;
            pitchArrayMinor7[1] = pitch + 3;
            pitchArrayMinor7[2] = pitch + 7;
            pitchArrayMinor7[3] = pitch + 10;
            return pitchArrayMinor7;
        }
        else if(type.equals("M") && extension == 7)
        {
            int[] pitchArrayMajor7 = new int[4];
            pitchArrayMajor7[0] = pitch;
            pitchArrayMajor7[1] = pitch + 4;
            pitchArrayMajor7[2] = pitch + 7;
            pitchArrayMajor7[3] = pitch + 11;
            return pitchArrayMajor7;

        }
        else if(type.equals("D") && extension == 7)
        {

            int[] pitchArrayDominant7 = new int[4];
            pitchArrayDominant7[0] = pitch;
            pitchArrayDominant7[1] = pitch + 4;
            pitchArrayDominant7[2] = pitch + 7;
            pitchArrayDominant7[3] = pitch + 10;
            return pitchArrayDominant7;

        }
        else if (type.equals("Db") && extension == 7)
        {
            int[] pitchArrayDominantB7 = new int[4];
            pitchArrayDominantB7[0] = pitch-1;
            pitchArrayDominantB7[1] = pitch + 3;
            pitchArrayDominantB7[2] = pitch + 6;
            pitchArrayDominantB7[3] = pitch + 9;
            return pitchArrayDominantB7;
        }
        else if(type.equals("M") && extension == 0)
        {
            int[] pitchArrayTriad = new int[3];
            pitchArrayTriad[0] = pitch ;
            pitchArrayTriad[1] = pitch + 4;
            pitchArrayTriad[2] = pitch + 7;
            return pitchArrayTriad;
        }
        else if (type.equals("m") && extension == 0)
        {
            int[] pitchArrayTriad = new int[3];
            pitchArrayTriad[0] = pitch ;
            pitchArrayTriad[1] = pitch + 3;
            pitchArrayTriad[2] = pitch + 7;
            return pitchArrayTriad;
        }else if(type.equals("Dsus"))
        {
            int[] pitchArrayTriad = new int[3];
            pitchArrayTriad[0] = pitch ;
            pitchArrayTriad[1] = pitch + 7;
            pitchArrayTriad[2] = pitch + 10;
            return pitchArrayTriad;
        }
        else
        {
            pitchArray[0] = pitch;
            pitchArray[1] = rootPitch + 4;
            pitchArray[2] = rootPitch + 7;

        }

        return pitchArray;
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
