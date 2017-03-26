package tools.ensemble.behaviours.ComposerBhaviours.solo;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import tools.ensemble.behaviours.ComposerBhaviours.accompaniment.ComposeAccompanimentBehaviour;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;

/**
 * Created by OscarAlfonso on 3/26/2017.
 */
public class ComposeSoloBehaviour extends OneShotBehaviour implements DataStoreComposer {

    private int transition = 8;
    private int firstTimeHere = 0;
    private Section sectionInfo;

    public ComposeSoloBehaviour(Agent a)
    {
        super(a);
    }

    public void action()
    {
        if (firstTimeHere < 1)
        {
            System.out.println("IM in Compose solo");
            if(getDataStore().containsKey(SECTION_INSTANCE_FOR_SYN_SOLO))
            {
                sectionInfo = (Section) getDataStore().get(SECTION_INSTANCE_FOR_SYN_SOLO);
                System.out.println("current section is "+sectionInfo.getAccompanimentCurrentSection());
                System.out.println("current section time left "+sectionInfo.getTimeLeft().getTime());
            }
        }
    }

    public int onEnd()
    {
        firstTimeHere++;
        if (transition == 8)
        {
            block(500);
        }
        return transition;
    }

}
