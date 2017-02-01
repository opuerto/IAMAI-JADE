package tools.ensemble.agents;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.ControllerException;
import jm.music.data.Score;
import jm.util.Play;
import tools.ensemble.behaviours.timeManagerBehaviours.FindInternalMusician;
import tools.ensemble.behaviours.timeManagerBehaviours.GetEveryTimeManager;
import tools.ensemble.behaviours.timeManagerBehaviours.GetInfoIntro;
import tools.ensemble.behaviours.timeManagerBehaviours.Prueba;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.ontologies.timemanager.TimeHandler;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Chorus;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Intro;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Song;


/**
 * Created by OscarAlfonso on 1/29/2017.
 */
public class TimeManager extends Agent implements DataStoreTimeManager {

    private Codec codec = new SLCodec();
    private Ontology timeHandlerOntology = TimeHandler.getInstance();

    //Get instances of the concepts
    private Chorus dataChorus = new Chorus();
    private Intro dataIntro = new Intro();
    private Song dataSong = new Song();

    protected void setup()
    {

        //Register the services.
        RegisterTheServices();

        //Register Languages and Ontologies
        registerLanguageAndOntology();

        //Register the parallel behaviour
        registerParallelBehaviour();

    }

    protected void takeDown()
    {
        System.out.println("Bye ");

    }



    private void registerLanguageAndOntology()
    {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(timeHandlerOntology);
    }


    private void registerParallelBehaviour()
    {
        //Create instance of the parallel behaviour
        ParallelBehaviour pb = new ParallelBehaviour(this,ParallelBehaviour.WHEN_ALL);
        //Set the concepts in the data store of the parallel behaviour
        pb.getDataStore().put(CHORUS_INSTANCE,dataChorus);
        pb.getDataStore().put(INTRO_INSTANCE,dataIntro);
        pb.getDataStore().put(SONG_INSTANCE,dataSong);
        //Create instance of the getTimerList
        GetEveryTimeManager getTimerList = new GetEveryTimeManager(this);
        //Share the data store
        getTimerList.setDataStore(pb.getDataStore());
        //ad the behaviour to the parallel behaviour
        pb.addSubBehaviour(getTimerList);
        //Create instances of the behaviour
        FindInternalMusician findMymusician = new FindInternalMusician(this);
        //Share the data store
        findMymusician.setDataStore(pb.getDataStore());
        //Add subbehaviour to the parallel behaviour
        pb.addSubBehaviour(findMymusician);
        //Create instance of the get Info Intro behaviour
        GetInfoIntro getInfoIntro = new GetInfoIntro();
        //Share the data store
        getInfoIntro.setDataStore(pb.getDataStore());
        //Add the subBehaviour
        pb.addSubBehaviour(getInfoIntro);


        addBehaviour(pb);

    }

    /**
     * This method register the services of this agent
     *
     */

    private void RegisterTheServices()
    {
        //Register a service for the musician living in the same container agent in the yellow pages.
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("InternalTimeManager");
        sd1.setName(getLocalName()+"-TimeManager");
        try {
            sd1.setOwnership(getContainerController().getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }

        //Register a service for hold interaction with the rest of the Time Manager agents.
        ServiceDescription sd2 = new ServiceDescription();
        sd2.setType("ExternalTimeManager");
        sd2.setName(getLocalName()+"-ExternalTimeManager");

        //Add the services
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd1);
        dfd.addServices(sd2);

        //Register the services
        try {
            DFService.register(this,dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
