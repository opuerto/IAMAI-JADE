package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import tools.ensemble.interfaces.DataStorteMusicians;

import java.util.Vector;

/**
 * Created by OscarAlfonso on 1/18/2017.
 */
public class GetMembers extends OneShotBehaviour implements DataStorteMusicians {

    private Agent agent;
    private Vector Musicians = new Vector();
    private int transition;
    private boolean leader;
    private boolean accompanient;
    //public static final String MUSICIAN_LIST = "musicianList";
    //public static final String FIRST_LEADER = "firstLeader";

    public GetMembers(Agent a,boolean leader,boolean accompanient)
    {
        super(a);
        this.agent = a;
        this.leader = leader;
        this.accompanient = accompanient;

    }

    public void action()
    {
        // Find the musicians that provide accompaniment service
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Musician");
        template.addServices(sd);
        try
        {
            DFAgentDescription[] result = DFService.search(myAgent,template);
            Musicians.clear();
            if(getDataStore().containsKey(MUSICIAN_LIST))
            {
                getDataStore().remove(MUSICIAN_LIST);
            }
            for (int i=0; i<result.length; i++)
            {
                //Store the list in the vectors
                Musicians.addElement(result[i].getName());
            }
            //Save the vector of musicians into the dataStore in order to be use among the rest of the behaviours.

            getDataStore().put(MUSICIAN_LIST,Musicians);


        }catch (FIPAException fe)
        {
            fe.printStackTrace();
        }
        if(Musicians.isEmpty() || Musicians.size() < 4)
        {
            transition = 0;
        }
        else if(leader)
        {
            //Setting a flag that we will use in the next state, so we can know if this is the first agent been leader.
            getDataStore().put(FIRST_LEADER,leader);
            transition = 1;


        }
        else if(accompanient)
        {
            transition = 2;
        }

    }
    public int onEnd() {
        return transition;
    } //Exit with the transition value to the corresponding state.
}
