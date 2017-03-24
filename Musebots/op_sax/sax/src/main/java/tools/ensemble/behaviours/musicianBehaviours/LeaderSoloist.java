package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import tools.ensemble.interfaces.DataStorteMusicians;

/**
 * Created by OscarAlfonso on 1/18/2017.
 * This class represent the behaviour of the State Leader
 */
public class LeaderSoloist extends OneShotBehaviour implements DataStorteMusicians {


    private int transition;

    public LeaderSoloist(Agent a)
    {
        super(a);
    }
    public void action()
    {

       //Check if this is the first agent been a leader of the ensemble
        if(getDataStore().containsKey(FIRST_LEADER))
        {
            System.out.println("I'm the first agent been a leader in the ensemble");
            transition = 3;
        }
        else
        {
            System.out.println("I'm going to play the solo");
            transition = 11;
        }




    }

    public int onEnd() {
        return transition;
    } //Exit with the transition value to the corresponding state.
}
