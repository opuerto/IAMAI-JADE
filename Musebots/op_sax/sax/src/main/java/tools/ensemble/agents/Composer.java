package tools.ensemble.agents;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.ControllerException;
import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.util.Play;
import tools.ensemble.behaviours.ComposerBhaviours.accompaniment.*;
import tools.ensemble.behaviours.ComposerBhaviours.intro.ComposeIntro;
import tools.ensemble.behaviours.ComposerBhaviours.intro.ComposerEndIntro;
import tools.ensemble.behaviours.ComposerBhaviours.intro.ComposerPlayIntro;
import tools.ensemble.behaviours.ComposerBhaviours.intro.ExpectingIntroRequest;
import tools.ensemble.behaviours.ComposerBhaviours.solo.*;
import tools.ensemble.interfaces.ComposerStatesNames;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.interfaces.DataStorteMusicians;
import tools.ensemble.interfaces.MusicianStates;
import tools.ensemble.ontologies.composer.ComposerOntology;
import tools.ensemble.ontologies.composer.vocabulary.actions.PreviousSection;
import tools.ensemble.ontologies.composer.vocabulary.concepts.AccompanimentConcepts;
import tools.ensemble.ontologies.composer.vocabulary.concepts.IntroConcepts;
import tools.ensemble.ontologies.composer.vocabulary.concepts.SoloConcepts;
import tools.ensemble.ontologies.musicelements.MusicElementsOntology;
import tools.ensemble.ontologies.musicians.MusicianOntology;
import tools.ensemble.ontologies.timemanager.TimeHandler;

/**
 * Created by OscarAlfonso on 2/22/2017.
 */
public class Composer extends Agent implements MusicianStates,DataStorteMusicians,ComposerStatesNames,DataStoreComposer, JMC {

    //The  Ontologies
    private Codec codec = new SLCodec();
    private Ontology ontology = MusicElementsOntology.getInstance();
    private Ontology musicianOntology = MusicianOntology.getInstance();
    private Ontology timeHandlerOntology = TimeHandler.getInstance();
    private Ontology composerOntology = ComposerOntology.getInstance();
    // The Internal Time Manager
    private AID internalTimeManager = new AID();

    //Rules to be used during composing and performing
    public static int holdComposition = 0;
    public static synchronized void setHoldComposition(int holdCompo)
    {
        holdComposition = holdCompo;
    }

    public static synchronized int getHoldComposition()
    {
        return holdComposition;
    }

    public static int holdPlay = 0;
    public static synchronized void setHoldPlay(int holdplay)
    {
        holdPlay =holdplay;
    }
    public static synchronized int getHoldPlay()
    {
        return holdPlay;
    }

    public static int NextsectionIndex;
    public static synchronized void setNextSectionIndex(int secIndex)
    {
        NextsectionIndex = secIndex;
    }
    public static synchronized int getNextsectionIndex()
    {
        return NextsectionIndex;
    }
    public static Character NextsectionCharacter;
    public static synchronized void  setNextsectionCharacter(Character nextSecChar)
    {
        NextsectionCharacter = nextSecChar;
    }
    public static synchronized Character getNextsectionCharacter()
    {
        return NextsectionCharacter;
    }
    public static Long sectionPlayLeft;

    public static synchronized void setSectionPlayLeft(Long playleft)
    {
        sectionPlayLeft = playleft;
    }

