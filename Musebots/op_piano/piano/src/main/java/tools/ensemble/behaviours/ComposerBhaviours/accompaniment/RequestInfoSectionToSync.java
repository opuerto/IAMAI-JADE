package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Created by OscarAlfonso on 3/28/2017.
 */
public class RequestInfoSectionToSync extends OneShotBehaviour {

    private int transition;

    public RequestInfoSectionToSync(Agent a)
    {
        super(a);
    }

    public void action()
    {

    }

    public int onEnd()
    {
        return transition;
    }
}
