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
import tools.ensemble.agents.TimeManager;
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
    private ACLMessage handleCurrentMessageFromComposer;
    private int ReceiversNo;
    private String form;
    private Ontology SynOnto;
    private Codec language;
    private int firsSolo;
    private int sectionIndex;
    private String CurrentSection;
    private Date timeLeft;
    private Date sectionStartedAt;
    private ACLMessage findSection = new ACLMessage(ACLMessage.REQUEST);
    Vector timeManagerList = new Vector();
    AID internalComposer;

    private int state = 0;


    public RequestToExternalSyn(Agent a, Ontology ont, Codec lan)
    {
        super(a);
        this.SynOnto = ont;
        this.language = lan;
    }

    public void onStart()
    {
        System.out.println("start on externally ");
        transition = 3;
        firstTimeHere = 0;
        state = 0;
        System.out.println("transition "+transition);
        System.out.println("Times here "+firstTimeHere);
        System.out.println("State "+state);
    }

    public void action()
    {


        switch (state)
        {
            case 0:
                if (firstTimeHere < 1)
                {
                    System.out.println("Im in case 0 in request externally state");
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
                    System.out.println("Receiver: "+timeManagerList);
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
                        sectionStartedAt = ((Section) concept).getSectionStartedAt();
                        System.out.println("Info from my section in external syn");
                        System.out.println("section index "+sectionIndex);
                        System.out.println("Section current section " + CurrentSection);
                        System.out.println("Section time left "+timeLeft.getTime());
                    }
                    //state = 2;
                    long Now = System.currentTimeMillis();
                    long timeElapsed = Now - sectionStartedAt.getTime();
                    long timeLefts = timeLeft.getTime() - timeElapsed;
                    long quarterofTimeLeft = timeLeft.getTime() /2;
                    long isEnoughTime = timeLeft.getTime() - quarterofTimeLeft;
                    System.out.println(" is enough time "+isEnoughTime);

                    if(timeLefts < 0 || timeLefts < isEnoughTime)
                    {
                        System.out.println("the time left is state 0 "+timeLefts);
                        firstTimeHere = -1;
                        state = 0;
                    }else
                    {
                        System.out.println("the time left is "+timeLefts);
                        state = 2;
                    }


                   /* if(firsSolo == 1)
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
                    }*/


                }else
                {
                    block();
                }
                break;
            case 2:
                //Set the reply with from the original message
                handleCurrentMessageFromComposer = (ACLMessage) getDataStore().get(CURRENT_MESSAGE_FROM_COMPOSER);
                ACLMessage sendInfoToComposer = new ACLMessage(ACLMessage.CONFIRM);
                sendInfoToComposer.setConversationId("Inform-the-composer-the-currentSection");
                sendInfoToComposer.setInReplyTo(handleCurrentMessageFromComposer.getReplyWith());
                sendInfoToComposer.setReplyWith(myAgent.getLocalName()+System.currentTimeMillis());
                sendInfoToComposer.setOntology(SynOnto.getName());
                sendInfoToComposer.setLanguage(language.getName());
                Section se = new Section();
                se.setSectionIndex(sectionIndex);
                se.setAccompanimentCurrentSection(CurrentSection);
                se.setTimeLeft(timeLeft);
                se.setSectionStartedAt(sectionStartedAt);
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
        if (transition == 4)
        {
            firstTimeHere = 0;
        }
        return transition;
    }
}