    public static synchronized Long getSectionPlayLeft()
    {
        return sectionPlayLeft;
    }
    public static int measureCounter = 0;
    public static synchronized void setMeasureCounter(int measure)
    {
        measureCounter = measure;
    }
    public static synchronized int getMeasureCounter()
    {
        return measureCounter;
    }
    public static synchronized void incrementMeasureCounter()
    {
        measureCounter++;
    }
    public static int firstTimePlayingSolo = 0;
    public static synchronized void setFirstTimePlayingSolo(int firstTime)
    {
        firstTimePlayingSolo = firstTime;
    }
    public static synchronized int getFirstTimePlayingSolo()
    {
        return firstTimePlayingSolo;
    }
    public static int holdSoloComposition = 0;
    public static synchronized void setHodSoloComposition(int solocomp)
    {
        holdSoloComposition = solocomp;
    }
    public static synchronized int getHoldSoloComposition()
    {
        return holdSoloComposition;
    }
    public static int holdSoloPlayback = 0;
    public static synchronized void setHoldSoloPlayback(int playback){
        holdSoloPlayback = playback;
    }
    public static synchronized int getHoldSoloPlayback()
    {
        return holdSoloPlayback;
    }
    public static int NextSectionSoloIndex = 0;
    public static synchronized void setNextSectionSoloIndex(int soloIndex)
    {
        NextSectionSoloIndex = soloIndex;
    }
    public static synchronized int getNextSectionSoloIndex()
    {
        return NextSectionSoloIndex;
    }
    public static Character NextSectionSoloCharacter = 0;
    public static synchronized void setNextSectionSoloCharacter(Character c)
    {
        NextSectionSoloCharacter = c;
    }
    public static synchronized Character getNextSectionSoloCharacter()
    {
        return NextSectionSoloCharacter;
    }
    public static Long timeLeftInCurrentsection = null;
    public static synchronized void setTimeLeftInCurrentsection(Long time)
    {
        timeLeftInCurrentsection = time;
    }
    public static synchronized Long getTimeLeftInCurrentsection()
    {
        return timeLeftInCurrentsection;
    }
    public static Score SoloSaxScore = new Score("Sax Solo Score");
    public static synchronized void setSoloScore(Score s)
    {
        SoloSaxScore = s;
    }
    public static synchronized Score getSoloScore()
    {
        return SoloSaxScore;
    }

    public static Score accompanimentScore = new Score("Accompaniment Piano");
    public static synchronized void setAccompanimentScore(Score as)
    {
        accompanimentScore = as;
    }
    public static synchronized Score getAccompanimentScore()
    {
        return accompanimentScore;
    }

    public static PreviousSection previousSection = new PreviousSection();
    public static synchronized void setPreviousSection(PreviousSection p)
    {
        previousSection = p;
    }
    public static synchronized PreviousSection getPreviousSection()
    {
        return previousSection;
    }

    public static int firstTime = 0;

    //Finite state machines objects declaration
    FSMBehaviour introFSM;
    FSMBehaviour soloFSM;
    FSMBehaviour accompaniementFSM;



    //The instance of the objects on the ontology
    public IntroConcepts introConceptsIntance = new IntroConcepts();
    public SoloConcepts soloConceptsInstance = new SoloConcepts();
    public AccompanimentConcepts accConcept = new AccompanimentConcepts();
    //Score for the Solo



    protected void setup()
    {
        //run the midi
        Play.midi(new Score(),false,false,1,1);

        //

        //Register the ontologies.
        registerLanguagesAndOntologies();

        //get the synchronizer agent
        getMySynchronizerID();
        //Register the service to the DF.
        registerServiceToDF();
        //Set intro FSM
        setIntroFSM();
        //Set SoloFSM
        setSoloFSM();
        //set accompanimentFSM
        setAccompanimentFSM();



    }

