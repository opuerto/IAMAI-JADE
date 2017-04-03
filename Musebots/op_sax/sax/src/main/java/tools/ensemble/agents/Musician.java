package tools.ensemble.agents;

/**
 * Created by OscarAlfonso on 1/15/2017.
 */
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import java.util.*;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jm.JMC;
import jm.music.data.Score;
import jm.util.Play;
import tools.ensemble.behaviours.musicianBehaviours.*;
import tools.ensemble.interfaces.DataStorteMusicians;
import tools.ensemble.interfaces.MusicianStates;
import tools.ensemble.interfaces.SongStructure;
import tools.ensemble.ontologies.composer.ComposerOntology;
import tools.ensemble.ontologies.musicelements.MusicElementsOntology;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ChordsAttributes;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ScoreElements;
import tools.ensemble.ontologies.musicians.MusicianOntology;
import tools.ensemble.ontologies.timemanager.TimeHandler;
import jade.util.leap.List;
import jade.util.leap.ArrayList;


public class Musician extends Agent implements MusicianStates,DataStorteMusicians, JMC {

    public static boolean leader = true;
    public static boolean fromSupportToLead = false;
    private boolean acompaniement = false;
    private AID myMusician = new AID();
    private Vector Musicians = new Vector();
    private Codec codec = new SLCodec();
    private Ontology ontology = MusicElementsOntology.getInstance();
    private Ontology musicianOntology = MusicianOntology.getInstance();
    private Ontology timeHandlerOntology = TimeHandler.getInstance();
    private Ontology composerOntology = ComposerOntology.getInstance();

    //Elements of the Score
    public static int tempo;
    public static int timeSignatureNumerator;
    public static int timeSignatureDenominator;
    public static String tuneForm;

    public static List sectionAchords;
    public static List sectionBchords;
    public static List sectionCchords;

    public static AID lastMusicianIpassedTheLeadership = null;

    public static synchronized void setLeader(Boolean lead) {
        leader = lead;
    }
    public static synchronized boolean getLeader() {
        return leader;
    }

    public static synchronized void setFromSupportTolead(boolean t)
    {
        fromSupportToLead = t;
    }
    public static synchronized boolean getFromSupportTolead()
    {
        return fromSupportToLead;
    }


    public static synchronized void setTempo(int temp)
    {
        tempo = temp;
    }

    public static synchronized int getTempo()
    {
        return tempo;
    }

    public static synchronized void setTimeSignatureNumerator(int numerator)
    {
        timeSignatureNumerator = numerator;
    }

    public static synchronized int getTimeSignatureNumerator()
    {
        return timeSignatureNumerator;
    }

    public static synchronized void setTimeSignatureDenominator(int denominator)
    {
        timeSignatureDenominator = denominator;
    }

    public static synchronized int getTimeSignatureDenominator()
    {
        return timeSignatureDenominator;
    }

    public static synchronized void setTuneForm(String Form)
    {
        tuneForm = Form;
    }

    public static synchronized String getTuneForm()
    {
        return tuneForm;
    }

    public static synchronized void setSectionAchords(List sectionA)
    {
        sectionAchords = sectionA;
    }
    public static synchronized List getSectionAchords()
    {
        return sectionAchords;
    }

    public static synchronized void setSectionBchords(List sectionB)
    {
        sectionBchords = sectionB;
    }

    public static synchronized List getSectionBchords()
    {
        return sectionBchords;
    }

    public static synchronized void setSectionCchords(List sectionC)
    {
        sectionCchords = sectionC;
    }

    public static synchronized List getSectionCchords()
    {
        return sectionCchords;
    }

    //RULE FROM LEADING TO SUPPORT
    public static boolean isFromLeadingToSupport = false;

    public static synchronized void setIsFromLeadingToSupport(boolean var)
    {
        isFromLeadingToSupport = var;
    }

    public static synchronized boolean getIsFromLeadingToSupport()
    {
        return isFromLeadingToSupport;
    }


