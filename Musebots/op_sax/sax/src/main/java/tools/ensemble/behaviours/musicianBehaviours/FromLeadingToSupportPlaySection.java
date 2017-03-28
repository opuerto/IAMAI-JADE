package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import tools.ensemble.interfaces.DataStorteMusicians;

/**
 * Created by OscarAlfonso on 3/28/2017.
 */
public class FromLeadingToSupportPlaySection extends OneShotBehaviour implements DataStorteMusicians {

    private int transition = 3;
    private int firstTimeHere = 0;

    public FromLeadingToSupportPlaySection(Agent a)
    {
        super(a);
    }

    public void action()
    {
        if (firstTimeHere < 1)
        {
            System.out.println("Request Sections");
        }
    }

    public int onEnd()
    {
        return transition;
    }
}
