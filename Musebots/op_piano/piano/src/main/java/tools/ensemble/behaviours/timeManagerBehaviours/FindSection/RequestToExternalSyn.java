package tools.ensemble.behaviours.timeManagerBehaviours.FindSection;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;

import java.util.Date;
import java.util.Vector;

/**
 * Created by OscarAlfonso on 3/25/2017.
 */
public class RequestToExternalSyn extends OneShotBehaviour implements DataStoreTimeManager {

    private int transition = 3;
    private int firstTimeHere = 0;
    private int state = 0;
    private int ReceiversNo;
    private String form;
    private Ontology SynOnto;
    private Codec language;
    private int firsSolo;
    private int sectionIndex;
    private String CurrentSection;
    private Date timeLeft;

    private ACLMessage findSection = new ACLMessage(ACLMessage.REQUEST);
    Vector timeManagerList = new Vector();
    AID internalComposer;
    public RequestToExternalSyn(Agent a, Ontology ont, Codec lan)
    {
        super(a);
        this.SynOnto = ont;
        this.language = lan;
    }

    public void action()
    {

        switch (state)
        {
            case 0:
                if (firstTimeHere < 1)
                {
                    form = Musician.tuneForm;
                    if (getDataStore().containsKey(FIRST_TIME_SOLO_IN_SYN))
                    {
                        firsSolo = (Integer) getDataStore().get(FIRST_TIME_SOLO_IN_SYN);

                    }

                    if(getDataStore().containsKey(TIME_MANAGER_LIST))
                    {
                        timeManagerList = (Vector) getDataStore().get(TIME_MANAGER_LIST);
                        ReceiversNo = timeManagerList.size();
                    }
                    if (getDataStore().containsKey(INTERNAL_COMPOSER_IN_SYN))
                    {
                        internalComposer = (AID) getDataStore().get(INTERNAL_COMPOSER_IN_SYN);

                    }

                    findSection.setConversationId("Find-Section-With-External-Syn");
                    findSection.setReplyWith(myAgent.getLocalName()+System.currentTimeMillis());
                    for (int i = 0; i < ReceiversNo; i++)
                    {
                        findSection.addReceiver((AID) timeManagerList.get(i));
                    }
                    myAgent.send(findSection);
                    state = 1;
                }

                break;
            case 1:
                         MessageTemplate mt1 = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("Get-Section-From-External-Syn"),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM)
                );
                MessageTemplate mt1Andmt2 = MessageTemplate.and(mt1,MessageTemplate.MatchInReplyTo(findSection.getReplyWith()));
                ACLMessage getSection = myAgent.receive(mt1Andmt2);
                if(getSection != null)
                {
                    ContentElement content = null;
                    try {
                        content = myAgent.getContentManager().extractContent(getSection);
                    } catch (Codec.CodecException e) {
                        e.printStackTrace();
                    } catch (OntologyException e) {
                        e.printStackTrace();
                    }

                    //Concept
                    Concept concept = ((Action)content).getAction();
                    if (concept instanceof Section)
                    {
                        sectionIndex = ((Section) concept).getSectionIndex();
                        CurrentSection = ((Section) concept).getAccompanimentCurrentSection();
                        timeLeft = ((Section) concept).getTimeLeft();
                    }

                    if(firsSolo == 1)
                    {
                        //Check if the song is in the last section of the structure so we can start right at the beginning
                        int lastSectionIndex = form.length()-1;
                        String LastSection = String.valueOf(form.charAt(lastSectionIndex));

                        if(sectionIndex == lastSectionIndex && LastSection.equals(CurrentSection))
                        {
                            //If is the last section then send the info to the composer
                            state = 2;
                        }
                        else
                        {
                            //If not try again until we got the last section
                            state = 0;
                        }

                    }
                    else // If is not the first solo to be play in the song then send the composer whatever it got from the external synchronizer
                    {
                        state = 2;
                    }


                }else
                {
                    block();
                }
                break;
            case 2:
                ACLMessage sendInfoToComposer = new ACLMessage(ACLMessage.CONFIRM);
                sendInfoToComposer.setConversationId("Inform-the-composer-the-currentSection");
                sendInfoToComposer.setReplyWith(myAgent.getLocalName()+System.currentTimeMillis());
                sendInfoToComposer.setOntology(SynOnto.getName());
                sendInfoToComposer.setLanguage(language.getName());
                Section se = new Section();
                se.setSectionIndex(sectionIndex);
                se.setAccompanimentCurrentSection(CurrentSection);
                se.setTimeLeft(timeLeft);
                try
                {
                    //fill the content using the Ontology concept
                    myAgent.getContentManager().fillContent(sendInfoToComposer,new Action((AID)internalComposer,se));
                }catch (Exception ex) { ex.printStackTrace(); }
                sendInfoToComposer.addReceiver(internalComposer);
                myAgent.send(sendInfoToComposer);
                transition = 4;
                break;
        }
    }

    public int onEnd()
    {
        firstTimeHere++;
        if (transition == 3)
        {
            block(500);
        }
        return transition;
    }
}