    protected void takeDown()
    {
        System.out.println("Bye ");

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

    private void getMySynchronizerID()
    {
        //Find the internal TimeManager Agent
        DFAgentDescription template_2 = new DFAgentDescription();
        ServiceDescription sd_2 = new ServiceDescription();
        sd_2.setType("InternalTimeManager");
        try {
            sd_2.setOwnership(getContainerController().getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }
        template_2.addServices(sd_2);
        //Now get the internal time manager
        DFAgentDescription[] resultSearchTimeManager = new DFAgentDescription[0];
        try {
            resultSearchTimeManager = DFService.search(this,template_2);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        for (int i=0; i<resultSearchTimeManager.length; i++)
        {
            internalTimeManager = resultSearchTimeManager[i].getName();
        }

    }


    //Register Services to the DF
    private void registerServiceToDF()
    {
        //Register a service for the musician living in the same container agent in the yellow pages.
        ServiceDescription sdInternal = new ServiceDescription();
        sdInternal.setType("InternalComposer");
        sdInternal.setName(getLocalName()+"-Composer");
        try {
            sdInternal.setOwnership(getContainerController().getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }
        //Add the services
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sdInternal);
       //Register the services
        try {
            DFService.register(this,dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }




    //Function for set the intro Finite state machine.
    private void setIntroFSM()
    {
        introFSM = new FSMBehaviour(this);
        introFSM.getDataStore().put(INTRO_COMPOSER_INSTANCE,introConceptsIntance);
        introFSM.getDataStore().put(COMPOSER_MY_INTERNAL_SYNCHRONIZER,internalTimeManager);
        //Register the first state
        //Get the instance of the bahaviour
        ExpectingIntroRequest expectingIntroRequest = new ExpectingIntroRequest(this,composerOntology,codec);
        expectingIntroRequest.setDataStore(introFSM.getDataStore());
        introFSM.registerFirstState(expectingIntroRequest,STATE_WAIT_FOR_INTRO_REQUEST);
        //Get the instance of the behavior
        ComposeIntro composeIntro = new ComposeIntro(this);
        composeIntro.setDataStore(introFSM.getDataStore());
        introFSM.registerState(composeIntro,STATE_COMPOSE_INTRO);
        //Get instance of the behaviour
        ComposerPlayIntro composerPlayIntro = new ComposerPlayIntro(this);
        composerPlayIntro.setDataStore(introFSM.getDataStore());
        introFSM.registerState(composerPlayIntro,STATE_PLAY_INTRO);
        introFSM.registerLastState(new ComposerEndIntro(this),STATE_END_INTRO);

        // REGISTER TRANSITIONS

        introFSM.registerTransition(STATE_WAIT_FOR_INTRO_REQUEST,STATE_WAIT_FOR_INTRO_REQUEST,0);
        introFSM.registerTransition(STATE_WAIT_FOR_INTRO_REQUEST,STATE_COMPOSE_INTRO,1);
        introFSM.registerTransition(STATE_COMPOSE_INTRO,STATE_COMPOSE_INTRO,2);
        introFSM.registerTransition(STATE_COMPOSE_INTRO,STATE_PLAY_INTRO,3);
        introFSM.registerTransition(STATE_PLAY_INTRO,STATE_PLAY_INTRO,4);
        introFSM.registerTransition(STATE_PLAY_INTRO,STATE_END_INTRO,5);


        // Add the IntroFSM to the agent behaviour

        addBehaviour(introFSM);



    }

    //Function for set the solo Finite state machine
    private void setSoloFSM()
    {
        soloFSM = new FSMBehaviour(this);
        soloFSM.getDataStore().put(SOLO_COMPOSER_INSTANCE,soloConceptsInstance);
        soloFSM.getDataStore().put(COMPOSER_MY_INTERNAL_SYNCHRONIZER,internalTimeManager);

        ResponseSoloRequest responseSoloRequest = new ResponseSoloRequest(this,timeHandlerOntology,codec);
        responseSoloRequest.setDataStore(soloFSM.getDataStore());
        soloFSM.registerFirstState(responseSoloRequest,STATE_WAIT_FOR_SOLO_REQUEST);

        RequestInfoSectionToSyn requestInfoSectionToSyn = new RequestInfoSectionToSyn(this);
        requestInfoSectionToSyn.setDataStore(soloFSM.getDataStore());
        soloFSM.registerState(requestInfoSectionToSyn,STATE_REQUEST_INFO_SECTION);

        GetInfoSectionFromSyn getInfoSectionFromSyn = new GetInfoSectionFromSyn(this);
        getInfoSectionFromSyn.setDataStore(soloFSM.getDataStore());
        soloFSM.registerState(getInfoSectionFromSyn,STATE_GET_INFO_SECTION);

        ConfirmToMusician confirm = new ConfirmToMusician(this);
        confirm.setDataStore(soloFSM.getDataStore());
        soloFSM.registerState(confirm,STATE_CONFIRM_TO_MUSICIAN);

        ComposeSoloBehaviour composeSoloBehaviour = new ComposeSoloBehaviour(this);
        composeSoloBehaviour.setDataStore(soloFSM.getDataStore());
        soloFSM.registerState(composeSoloBehaviour,STATE_COMPOSE_SOLO);

        PlaySoloBehaviour playSoloBehaviour = new PlaySoloBehaviour(this,timeHandlerOntology,codec);
        playSoloBehaviour.setDataStore(soloFSM.getDataStore());
        soloFSM.registerState(playSoloBehaviour,STATE_PLAY_SOLO);
        soloFSM.registerLastState(new TemporaryBehaviour(),STATE_END_SOLO);

        //Register Transitions

        soloFSM.registerTransition(STATE_WAIT_FOR_SOLO_REQUEST,STATE_WAIT_FOR_SOLO_REQUEST,0);
        soloFSM.registerTransition(STATE_WAIT_FOR_SOLO_REQUEST,STATE_REQUEST_INFO_SECTION,1);
        soloFSM.registerTransition(STATE_REQUEST_INFO_SECTION,STATE_REQUEST_INFO_SECTION,2);
        soloFSM.registerTransition(STATE_REQUEST_INFO_SECTION,STATE_GET_INFO_SECTION,3);
        soloFSM.registerTransition(STATE_GET_INFO_SECTION,STATE_GET_INFO_SECTION,4);
        soloFSM.registerTransition(STATE_GET_INFO_SECTION,STATE_CONFIRM_TO_MUSICIAN,5);
        soloFSM.registerTransition(STATE_CONFIRM_TO_MUSICIAN,STATE_CONFIRM_TO_MUSICIAN,6);
        soloFSM.registerTransition(STATE_CONFIRM_TO_MUSICIAN,STATE_COMPOSE_SOLO,7);
        soloFSM.registerTransition(STATE_COMPOSE_SOLO,STATE_COMPOSE_SOLO,8);
        soloFSM.registerTransition(STATE_COMPOSE_SOLO,STATE_WAIT_FOR_SOLO_REQUEST,18);
        //soloFSM.registerTransition(STATE_WAIT_FOR_SOLO_REQUEST,STATE_COMPOSE_SOLO,1);
        //soloFSM.registerTransition(STATE_COMPOSE_SOLO,STATE_COMPOSE_SOLO,20);
        soloFSM.registerTransition(STATE_COMPOSE_SOLO,STATE_PLAY_SOLO,9);
        soloFSM.registerTransition(STATE_PLAY_SOLO,STATE_PLAY_SOLO,10);
        soloFSM.registerTransition(STATE_PLAY_SOLO,STATE_COMPOSE_SOLO,12);
        soloFSM.registerTransition(STATE_PLAY_SOLO,STATE_REQUEST_INFO_SECTION,13);
        soloFSM.registerTransition(STATE_PLAY_SOLO,STATE_WAIT_FOR_SOLO_REQUEST,17);
        soloFSM.registerTransition(STATE_COMPOSE_SOLO,STATE_WAIT_FOR_SOLO_REQUEST,14);
        soloFSM.registerTransition(STATE_PLAY_SOLO,STATE_COMPOSE_SOLO,15);
        soloFSM.registerTransition(STATE_PLAY_SOLO,STATE_END_SOLO,16);

        //example transition
        //soloFSM.registerTransition(STATE_CONFIRM_TO_MUSICIAN,STATE_END_SOLO,17);

        // Add the SoloFSM to the agent behaviour
        addBehaviour(soloFSM);

    }

    //Function for set the accompaniment Finite state machine
    private void setAccompanimentFSM()
    {
        accompaniementFSM = new FSMBehaviour(this);
        accompaniementFSM.getDataStore().put(ACCOMPANIMENT_COMPOSER_INSTANCE,accConcept);
        accompaniementFSM.getDataStore().put(COMPOSER_MY_INTERNAL_SYNCHRONIZER,internalTimeManager);
        //Instance of the Behaviour
        ResponseAccompanimentRequest responseAccompanimentRequest = new ResponseAccompanimentRequest(this,introFSM);
        responseAccompanimentRequest.setDataStore(accompaniementFSM.getDataStore());
        accompaniementFSM.registerFirstState(responseAccompanimentRequest,STATE_WAIT_FOR_ACCOMP_REQUEST);
        //Instance confirm composition
        ConfirmComposeAccompaniment confirm = new ConfirmComposeAccompaniment(this);
        confirm.setDataStore(accompaniementFSM.getDataStore());
        accompaniementFSM.registerState(confirm,STATE_CONFIRM_COMPOSIION);
        //Instance of the behaviour compose accompaniment
        ComposeAccompanimentBehaviour CAB = new ComposeAccompanimentBehaviour(this);
        CAB.setDataStore(accompaniementFSM.getDataStore());
        accompaniementFSM.registerState(CAB,STATE_COMPOSE_ACCOMP);
        PlayAccompanimentBehaviour playAccompanimentBehaviour = new PlayAccompanimentBehaviour(this,timeHandlerOntology,codec);
        playAccompanimentBehaviour.setDataStore(accompaniementFSM.getDataStore());
        accompaniementFSM.registerState(playAccompanimentBehaviour,STATE_PLAY_ACCOMP);
        //Request Info Section to Sync
        RequestInfoSectionToSync requestInfoToSync = new RequestInfoSectionToSync(this);
        requestInfoToSync.setDataStore(accompaniementFSM.getDataStore());
        accompaniementFSM.registerState(requestInfoToSync,STATE_REQUEST_INFO_TO_SYNC);

        GetInfoSectionFromSync getInfoFromSynC = new GetInfoSectionFromSync(this);
        getInfoFromSynC.setDataStore(accompaniementFSM.getDataStore());
        accompaniementFSM.registerState(getInfoFromSynC,STATE_GET_INFO_FROM_SYNC);

        accompaniementFSM.registerLastState(new TemporaryBehaviour(),STATE_END_ACCOMP);


        // Transitions

        accompaniementFSM.registerTransition(STATE_WAIT_FOR_ACCOMP_REQUEST,STATE_WAIT_FOR_ACCOMP_REQUEST,0);
        accompaniementFSM.registerTransition(STATE_WAIT_FOR_ACCOMP_REQUEST,STATE_CONFIRM_COMPOSIION,9);
        accompaniementFSM.registerTransition(STATE_WAIT_FOR_ACCOMP_REQUEST,STATE_REQUEST_INFO_TO_SYNC,12);
        accompaniementFSM.registerTransition(STATE_REQUEST_INFO_TO_SYNC,STATE_GET_INFO_FROM_SYNC,13);
        accompaniementFSM.registerTransition(STATE_GET_INFO_FROM_SYNC,STATE_GET_INFO_FROM_SYNC,14);
        accompaniementFSM.registerTransition(STATE_GET_INFO_FROM_SYNC,STATE_COMPOSE_ACCOMP,15);
        accompaniementFSM.registerTransition(STATE_CONFIRM_COMPOSIION,STATE_CONFIRM_COMPOSIION,8);
        accompaniementFSM.registerTransition(STATE_CONFIRM_COMPOSIION,STATE_COMPOSE_ACCOMP,1);
        //accompaniementFSM.registerTransition(STATE_WAIT_FOR_ACCOMP_REQUEST,STATE_COMPOSE_ACCOMP,1);
        accompaniementFSM.registerTransition(STATE_COMPOSE_ACCOMP,STATE_COMPOSE_ACCOMP,2);
        accompaniementFSM.registerTransition(STATE_COMPOSE_ACCOMP,STATE_WAIT_FOR_ACCOMP_REQUEST,3);
        accompaniementFSM.registerTransition(STATE_COMPOSE_ACCOMP,STATE_PLAY_ACCOMP,4);
        accompaniementFSM.registerTransition(STATE_PLAY_ACCOMP,STATE_PLAY_ACCOMP,5);
        accompaniementFSM.registerTransition(STATE_PLAY_ACCOMP,STATE_COMPOSE_ACCOMP,6);
        accompaniementFSM.registerTransition(STATE_PLAY_ACCOMP,STATE_END_ACCOMP,7);
        accompaniementFSM.registerTransition(STATE_PLAY_ACCOMP,STATE_WAIT_FOR_ACCOMP_REQUEST,11);
        accompaniementFSM.registerTransition(STATE_PLAY_ACCOMP,STATE_REQUEST_INFO_TO_SYNC,17);



        //example transition
        accompaniementFSM.registerTransition(STATE_COMPOSE_ACCOMP,STATE_END_ACCOMP,10);

        //Add the AccompaniementFSM to the agent behaviour
        addBehaviour(accompaniementFSM);
    }

    private class TemporaryBehaviour extends OneShotBehaviour {
               public void action(){
            System.out.println("composer behaviour and bye"+getBehaviourName());


        }
        public int onEnd()
        {

            return 10;
        }



    }

}
