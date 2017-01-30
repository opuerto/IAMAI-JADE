package tools.ensemble.behaviours.timeManagerBehaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.ControllerException;

/**
 * Created by OscarAlfonso on 1/30/2017.
 */
public class FindInternalMusician extends Behaviour {

    private Agent agent;
    private AID myMusician = null;

    public FindInternalMusician(Agent a)
    {
        super(a);
        this.agent = a;
    }
    public void action()
    {
        //Find the time managers in the platform
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("interact-internal-time-manager");
        try {
            sd.setOwnership(myAgent.getContainerController().getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }
        template.addServices(sd);

        try
        {
            DFAgentDescription[] result = DFService.search(myAgent,template);
            for(int i = 0; i<result.length; i++)
            {
                myMusician = result[i].getName();
            }
        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }
        System.out.println("my musician: "+myMusician);


    }

    public boolean done()
    {
        if (myMusician != null)
        {
            return true;
        }
        return false;

    }
}
