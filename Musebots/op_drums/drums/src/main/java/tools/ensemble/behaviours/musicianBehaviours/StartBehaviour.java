package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Created by OscarAlfonso on 1/18/2017.
 */
public class StartBehaviour extends OneShotBehaviour {

    public StartBehaviour(Agent a)
    {
        super(a);
    }

    public void action()
    {
        System.out.println("Agent "+myAgent.getAID().getLocalName()+ " is starting");
    }
}
