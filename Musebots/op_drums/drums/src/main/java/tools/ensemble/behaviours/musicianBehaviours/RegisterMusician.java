package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

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
        //Register the agent to the yellow pages.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Musician");
        sd.setName(agent.getLocalName()+"-musician");
        dfd.addServices(sd);
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
