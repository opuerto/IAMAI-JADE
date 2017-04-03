package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

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
import tools.ensemble.agents.Composer;
import tools.ensemble.agents.Musician;
import tools.ensemble.behaviours.ComposerBhaviours.solo.GetInfoSectionFromSyn;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;

import java.util.Date;

/**
 * Created by OscarAlfonso on 3/29/2017.
 */
public class GetInfoSectionFromSync extends OneShotBehaviour implements DataStoreComposer {


    int transition = 13;
    int firstTimeHere = 0;
    AID internalMusician;
    MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchConversationId("Inform-the-composer-the-currentSection"),
            MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
    );
    MessageTemplate mt2;

    Section se = new Section();
    public GetInfoSectionFromSync(Agent a)
    {
        super(a);
    }

    public void onStart()
    {
        transition = 13;
        firstTimeHere = 0;
    }

    public void action()
    {
        if (firstTimeHere < 1)
        {
            if (getDataStore().containsKey(INTERNAL_MUSICIAN_AID))
            {
                internalMusician = (AID) getDataStore().get(INTERNAL_MUSICIAN_AID);
            }

            ACLMessage previousmessage = (ACLMessage) getDataStore().get(CURRENT_MESSAGE_FOR_SYN);

            mt2 = MessageTemplate.and(mt1,MessageTemplate.MatchInReplyTo(previousmessage.getReplyWith()));
        }

        ACLMessage getInfoSection = myAgent.receive(mt2);
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
                //SET THIS TO TRUE
                Composer.setMustRecalculateTime(true);
                System.out.println("Current Section in From Solo to Support  : " +getBehaviourName() +" is " +((Section) concept).getAccompanimentCurrentSection());
                System.out.println("index in Section  From Solo to Support : " +getBehaviourName() +" is " +((Section) concept).getSectionIndex());
                System.out.println("Time left From Solo to Support "+((Section) concept).getTimeLeft().getTime());
                long sectionStartedAt = ((Section) concept).getSectionStartedAt().getTime();
                long Now = System.currentTimeMillis();
                long timeElapsed = Now - sectionStartedAt;
                long timeLeft = ((Section) concept).getTimeLeft().getTime() - timeElapsed;
                se.setAccompanimentCurrentSection(((Section) concept).getAccompanimentCurrentSection());
                System.out.println("time elapsed "+timeElapsed);
                System.out.println("time left "+timeLeft);
                Composer.setSectionPlayLeft(timeLeft);
                se.setSectionStartedAt(((Section) concept).getSectionStartedAt());
                se.setTimeLeft(((Section) concept).getTimeLeft());
                se.setSectionIndex(((Section) concept).getSectionIndex());
                if (getDataStore().containsKey(SECTION_INSTANCE_FOR_SYN_ACCOMP))
                {
                    getDataStore().remove(SECTION_INSTANCE_FOR_SYN_ACCOMP);
                    getDataStore().put(SECTION_INSTANCE_FOR_SYN_ACCOMP,se);
                }else
                {
                    getDataStore().put(SECTION_INSTANCE_FOR_SYN_ACCOMP,se);
                }

            }
            ACLMessage informToMusician = new ACLMessage(ACLMessage.INFORM);
            informToMusician.setConversationId("From-leader-to-Support-Inform");
            informToMusician.addReceiver(internalMusician);
            myAgent.send(informToMusician);
            System.out.println("send meassage to musician");
            Musician.setIsFromLeadingToSupport(true);
            Composer.setHoldComposition(0);
            transition = 15;



        }else
        {
            block();
        }


    }

    public int onEnd()
    {
        return transition;
    }
}
