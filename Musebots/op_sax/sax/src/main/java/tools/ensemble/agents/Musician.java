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
    private boolean acompaniement = false;
    private AID myMusician = new AID();
    //Map<String, String> songStructure = new HashMap<String, String>();
    //Map<String, AID> musiciansList = new HashMap<String, AID>();
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



    protected void setup()
    {
        Play.midi(new Score(),false,false,1,0);
        //register Languages and Ontologies
        registerLanguagesAndOntologies();

        //Create the final state machine instance
        FSMBehaviour fsm = new FSMBehaviour(this);

        // Register state A (first state)
        fsm.registerFirstState(new StartBehaviour(this), STATE_START);
        fsm.registerState(new RegisterMusician(this),STATE_REGISTER);
        //Create an instance of the GetMembers class behaviour so we are allowed to use the Data Store
        Behaviour getMembers = new GetMembers(this,leader,acompaniement);
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

        fsm.registerLastState(new TemporaryBehaviour(),STATE_SILENT);
       /* fsm.registerState(new TemporaryBehaviour(),STATE_LEADER);
        fsm.registerState(new TemporaryBehaviour(),STATE_REQUEST_SOLO);
        fsm.registerState(new TemporaryBehaviour(),STATE_PASS_LEAD);
        fsm.registerState(new TemporaryBehaviour(),STATE_SHARE_STRUCTURE);
        fsm.registerState(new TemporaryBehaviour(),STATE_REQUEST_INTRO);
        fsm.registerState(new TemporaryBehaviour(),STATE_REQUEST_END);
        fsm.registerLastState(new TemporaryBehaviour(),STATE_END);
        fsm.registerState(new TemporaryBehaviour(),STATE_SILENT);
        fsm.registerState(new TemporaryBehaviour(),STATE_ACCOMPANIST);
        fsm.registerState(new TemporaryBehaviour(),STATE_GET_STRUCTURE);
        fsm.registerState(new TemporaryBehaviour(),STATE_INTRO);
        fsm.registerState(new TemporaryBehaviour(),STATE_ACCEPT_INTRO);
        fsm.registerState(new TemporaryBehaviour(),STATE_REFUSE_INTRO);
        fsm.registerState(new TemporaryBehaviour(),STATE_ACCEPT_ACCOMPANIMENT);
        fsm.registerState(new TemporaryBehaviour(),STATE_REQUEST_ACCOMPANIMENT);
        fsm.registerState(new TemporaryBehaviour(),STATE_WAITING_LEADERSHIP);
        fsm.registerState(new TemporaryBehaviour(),STATE_ACCOMPANIENT_SILENT);
        fsm.registerState(new TemporaryBehaviour(),STATE_ACCEPT_ENDING);*/

       // fsm.registerState(new LeadingBehaviour(),STATE_LEADING);
       // fsm.registerState(new AccompanimentBehaviour(),STATE_ACCOMPANIMENT);
       // fsm.registerState(new GetMusicianList(),STATE_GETMUSICIANSLIST);
       // fsm.registerLastState(new ShareSongStructure(),STATE_SHARESONGSTRUCTURE);

        //Register transitions
        fsm.registerDefaultTransition(STATE_START,STATE_REGISTER);
        fsm.registerDefaultTransition(STATE_REGISTER,STATE_GET_MEMBERS);
        fsm.registerTransition(STATE_GET_MEMBERS,STATE_GET_MEMBERS,0);
        fsm.registerTransition(STATE_GET_MEMBERS,STATE_LEADER,1);
        fsm.registerTransition(STATE_GET_MEMBERS,STATE_ACCOMPANIST,2);
        fsm.registerTransition(STATE_LEADER,STATE_LEADER_GET_SONG_STRUCTURE,3);
        fsm.registerTransition(STATE_LEADER,STATE_SILENT,15);
        fsm.registerDefaultTransition(STATE_LEADER_GET_SONG_STRUCTURE,STATE_SHARE_STRUCTURE);
        fsm.registerTransition(STATE_SHARE_STRUCTURE,STATE_REQUEST_INTRO,4);
        fsm.registerTransition(STATE_SHARE_STRUCTURE,STATE_SHARE_STRUCTURE,28);
        fsm.registerTransition(STATE_REQUEST_INTRO,STATE_REQUEST_INTRO,29);
        fsm.registerTransition(STATE_REQUEST_INTRO,STATE_LEADER,17);
        //Transition to request the solo
        fsm.registerTransition(STATE_LEADER,STATE_REQUEST_SOLO,11);
        fsm.registerTransition(STATE_REQUEST_SOLO,STATE_REQUEST_SOLO,30);
        fsm.registerTransition(STATE_REQUEST_SOLO,STATE_SILENT,50);

        /*fsm.registerTransition(STATE_SHARE_STRUCTURE,STATE_REQUEST_INTRO,4);
        fsm.registerTransition(STATE_REFUSE_INTRO,STATE_LEADER,17);
        fsm.registerTransition(STATE_LEADER,STATE_REQUEST_SOLO,11);
        fsm.registerTransition(STATE_REQUEST_SOLO,STATE_LEADER,10);
        fsm.registerTransition(STATE_REQUEST_SOLO,STATE_PASS_LEAD,12);
        fsm.registerTransition(STATE_PASS_LEAD,STATE_WAITING_LEADERSHIP,13);
        fsm.registerTransition(STATE_LEADER,STATE_REQUEST_END,19);
        fsm.registerTransition(STATE_REQUEST_END,STATE_END,21);
        fsm.registerTransition(STATE_LEADER,STATE_SILENT,15);
        fsm.registerTransition(STATE_SILENT,STATE_LEADER,16);
        fsm.registerTransition(STATE_ACCOMPANIST,STATE_GET_STRUCTURE,5);
        fsm.registerTransition(STATE_GET_STRUCTURE,STATE_INTRO,6);
        fsm.registerTransition(STATE_INTRO,STATE_ACCEPT_INTRO,7);
        fsm.registerTransition(STATE_INTRO,STATE_REFUSE_INTRO,18);
        fsm.registerTransition(STATE_ACCEPT_INTRO,STATE_ACCEPT_ACCOMPANIMENT,8);
        fsm.registerDefaultTransition(STATE_REFUSE_INTRO,STATE_ACCEPT_ACCOMPANIMENT);
        fsm.registerTransition(STATE_ACCEPT_ACCOMPANIMENT,STATE_REQUEST_ACCOMPANIMENT,9);
        fsm.registerTransition(STATE_REQUEST_ACCOMPANIMENT,STATE_WAITING_LEADERSHIP,20);
        fsm.registerTransition(STATE_WAITING_LEADERSHIP,STATE_REQUEST_ACCOMPANIMENT,26);
        fsm.registerTransition(STATE_REQUEST_ACCOMPANIMENT,STATE_ACCOMPANIENT_SILENT,22);
        fsm.registerTransition(STATE_ACCOMPANIENT_SILENT,STATE_REQUEST_ACCOMPANIMENT,23);
        fsm.registerTransition(STATE_WAITING_LEADERSHIP,STATE_LEADER,14);
        fsm.registerTransition(STATE_WAITING_LEADERSHIP,STATE_ACCEPT_ENDING,27);
        fsm.registerTransition(STATE_ACCEPT_ENDING,STATE_END,25);
      //  fsm.registerTransition(STATE_START,STATE_LEADING,0);*/
      //  fsm.registerTransition(STATE_START,STATE_ACCOMPANIMENT,1);
      //  fsm.registerDefaultTransition(STATE_LEADING,STATE_GETMUSICIANSLIST);
      //  fsm.registerTransition(STATE_GETMUSICIANSLIST,STATE_GETMUSICIANSLIST,3);
      //  fsm.registerTransition(STATE_GETMUSICIANSLIST,STATE_SHARESONGSTRUCTURE,2);

        //Add the Behaviour
        addBehaviour(fsm);



        /*addBehaviour(new TickerBehaviour(this,6000) {
            @Override
            protected void onTick() {
               // System.out.println("Hello");
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("Accompaniment");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent,template);
                    Musicians.clear();
                    for (int i=0; i<result.length; i++)
                    {
                      Musicians.add(result[i].getName());
                    }

                }
                catch (FIPAException fe)
                {
                    fe.printStackTrace();
                }
            }
        });*/

        /*addBehaviour(new TickerBehaviour(this,2000) {
            @Override
            protected void onTick() {
                Iterator it = Musicians.iterator();
                if(!Musicians.isEmpty())
                {

                   for(int i=0; i < Musicians.size(); i++)
                    {
                        System.out.println(Musicians.elementAt(i));


                    }
                }
            }
        });*/



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
            tempo = TEMPO;
            timeSignatureNumerator = NUMERATOR;
            timeSignatureDenominator = DENOMINATOR;
            tuneForm = FORM;

            //set the chords for section A
            sectionAchords = new ArrayList();
            //Create the individual chords.
            ChordsAttributes Em7 = new ChordsAttributes();
            Em7.setRootPitch(E4);
            Em7.setMajorOrMinor("m");
            Em7.setExtension(7);
            //Add them to the list of section A
            sectionAchords.add(Em7);

            //create the next chord
            ChordsAttributes A7 = new ChordsAttributes();
            A7.setRootPitch(A4);
            A7.setMajorOrMinor("D");
            A7.setExtension(7);
            //Add them to the list of section A
            sectionAchords.add(A7);

            //create the next chord
            ChordsAttributes Dm7 = new ChordsAttributes();
            Dm7.setRootPitch(D4);
            Dm7.setMajorOrMinor("m");
            Dm7.setExtension(7);
            //Add them to the list of section A
            sectionAchords.add(Dm7);

            //create the next chord
            ChordsAttributes G7 = new ChordsAttributes();
            G7.setRootPitch(G4);
            G7.setMajorOrMinor("D");
            G7.setExtension(7);
            //Add them to the list of section A
            sectionAchords.add(G7);

            //set the chords for section B
            sectionBchords = new ArrayList();
            //Create the individual chords for section B.
            ChordsAttributes CM7 = new ChordsAttributes();
            CM7.setRootPitch(C4);
            CM7.setMajorOrMinor("M");
            CM7.setExtension(7);
            sectionBchords.add(CM7);
            sectionBchords.add(CM7);

            //set the chords for section C
            sectionCchords = new ArrayList();
            //Create the individual chords for section C.
            sectionCchords.add(CM7);

            //create instance of the object ScoreElements and set the object in the DataStore of the behaviour
            ScoreElements se = new ScoreElements();
            se.setTempo(tempo);
            se.setNumerator(timeSignatureNumerator);
            se.setDenominator(timeSignatureDenominator);
            se.setForm(tuneForm);
            se.setSectionAchords(sectionAchords);
            se.setSectionBchords(sectionBchords);
            se.setSectionCchords(sectionCchords);
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
