package tools.ensemble.agents;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.ControllerException;
import jm.music.data.Score;
import jm.util.Play;
import tools.ensemble.behaviours.ComposerBhaviours.accompaniment.ComposeAccompanimentBehaviour;
import tools.ensemble.behaviours.ComposerBhaviours.accompaniment.PlayAccompanimentBehaviour;
import tools.ensemble.behaviours.ComposerBhaviours.accompaniment.ResponseAccompanimentRequest;
import tools.ensemble.behaviours.ComposerBhaviours.intro.ComposeIntro;
import tools.ensemble.behaviours.ComposerBhaviours.intro.ComposerEndIntro;
import tools.ensemble.behaviours.ComposerBhaviours.intro.ComposerPlayIntro;
import tools.ensemble.behaviours.ComposerBhaviours.intro.ExpectingIntroRequest;
import tools.ensemble.interfaces.ComposerStatesNames;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.interfaces.DataStorteMusicians;
import tools.ensemble.interfaces.MusicianStates;
import tools.ensemble.ontologies.composer.ComposerOntology;
import tools.ensemble.ontologies.composer.vocabulary.concepts.AccompanimentConcepts;
import tools.ensemble.ontologies.composer.vocabulary.concepts.IntroConcepts;
import tools.ensemble.ontologies.composer.vocabulary.concepts.SoloConcepts;
import tools.ensemble.ontologies.musicelements.MusicElementsOntology;
import tools.ensemble.ontologies.musicians.MusicianOntology;
import tools.ensemble.ontologies.timemanager.TimeHandler;

/**
 * Created by OscarAlfonso on 2/22/2017.
 */
public class Composer extends Agent implements MusicianStates,DataStorteMusicians,ComposerStatesNames,DataStoreComposer {

    //The  Ontologies
    private Codec codec = new SLCodec();
    private Ontology ontology = MusicElementsOntology.getInstance();
    private Ontology musicianOntology = MusicianOntology.getInstance();
    private Ontology timeHandlerOntology = TimeHandler.getInstance();
    private Ontology composerOntology = ComposerOntology.getInstance();

    //Finite state machines objects declaration
    FSMBehaviour introFSM;
    FSMBehaviour soloFSM;
    FSMBehaviour accompaniementFSM;

    //The instance of the objects on the ontology
    public IntroConcepts introConceptsIntance = new IntroConcepts();
    public SoloConcepts soloConceptsInstance = new SoloConcepts();
    public AccompanimentConcepts accConcept = new AccompanimentConcepts();

    protected void setup()
    {
        //run the midi
        Play.midi(new Score(),false,false,3,0);

        //

        //Register the ontologies.
        registerLanguagesAndOntologies();
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
        soloFSM.registerFirstState(new TemporaryBehaviour(),STATE_WAIT_FOR_SOLO_REQUEST);
        soloFSM.registerState(new TemporaryBehaviour(),STATE_COMPOSE_SOLO);
        soloFSM.registerState(new TemporaryBehaviour(),STATE_PLAY_SOLO);
        soloFSM.registerLastState(new TemporaryBehaviour(),STATE_END_SOLO);

        //Register Transitions

        soloFSM.registerTransition(STATE_WAIT_FOR_SOLO_REQUEST,STATE_WAIT_FOR_SOLO_REQUEST,0);
        soloFSM.registerTransition(STATE_WAIT_FOR_SOLO_REQUEST,STATE_COMPOSE_SOLO,1);
        soloFSM.registerTransition(STATE_COMPOSE_SOLO,STATE_COMPOSE_SOLO,2);
        soloFSM.registerTransition(STATE_COMPOSE_SOLO,STATE_PLAY_SOLO,3);
        soloFSM.registerTransition(STATE_COMPOSE_SOLO,STATE_WAIT_FOR_SOLO_REQUEST,4);
        soloFSM.registerTransition(STATE_PLAY_SOLO,STATE_COMPOSE_SOLO,5);
        soloFSM.registerTransition(STATE_PLAY_SOLO,STATE_END_SOLO,6);

        //example transition
        soloFSM.registerTransition(STATE_WAIT_FOR_SOLO_REQUEST,STATE_END_SOLO,10);

        // Add the SoloFSM to the agent behaviour
        addBehaviour(soloFSM);

    }

    //Function for set the accompaniment Finite state machine
    private void setAccompanimentFSM()
    {
        accompaniementFSM = new FSMBehaviour(this);
        accompaniementFSM.getDataStore().put(ACCOMPANIMENT_COMPOSER_INSTANCE,accConcept);
        //Instance of the Behaviour
        ResponseAccompanimentRequest responseAccompanimentRequest = new ResponseAccompanimentRequest(this);
        responseAccompanimentRequest.setDataStore(accompaniementFSM.getDataStore());
        accompaniementFSM.registerFirstState(responseAccompanimentRequest,STATE_WAIT_FOR_ACCOMP_REQUEST);

        //Instance of the behaviour compose accompaniment
        ComposeAccompanimentBehaviour CAB = new ComposeAccompanimentBehaviour(this);
        CAB.setDataStore(accompaniementFSM.getDataStore());
        accompaniementFSM.registerState(CAB,STATE_COMPOSE_ACCOMP);
        PlayAccompanimentBehaviour playAccompanimentBehaviour = new PlayAccompanimentBehaviour(this);
        playAccompanimentBehaviour.setDataStore(accompaniementFSM.getDataStore());
        accompaniementFSM.registerState(playAccompanimentBehaviour,STATE_PLAY_ACCOMP);
        accompaniementFSM.registerLastState(new TemporaryBehaviour(),STATE_END_ACCOMP);

        // Transitions

        accompaniementFSM.registerTransition(STATE_WAIT_FOR_ACCOMP_REQUEST,STATE_WAIT_FOR_ACCOMP_REQUEST,0);
        accompaniementFSM.registerTransition(STATE_WAIT_FOR_ACCOMP_REQUEST,STATE_COMPOSE_ACCOMP,1);
        accompaniementFSM.registerTransition(STATE_COMPOSE_ACCOMP,STATE_COMPOSE_ACCOMP,2);
        accompaniementFSM.registerTransition(STATE_COMPOSE_ACCOMP,STATE_WAIT_FOR_ACCOMP_REQUEST,3);
        accompaniementFSM.registerTransition(STATE_COMPOSE_ACCOMP,STATE_PLAY_ACCOMP,4);
        accompaniementFSM.registerTransition(STATE_PLAY_ACCOMP,STATE_PLAY_ACCOMP,5);
        accompaniementFSM.registerTransition(STATE_PLAY_ACCOMP,STATE_COMPOSE_ACCOMP,6);
        accompaniementFSM.registerTransition(STATE_PLAY_ACCOMP,STATE_END_ACCOMP,7);

        //example transition
        accompaniementFSM.registerTransition(STATE_COMPOSE_ACCOMP,STATE_END_ACCOMP,10);

        //Add the AccompaniementFSM to the agent behaviour
        addBehaviour(accompaniementFSM);
    }

    private class TemporaryBehaviour extends OneShotBehaviour {
               public void action(){
            System.out.println("composer behaviour "+getBehaviourName());


        }
        public int onEnd()
        {

            return 10;
        }



    }

}
