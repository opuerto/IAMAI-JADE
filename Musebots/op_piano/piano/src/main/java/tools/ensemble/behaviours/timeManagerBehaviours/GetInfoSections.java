package tools.ensemble.behaviours.timeManagerBehaviours;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;

/**
 * Created by OscarAlfonso on 3/21/2017.
 */
public class GetInfoSections extends Behaviour implements DataStoreTimeManager {
    private MessageTemplate mt;
    private Section sectionInstance = null;
    public void action()
    {
        receiveMessage();
    }

    public boolean done()
    {
        return false;
    }

    public void receiveMessage()
    {
        mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId("update-syn-what-section-is-played"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        ACLMessage msg = myAgent.receive(mt);
        if(msg != null)
        {
            System.out.println("section in synchronizer");
            try {
                ContentElement content = myAgent.getContentManager().extractContent(msg);
                Concept concept = ((Action)content).getAction();
                if(concept instanceof Section)
                {
                    if(getDataStore().containsKey(SECTION_INSTANCE)) {
                        sectionInstance = (Section) getDataStore().get(SECTION_INSTANCE);
                        String s = ((Section) concept).getAccompanimentCurrentSection();
                        sectionInstance.setAccompanimentCurrentSection(s);
                        sectionInstance.setTimeLeft(((Section) concept).getTimeLeft());
                        sectionInstance.setSectionIndex(((Section) concept).getSectionIndex());
                        sectionInstance.setSectionStartedAt(((Section) concept).getSectionStartedAt());
                        getDataStore().remove(SECTION_INSTANCE);
                        getDataStore().put(SECTION_INSTANCE,sectionInstance);

                    }
                    else
                    {
                        sectionInstance = new Section();
                        String s = ((Section) concept).getAccompanimentCurrentSection();
                        sectionInstance.setAccompanimentCurrentSection(s);
                        sectionInstance.setTimeLeft(((Section) concept).getTimeLeft());
                        sectionInstance.setSectionIndex(((Section) concept).getSectionIndex());
                        sectionInstance.setSectionStartedAt(((Section) concept).getSectionStartedAt());
                        getDataStore().put(SECTION_INSTANCE,sectionInstance);


                    }

                }

                //Update section instance.

            } catch (Codec.CodecException e) {
                e.printStackTrace();
            } catch (OntologyException e) {
                e.printStackTrace();
            }
        }else{block();}
    }
}
