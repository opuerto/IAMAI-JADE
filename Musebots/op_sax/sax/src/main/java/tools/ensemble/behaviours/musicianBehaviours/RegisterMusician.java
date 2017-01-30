package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.ControllerException;

/**
 * Created by OscarAlfonso on 1/18/2017.
 * This class will represent the state RegisterMusician
 * and will register the agent to the yellow pages
 */
public class RegisterMusician extends OneShotBehaviour {
   //Save the agent instance
    private Agent agent;
    public RegisterMusician(Agent a)
    {
        super(a);
        this.agent = a;
    }

    public void action()
    {
        //Register Service for the rest of the musician in the platform
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Musician");
        sd.setName(agent.getLocalName()+"-musician");

        //Register Service for the Internal Time Manager
        ServiceDescription sd_2 = new ServiceDescription();
        sd_2.setType("interact-internal-time-manager");
        sd_2.setName(agent.getName());
        try {
            sd_2.setOwnership(agent.getContainerController().getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }

        //Register the agent to the yellow pages.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());
        dfd.addServices(sd);
        dfd.addServices(sd_2);
        try {
            DFService.register(agent,dfd);
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }

        System.out.println("I just registered the agent "+agent.getAID()+" to the yellow pages ");
    }
}
