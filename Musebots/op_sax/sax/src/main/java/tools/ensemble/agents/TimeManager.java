package tools.ensemble.agents;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.ControllerException;
import jm.music.data.Score;
import jm.util.Play;
import tools.ensemble.behaviours.timeManagerBehaviours.GetEveryTimeManager;


/**
 * Created by OscarAlfonso on 1/29/2017.
 */
public class TimeManager extends Agent {

    //TODO Get the musician in the container

    protected void setup()
    {
        Play.midi(new Score(),false,false,1,0);
        //Register the services.
        RegisterTheServices();
        ParallelBehaviour pb = new ParallelBehaviour(this,ParallelBehaviour.WHEN_ALL);
        pb.addSubBehaviour(new GetEveryTimeManager(this));
        addBehaviour(pb);
    }

    protected void takeDown()
    {
        System.out.println("Bye ");

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