    protected void setup()
    {

        //register Languages and Ontologies
        registerLanguagesAndOntologies();

        //Create the final state machine instance
        FSMBehaviour fsm = new FSMBehaviour(this);

        // Register state A (first state)
        fsm.registerFirstState(new StartBehaviour(this), STATE_START);
        fsm.registerState(new RegisterMusician(this),STATE_REGISTER);
        //Create an instance of the GetMembers class behaviour so we are allowed to use the Data Store
        Behaviour getMembers = new GetMembers(this,getLeader(),acompaniement);
        //We share the Final State Machine Data Store with the Behaviour GetMembers
        getMembers.setDataStore(fsm.getDataStore());
        //Add register the state Get_Members and pass the instance of the class as the first parameter.
        fsm.registerState(getMembers,STATE_GET_MEMBERS);
        //Create an instance of the leaderBehaviour
        Behaviour leaderBehaviour = new LeaderSoloist(this);
        //Share the DataStore from the fsm with the leaderBehaviour, so they can share data
        leaderBehaviour.setDataStore(fsm.getDataStore());
        //Register the state with its corresponding leaderBehaviour
        fsm.registerState(leaderBehaviour,STATE_LEADER);
        //Create an instance of the GetSongStructure class
        Behaviour getSongStructure = new GetSongStructure();
        //Share the DataStore from the fsm with the getSongStructureBehaviour, so they can share data
        getSongStructure.setDataStore(fsm.getDataStore());
        //Register the state get song structure
        fsm.registerState(getSongStructure,STATE_LEADER_GET_SONG_STRUCTURE);
        //Create an instance of the ShareSongStructure Behaviour class
        Behaviour shareStructure = new ShareSongStructure(this,codec,ontology);
        //Share the DataStore from the fsm with the ShareSongStructure, so they can share data
        shareStructure.setDataStore(fsm.getDataStore());
        //Register the state with its corresponding ShareSongStructure class behaviour
        fsm.registerState(shareStructure,STATE_SHARE_STRUCTURE);
        //Create an instance of the ShareSongStructure Behaviour class
        RequestIntro requestIntro = new RequestIntro(this, codec,musicianOntology,timeHandlerOntology);
        //Set the DataStore from fsm with the Request Intro behaviour
        requestIntro.setDataStore(fsm.getDataStore());
        //Register the state RequestINTRO To the FSM
        fsm.registerState(requestIntro,STATE_REQUEST_INTRO);
        //State Request Solo
        //Get the instance of the behaviour
        LeaderRequestSoloToMyComposer LRSTC = new LeaderRequestSoloToMyComposer(this);
        //Share the data structure of the fsm
        LRSTC.setDataStore(fsm.getDataStore());
        fsm.registerState(LRSTC,STATE_REQUEST_SOLO);

        //Pass Lead to accompaniement
        PassLeadToAccomBehaviour passLead = new PassLeadToAccomBehaviour(this);
        passLead.setDataStore(fsm.getDataStore());
        fsm.registerState(passLead,STATE_PASS_LEAD);

        //Register Accompaniest state
        fsm.registerState(new TemporaryBehaviour(),STATE_ACCOMPANIST);
        //Get the instance of the get structure of the song behaviour
        AccompanientGetStructure accompanientGetStructure = new AccompanientGetStructure(this);
        //Share the DataStore with the behaviour
        accompanientGetStructure.setDataStore(fsm.getDataStore());
        //Register the behaviour of the state get structure
        fsm.registerState(accompanientGetStructure,STATE_GET_STRUCTURE);
        //Get the instance of the Play intro
        AccompanientPlayIntro accompanientPlayIntro = new AccompanientPlayIntro(this,codec,musicianOntology,timeHandlerOntology,composerOntology);
        //Share the data store
        accompanientPlayIntro.setDataStore(fsm.getDataStore());
        //Register the behaviour in the state intro
        fsm.registerState(accompanientPlayIntro,STATE_INTRO);
        //Create instance of the play Head behaviour
        AccompanientPlaySections playSections = new AccompanientPlaySections(this, codec,musicianOntology,timeHandlerOntology);
        //Share the data Store
        playSections.setDataStore(fsm.getDataStore());
        //Register the state
        fsm.registerState(playSections, STATE_PLAY_SECTIONS);
         //State from leading to support This state will request to play section in order to suppor the new leader
        //We implement this state because the state STATE_PLAY_SECTIONS have some functions related with the intro
        //such functions are not needed when the agent is coming from being a leader.
        FromLeadingToSupportPlaySection fromleaderToSupport = new FromLeadingToSupportPlaySection(this);
        fromleaderToSupport.setDataStore(fsm.getDataStore());
        fsm.registerState(fromleaderToSupport,STATE_FROM_LEADING_TO_SUPPORT);
        //Wait to lead
        WaitToGetLead getLead = new WaitToGetLead(this);
        getLead.setDataStore(fsm.getDataStore());
        fsm.registerState(getLead,STATE_WAITING_LEADERSHIP);

        //Transition

        //Independent transitions
        fsm.registerDefaultTransition(STATE_START,STATE_REGISTER);
        fsm.registerDefaultTransition(STATE_REGISTER,STATE_GET_MEMBERS);
        fsm.registerTransition(STATE_GET_MEMBERS,STATE_GET_MEMBERS,0);

        //Leader Transitions
        fsm.registerTransition(STATE_GET_MEMBERS,STATE_LEADER,1);
        fsm.registerTransition(STATE_LEADER,STATE_LEADER_GET_SONG_STRUCTURE,3);
        fsm.registerDefaultTransition(STATE_LEADER_GET_SONG_STRUCTURE,STATE_SHARE_STRUCTURE);
        fsm.registerTransition(STATE_SHARE_STRUCTURE,STATE_SHARE_STRUCTURE,28);
        fsm.registerTransition(STATE_SHARE_STRUCTURE,STATE_REQUEST_INTRO,4);
        fsm.registerTransition(STATE_REQUEST_INTRO,STATE_REQUEST_INTRO,29);
        fsm.registerTransition(STATE_REQUEST_INTRO,STATE_LEADER,17);
        fsm.registerTransition(STATE_LEADER,STATE_REQUEST_SOLO,11);
        fsm.registerTransition(STATE_REQUEST_SOLO,STATE_REQUEST_SOLO,30);
        fsm.registerTransition(STATE_REQUEST_SOLO,STATE_PASS_LEAD,12,new String[]{STATE_WAITING_LEADERSHIP,
                STATE_REQUEST_SOLO});
        fsm.registerTransition(STATE_PASS_LEAD,STATE_PASS_LEAD,34);


        //Accompaniment Transitions
        fsm.registerTransition(STATE_GET_MEMBERS,STATE_ACCOMPANIST,2);
        fsm.registerDefaultTransition(STATE_ACCOMPANIST,STATE_GET_STRUCTURE);
        fsm.registerTransition(STATE_GET_STRUCTURE,STATE_GET_STRUCTURE,21);
        fsm.registerTransition(STATE_GET_STRUCTURE,STATE_INTRO,6);
        fsm.registerTransition(STATE_INTRO,STATE_INTRO,8);
        fsm.registerTransition(STATE_INTRO,STATE_PLAY_SECTIONS,7);
        fsm.registerTransition(STATE_PLAY_SECTIONS,STATE_PLAY_SECTIONS,40);
        fsm.registerTransition(STATE_FROM_LEADING_TO_SUPPORT,STATE_WAITING_LEADERSHIP,80,new String[]{STATE_PASS_LEAD,
                STATE_FROM_LEADING_TO_SUPPORT});
        fsm.registerTransition(STATE_FROM_LEADING_TO_SUPPORT,STATE_FROM_LEADING_TO_SUPPORT,38);
        fsm.registerTransition(STATE_PLAY_SECTIONS,STATE_WAITING_LEADERSHIP,20);
        fsm.registerTransition(STATE_WAITING_LEADERSHIP,STATE_WAITING_LEADERSHIP,37);

        //INTERCHANGEBLE
        fsm.registerTransition(STATE_WAITING_LEADERSHIP,STATE_REQUEST_SOLO,14,new String[]{STATE_FROM_LEADING_TO_SUPPORT,
                STATE_WAITING_LEADERSHIP});
        fsm.registerTransition(STATE_PASS_LEAD,STATE_FROM_LEADING_TO_SUPPORT,39,new String[]{STATE_REQUEST_SOLO,
                STATE_PASS_LEAD});

        //Add the Behaviour
        addBehaviour(fsm);







    }

