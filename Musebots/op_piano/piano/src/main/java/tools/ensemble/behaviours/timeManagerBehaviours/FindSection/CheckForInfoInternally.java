package tools.ensemble.behaviours.timeManagerBehaviours.FindSection;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Created by OscarAlfonso on 3/25/2017.
 */
public class CheckForInfoInternally extends OneShotBehaviour {

    public CheckForInfoInternally(Agent a, Ontology ont, Codec lan)
    {
        super(a);
    }

    public void action()
    {

    }

    public int onEnd()
    {
        return 1;
    }
}
