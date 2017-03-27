package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Created by OscarAlfonso on 3/27/2017.
 */
public class WaitToGetLead extends OneShotBehaviour {

    private int transition;
    private int firstTimeHere = 0;

    public WaitToGetLead(Agent a)
    {
        super(a);
    }

    public void action()
    {
        System.out.println("Wait For lead ");
    }

    public int onEnd()
    {
        firstTimeHere++;
        return transition;
    }
}