    //Register Languages and Ontologies
    private void registerLanguagesAndOntologies()
    {
        //Register language and ontology
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(musicianOntology);
        getContentManager().registerOntology(ontology);
        getContentManager().registerOntology(timeHandlerOntology);
        getContentManager().registerOntology(composerOntology);

    }

    //This class will get the elements of the score and will storage this elements in the corresponding static fields of the agent class.
    //TODO Generate this elements, and also get them from the conductor.
    private class GetSongStructure extends OneShotBehaviour implements SongStructure
    {
        public void action()
        {
            //Since I got into this state remove FIRST_LEADER from the data store.
            getDataStore().remove(FIRST_LEADER);
            setTempo(TEMPO);
            setTimeSignatureNumerator(NUMERATOR);
            setTimeSignatureDenominator(DENOMINATOR);
            setTuneForm(FORM);

            //set the chords for section A
            sectionAchords = new ArrayList();
            //Create the individual chords.
            ChordsAttributes Em7 = new ChordsAttributes();
            Em7.setRootPitch(E4);
            Em7.setMajorOrMinor("m");
            Em7.setExtension(7);
            //Add them to the list of section A
            getSectionAchords().add(Em7);

            //create the next chord
            ChordsAttributes A7 = new ChordsAttributes();
            A7.setRootPitch(A4);
            A7.setMajorOrMinor("D");
            A7.setExtension(7);
            //Add them to the list of section A
            getSectionAchords().add(A7);

            //create the next chord
            ChordsAttributes Dm7 = new ChordsAttributes();
            Dm7.setRootPitch(D4);
            Dm7.setMajorOrMinor("m");
            Dm7.setExtension(7);
            //Add them to the list of section A
            getSectionAchords().add(Dm7);

            //create the next chord
            ChordsAttributes G7 = new ChordsAttributes();
            G7.setRootPitch(G4);
            G7.setMajorOrMinor("D");
            G7.setExtension(7);
            //Add them to the list of section A
            getSectionAchords().add(G7);

            //set the chords for section B
            sectionBchords = new ArrayList();
            //Create the individual chords for section B.
            ChordsAttributes CM7 = new ChordsAttributes();
            CM7.setRootPitch(C4);
            CM7.setMajorOrMinor("M");
            CM7.setExtension(7);
            getSectionBchords().add(CM7);
            getSectionBchords().add(CM7);



            //set the chords for section C
            sectionCchords = new ArrayList();
            //Create the individual chords for section C.
            getSectionCchords().add(CM7);

            //create instance of the object ScoreElements and set the object in the DataStore of the behaviour
            ScoreElements se = new ScoreElements();
            se.setTempo(getTempo());
            System.out.println("Tempo "+getTempo());
            se.setNumerator(getTimeSignatureNumerator());
            System.out.println("Numerator "+getTimeSignatureNumerator());
            se.setDenominator(getTimeSignatureDenominator());
            System.out.println("Denominator "+getTimeSignatureDenominator());
            se.setForm(getTuneForm());
           System.out.println("the form "+getTuneForm());
            se.setSectionAchords(getSectionAchords());
            se.setSectionBchords(getSectionBchords());
            se.setSectionCchords(getSectionCchords());
            getDataStore().put(SCORE_ELEMENTS,se);

        }
    }

