package tools.ensemble.behaviours.timeManagerBehaviours;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Intro;

import java.util.Date;


/**
 * Created by OscarAlfonso on 2/3/2017.
 */
public class RequestIntroDataResponder extends AchieveREResponder implements DataStoreTimeManager {
    private Intro introInstance;
    private float introLength;
    private Date introStartedAt;
    private Codec codec;
    private Ontology timeHandlerOntology;

    public RequestIntroDataResponder(Agent a, MessageTemplate mt, Ontology onto, Codec lang)
    {
        super(a,mt);
        this.codec = lang;
        this.timeHandlerOntology = onto;
    }

    protected ACLMessage handleRequest(ACLMessage request)
            throws NotUnderstoodException, RefuseException
    {
        introInstance = (getDataStore().containsKey(INTRO_INSTANCE))? (Intro) getDataStore().get(INTRO_INSTANCE) : null;
              //System.out.println("start at in millis from "+getBehaviourName()+" "+introInstance.getIntroStartedAt().getTime());
        if(introInstance != null)
        {
            introLength = introInstance.getIntroLenght();
            introStartedAt = introInstance.getIntroStartedAt();

           // System.out.println("start at second in millis "+introInstance.getIntroStartedAt().getTime());


            if(introLength > 0 && introStartedAt.getTime() > 0)
            {
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }else
            {
                throw new RefuseException("We don´t have this information");
            }
        }else{throw new RefuseException("We don´t have this information");}
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

        return inform;
    }

}
