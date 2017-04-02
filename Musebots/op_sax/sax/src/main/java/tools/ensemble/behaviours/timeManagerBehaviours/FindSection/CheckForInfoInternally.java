package tools.ensemble.behaviours.timeManagerBehaviours.FindSection;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import tools.ensemble.agents.Musician;
import tools.ensemble.agents.TimeManager;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;

/**
 * Created by OscarAlfonso on 3/25/2017.
 */
public class CheckForInfoInternally extends OneShotBehaviour implements DataStoreTimeManager {

    private int transition = 5;
    //private int firstTimeHere = 0;
    private Ontology synOntology;
    private Codec language;
    private Section section;
    private AID internalComposer;

    public CheckForInfoInternally(Agent a, Ontology ont, Codec lan)
    {

        super(a);
        this.synOntology = ont;
        this.language = lan;
    }

    public void onStart()
    {
        transition = 5;

        Musician.setLeader(true);

        if (getDataStore().containsKey(INTERNAL_COMPOSER_IN_SYN))
        {
            internalComposer = (AID) getDataStore().get(INTERNAL_COMPOSER_IN_SYN);

        }
        if (getDataStore().containsKey(SECTION_INSTANCE))
        {
            section = (Section) getDataStore().get(SECTION_INSTANCE);
            ACLMessage sendInfoToComposer = new ACLMessage(ACLMessage.CONFIRM);
            sendInfoToComposer.setConversationId("Inform-the-composer-the-currentSection");
            sendInfoToComposer.setReplyWith(myAgent.getLocalName()+System.currentTimeMillis());
            sendInfoToComposer.setOntology(synOntology.getName());
            sendInfoToComposer.setLanguage(language.getName());
            try
            {
                //fill the content using the Ontology concept
                myAgent.getContentManager().fillContent(sendInfoToComposer,new Action((AID)internalComposer,section));
            }catch (Exception ex) { ex.printStackTrace(); }
            sendInfoToComposer.addReceiver(internalComposer);
            myAgent.send(sendInfoToComposer);
            transition = 6;
        }

    }
    public void action()
    {


    }

    public int onEnd()
    {
        if (transition == 5)
        {
            block(100);
        }

        return transition;
    }
}
