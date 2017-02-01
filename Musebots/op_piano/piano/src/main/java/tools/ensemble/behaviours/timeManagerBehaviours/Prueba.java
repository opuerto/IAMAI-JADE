package tools.ensemble.behaviours.timeManagerBehaviours;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Intro;

/**
 * Created by OscarAlfonso on 1/31/2017.
 */
public class Prueba extends TickerBehaviour implements DataStoreTimeManager
{
    public Prueba(Agent a)
    {
        super(a,10000);
    }

    public void onTick()
    {
        Intro intro = (Intro) getDataStore().get(INTRO_INSTANCE);
        System.out.println("intro instance length "+(long)intro.getIntroLenght());
        System.out.println("intro instance started at "+(long)intro.getIntroStartedAt());
    }

}