    //Check if the agent is leading the ensemble or not.
    private class TemporaryBehaviour extends OneShotBehaviour {
       /* int transitionValue;
        public void action() {
            if(leader)
            {
                transitionValue = 0;
            }
            else
            {
                transitionValue = 1;
            }
        }*/
        public void action(){
            System.out.println(getBehaviourName());
            //System.out.println(getDataStore().get("musicianList"));

        }


    }

    //Confirm that Im leading the ensemble with a message
    private class LeadingBehaviour extends OneShotBehaviour {

        public void action() {
            System.out.println("Executing behaviour "+getBehaviourName()+" Im Leading the ensemble");
        }

    }

    //Confirm that you obey the leader with a message
    private class AccompanimentBehaviour extends OneShotBehaviour
    {
        public void action()
        {
            System.out.println("Executing behaviour "+getBehaviourName()+" I Obey the leader of the ensemble");
        }
    }

    //Get the list of musicians that will be serving as accompaniment
    private class GetMusicianList extends OneShotBehaviour
    {
        int transitionValue;
        public void action()
        {
            // Find the musicians that provide accompaniment service
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Accompaniment");
            template.addServices(sd);
          /*  try {
                DFAgentDescription[] result = DFService.search(myAgent,template);
                Musicians.clear();
                for (int i=0; i<result.length; i++)
                {
                    //Store the list in the vector
                    Musicians.addElement(result[i].getName());
                }

            }
            catch (FIPAException fe)
            {
                fe.printStackTrace();
            }*/

            //At least 3 musicians playing with the leader in the ensemble
            if(Musicians.isEmpty() || Musicians.size() < 3)
            {
                //If there not at least 3 then return to this state
                transitionValue = 3;
            }
            else {transitionValue = 2;} //Go to the next state
        }
        public int onEnd() {
            return transitionValue;
        } //Exit with the transition value to the corresponding state.
    }

    // Share the structure of the song with the rest of the ensemble
   /* private class ShareSongStructure extends OneShotBehaviour
    {

    }*/



}
