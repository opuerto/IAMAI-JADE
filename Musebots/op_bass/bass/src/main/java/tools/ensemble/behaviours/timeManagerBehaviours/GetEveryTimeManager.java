package tools.ensemble.behaviours.timeManagerBehaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import tools.ensemble.interfaces.DataStoreTimeManager;

import java.util.Vector;

/**
 * Created by OscarAlfonso on 1/29/2017.
 */

public class GetEveryTimeManager extends Behaviour implements DataStoreTimeManager {

    private Agent agent;
    private Vector externalTimeManagerList = new Vector();

    private int listLength = 1;

    public GetEveryTimeManager(Agent a)
    {
        super(a);
        this.agent = a;
    }

    public void action()
    {
        //Find the time managers in the platform
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ExternalTimeManager");
        template.addServices(sd);

        try
        {
            DFAgentDescription[] result = DFService.search(myAgent,template);
            externalTimeManagerList.clear();
            if(getDataStore().containsKey(TIME_MANAGER_LIST))
            {
                getDataStore().remove(TIME_MANAGER_LIST);
            }

            for(int i = 0; i < result.length; i++)
            {


                    //Store the list of time managers
                if(!result[i].getName().equals(myAgent.getAID()))
                {
                    externalTimeManagerList.addElement(result[i].getName());
                }



            }

            //Save the list in the data store
            getDataStore().put(TIME_MANAGER_LIST,externalTimeManagerList);

        }
        catch (FIPAException fe)
        {
            fe.printStackTrace();
        }

    }

    public boolean done()
    {
        if(externalTimeManagerList.size() == listLength)
        {
            return true;
        }
        else
        {
            block(500);
            return false;
        }



    }

}
