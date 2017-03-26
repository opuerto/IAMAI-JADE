package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.ControllerException;
import tools.ensemble.agents.Musician;
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
    // The Internal Time Manager
    private AID internalTimeManager = new AID();
    //Internal Composer
    private AID internalComposer = new AID();
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

        //Find the internal TimeManager Agent
        DFAgentDescription template_2 = new DFAgentDescription();
        ServiceDescription sd_2 = new ServiceDescription();
        sd_2.setType("InternalTimeManager");
        try {
            sd_2.setOwnership(agent.getContainerController().getContainerName());
        } catch (ControllerException e) {
            e.printStackTrace();
        }
        template_2.addServices(sd_2);

        //Find the internal Composer
        DFAgentDescription template_3 = new DFAgentDescription();
        ServiceDescription sd_3 = new ServiceDescription();
        sd_3.setType("InternalComposer");
        try{
            sd_3.setOwnership(agent.getContainerController().getContainerName());
        }
        catch(ControllerException e)
        {
            e.printStackTrace();
        }
        template_3.addServices(sd_3);


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

            //Now get the internal time manager
            DFAgentDescription[] resultSearchTimeManager = DFService.search(myAgent,template_2);
            for (int i=0; i<resultSearchTimeManager.length; i++)
            {
                internalTimeManager = resultSearchTimeManager[i].getName();
            }
            getDataStore().put(INTERNAL_TIME_MANAGER,internalTimeManager);

            //Now get the internal Composer
            DFAgentDescription[] resultSearchComposer = DFService.search(myAgent,template_3);
            for (int i=0; i<resultSearchComposer.length; i++)
            {
                internalComposer = resultSearchComposer[i].getName();
                System.out.println("the internal composer found was "+internalComposer);
            }
            getDataStore().put(INTERNAL_COMPOSER,internalComposer);



        }catch (FIPAException fe)
        {
            fe.printStackTrace();
        }





        if(Musicians.isEmpty() || Musicians.size() < 2) //here was menor que 4
        {
            transition = 0;
        }
        else if(internalComposer.getLocalName().equals(""))
        {
            transition = 0;
        }
        else if(internalTimeManager.getLocalName().equals(""))
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
        if(transition == 0)
        {
            block(500);
        }
        return transition;
    } //Exit with the transition value to the corresponding state.
}
