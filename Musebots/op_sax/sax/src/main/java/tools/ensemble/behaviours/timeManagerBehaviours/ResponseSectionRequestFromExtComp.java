package tools.ensemble.behaviours.timeManagerBehaviours;

import com.sun.org.apache.bcel.internal.classfile.Code;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;

/**
 * Created by OscarAlfonso on 3/25/2017.
 */
public class ResponseSectionRequestFromExtComp extends SimpleBehaviour implements DataStoreTimeManager {

    private Ontology SynOnt;
    private Codec language;
    private int firstTimeHere = 0;
    private Section sectionInstance;
    private MessageTemplate mt1 = MessageTemplate.and(
      MessageTemplate.MatchConversationId("Find-Section-With-External-Syn"),MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
    );
    public ResponseSectionRequestFromExtComp(Agent a, Ontology ont, Codec lang)
    {
        super(a);
        this.SynOnt = ont;
        this.language = lang;
    }

    public void action()
    {
           ACLMessage replyToExtSyn = myAgent.receive(mt1);
            if(replyToExtSyn != null)
            {

                if (getDataStore().containsKey(SECTION_INSTANCE))
                {
                    sectionInstance = (Section) getDataStore().get(SECTION_INSTANCE);

                }
                if(sectionInstance.getSectionIndex() > -1 && sectionInstance.getTimeLeft().getTime() > 0 && sectionInstance.getSectionStartedAt().getTime() > 0)
                {
                    ACLMessage replyToPearSyn = replyToExtSyn.createReply();
                    replyToPearSyn.setPerformative(ACLMessage.INFORM);
                    replyToPearSyn.setConversationId("Get-Section-From-External-Syn");
                    replyToPearSyn.setOntology(SynOnt.getName());
                    replyToPearSyn.setLanguage(language.getName());
                    try {
                        myAgent.getContentManager().fillContent(replyToPearSyn, new Action(replyToExtSyn.getSender(), sectionInstance));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    //send to the pear synchronizer that was requesting the info
                    myAgent.send(replyToPearSyn);

                }
            }
           else
            {
                block();
            }
    }

    public boolean done()
    {

        firstTimeHere++;
        block(500);
        return false;
    }
}
