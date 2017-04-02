package tools.ensemble.behaviours.ComposerBhaviours.solo;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;

import java.security.acl.Acl;

/**
 * Created by OscarAlfonso on 3/25/2017.
 */
public class GetInfoSectionFromSyn extends OneShotBehaviour implements DataStoreComposer {

    int transition = 4;
    int firstTimeHere;
    MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchConversationId("Inform-the-composer-the-currentSection"),
            MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
    );
    //store the message that was sent by the musician In the previous state
    ACLMessage previousMusicianMessage;
    //
    private AID internalMusician;
    //Section Instance
    Section se = new Section();

    public GetInfoSectionFromSyn(Agent a)
    {
        super(a);
    }

    public void action()
    {
        if (firstTimeHere < 1)
        {
            if (getDataStore().containsKey(INTERNAL_MUSICIAN_AID))
            {
                internalMusician = (AID) getDataStore().get(INTERNAL_MUSICIAN_AID);
            }
        }

        ACLMessage getInfoSection = myAgent.receive(mt1);
        if (getInfoSection != null)
        {

            ContentElement content = null;
            try {
                content = myAgent.getContentManager().extractContent(getInfoSection);
            } catch (Codec.CodecException e) {
                e.printStackTrace();
            } catch (OntologyException e) {
                e.printStackTrace();
            }
            Concept concept = ((Action)content).getAction();
            if(concept instanceof Section)
            {
                System.out.println("Current Section in  : " +getBehaviourName() +" is " +((Section) concept).getAccompanimentCurrentSection());
                System.out.println("index in Section   : " +getBehaviourName() +" is " +((Section) concept).getSectionIndex());
                System.out.println("Time left "+((Section) concept).getTimeLeft().getTime());
                se.setAccompanimentCurrentSection(((Section) concept).getAccompanimentCurrentSection());
                se.setSectionStartedAt(((Section) concept).getSectionStartedAt());
                se.setTimeLeft(((Section) concept).getTimeLeft());
                se.setSectionIndex(((Section) concept).getSectionIndex());
                if (getDataStore().containsKey(SECTION_INSTANCE_FOR_SYN_SOLO))
                {
                    getDataStore().remove(SECTION_INSTANCE_FOR_SYN_SOLO);
                    getDataStore().put(SECTION_INSTANCE_FOR_SYN_SOLO,se);
                }else
                {
                    getDataStore().put(SECTION_INSTANCE_FOR_SYN_SOLO,se);
                }
            }
            transition = 5;


        }else
        {
            block();
        }
    }

    public int onEnd()
    {
        if(transition == 4)
        {
            block(500);
        }
        firstTimeHere++;
        return transition;
    }
}