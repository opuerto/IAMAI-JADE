package tools.ensemble.behaviours.timeManagerBehaviours;

import com.sun.org.apache.bcel.internal.classfile.Code;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.event.MessageAdapter;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Intro;

import java.util.Date;
import java.util.Vector;

/**
 * Created by OscarAlfonso on 2/2/2017.
 */



public class CheckInfoIntroResponder extends AchieveREResponder implements DataStoreTimeManager {
    private Intro introInstance;
    private float introLength;
    private Date introStartedAt;
    private Codec codec;
    private Ontology timeHandlerOntology;
    private  RequestIntroDataInitiator searchIntro = null;
    private MessageTemplate messageTemplateRequestIntro;

    public CheckInfoIntroResponder(Agent a, MessageTemplate mt, Codec codec, Ontology timeHandler)
    {
        super(a,mt);
        this.codec = codec;
        this.timeHandlerOntology = timeHandler;
        //We are making sure that clear the objects that we dont need anymore.

    }

    protected ACLMessage handleRequest(ACLMessage request)
            throws NotUnderstoodException, RefuseException
    {
        introInstance = (getDataStore().containsKey(INTRO_INSTANCE))? (Intro) getDataStore().get(INTRO_INSTANCE) : null;
        if(introInstance != null)
        {

            introLength = introInstance.getIntroLenght();
            introStartedAt = introInstance.getIntroStartedAt();
            if(introLength > 0 && introStartedAt.getTime() > 0)
            {
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }else
            {
                if(getDataStore().containsKey(TIME_MANAGER_LIST))
                {
                    ACLMessage searchIntroData = new ACLMessage(ACLMessage.REQUEST);
                    searchIntroData.setOntology(timeHandlerOntology.getName());
                    searchIntroData.setConversationId("Need-Info-Intro");
                    searchIntroData.setLanguage(codec.getName());
                    searchIntroData.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);

                    Vector timeManagerReceivers = new Vector();
                    timeManagerReceivers = (Vector) getDataStore().get(TIME_MANAGER_LIST);
                    for(int i = 0; i < timeManagerReceivers.size(); i++)
                    {
                        searchIntroData.addReceiver((AID) timeManagerReceivers.elementAt(i));
                    }
                    if (searchIntro != null)
                    {
                        myAgent.removeBehaviour(searchIntro);
                        searchIntro = null;
                    }
                    searchIntro = new RequestIntroDataInitiator(myAgent,searchIntroData);
                    searchIntro.setDataStore(getDataStore());
                    myAgent.addBehaviour(searchIntro);
                }



                throw new RefuseException("We don´t have this information yet");
            }
        }else
        {
            if(getDataStore().containsKey(TIME_MANAGER_LIST))
            {
                ACLMessage searchIntroData = new ACLMessage(ACLMessage.REQUEST);
                searchIntroData.setOntology(timeHandlerOntology.getName());
                searchIntroData.setConversationId("Need-Info-Intro");
                searchIntroData.setLanguage(codec.getName());
                searchIntroData.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);

                Vector timeManagerReceivers = new Vector();
                timeManagerReceivers = (Vector) getDataStore().get(TIME_MANAGER_LIST);
                for(int i = 0; i < timeManagerReceivers.size(); i++)
                {
                    searchIntroData.addReceiver((AID) timeManagerReceivers.elementAt(i));
                }

                if (searchIntro != null)
                {
                    myAgent.removeBehaviour(searchIntro);
                    searchIntro = null;
                }
                      searchIntro = new RequestIntroDataInitiator(myAgent,searchIntroData);
                      searchIntro.setDataStore(getDataStore());
                myAgent.addBehaviour(searchIntro);
            }
            throw new RefuseException("We don´t have this information yet");
        }
    }

    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
        ACLMessage inform = request.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        inform.setLanguage(codec.getName());
        inform.setOntology(timeHandlerOntology.getName());
        if (introInstance != null)
        {

            try
            {
                //fill the content using the Ontology concept
                myAgent.getContentManager().fillContent(inform,new Action( request.getSender(),introInstance));
            }catch (Exception ex) { ex.printStackTrace(); }

        }
        else
        {
            Intro theIntro = new Intro();
            theIntro.setIntroLenght(0);
            theIntro.setIntroStartedAt(null);
            try
            {
                //fill the content using the Ontology concept
                myAgent.getContentManager().fillContent(inform,new Action(inform.getSender(),theIntro));
            }catch (Exception ex) { ex.printStackTrace(); }
        }

        return inform;
    }


}
