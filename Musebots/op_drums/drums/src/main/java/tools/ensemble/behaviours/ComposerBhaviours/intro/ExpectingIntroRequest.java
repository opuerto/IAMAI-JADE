package tools.ensemble.behaviours.ComposerBhaviours.intro;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Template;
import jade.content.AgentAction;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.ontologies.composer.ComposerOntology;
import tools.ensemble.ontologies.composer.vocabulary.concepts.IntroConcepts;

/**
 * Created by OscarAlfonso on 2/28/2017.
 */
public class ExpectingIntroRequest extends OneShotBehaviour implements DataStoreComposer {

    //Ontologies
    private Ontology composerOntology = ComposerOntology.getInstance();
    //Language
    private Codec composerLanguage;
    //Agent instance
    private Agent agent;
    //transition of the ExpectingIntroRequest behaviour
    private int transition = 0;
    //step
    private int step = 0;
    //int flag that check if is the first time on this state
    private int firstTimeState = 0;
    //message template
    private MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchOntology(composerOntology.getName()),
            MessageTemplate.MatchPerformative(ACLMessage.CFP)
    );
    private MessageTemplate mt1andmt2 = MessageTemplate.and(mt1,MessageTemplate.MatchConversationId("introInteraction-musician-composer-CFP"));

    private ACLMessage meesageCfp;

    //intro concept object from the composer ontology
    private IntroConcepts introInstance;

    //ContractNET Initiator
    //ResponseToIntroRequest introRequest;

    public ExpectingIntroRequest(Agent a, Ontology ontology, Codec lang)
    {
        super(a);
        this.composerOntology = ontology;
        this.composerLanguage = lang;
        this.agent = a;

    }

    public void action()
    {
        meesageCfp = agent.receive(mt1andmt2);
        if(meesageCfp != null)
        {
            System.out.println("Agent "+agent.getLocalName()+": CFP received from "+meesageCfp.getSender().getName()+". Action is "+meesageCfp.getContent());
            getDataStore().put(COMPOSER_MY_INTERNAL_MUSICIAN,meesageCfp.getSender());
            //get the elements of the score from the musician agent
            try {
                ContentElement content = agent.getContentManager().extractContent(meesageCfp);
                Concept action = ((Action)content).getAction();
                if(action instanceof IntroConcepts)
                {
                    //assign the data
                    introInstance = (IntroConcepts) getDataStore().get(INTRO_COMPOSER_INSTANCE);
                    introInstance.setIntroLength(((IntroConcepts) action).getIntroLength());
                    introInstance.setIntroTempo(((IntroConcepts) action).getIntroTempo());
                    introInstance.setIntroNumerator(((IntroConcepts) action).getIntroNumerator());
                    introInstance.setIntroDenominator(((IntroConcepts) action).getIntroDenominator());
                    if(getDataStore().containsKey(INTRO_COMPOSER_INSTANCE))
                    {
                        getDataStore().remove(INTRO_COMPOSER_INSTANCE);
                        getDataStore().put(INTRO_COMPOSER_INSTANCE,introInstance);
                    }

                }

            } catch (Codec.CodecException e) {
                e.printStackTrace();
            } catch (OntologyException e) {
                e.printStackTrace();
            }

            ACLMessage propose = meesageCfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setConversationId("introInteraction-musician-composer-PROPOSE");
            propose.setReplyWith(meesageCfp.getSender().getLocalName()+System.currentTimeMillis());
            getDataStore().put(CURRENT_MESSAGE,propose);
            agent.send(propose);
            //pass to the case 2
            transition = 1;
        }else{block();}

    }



    public int onEnd()
    {
        firstTimeState++;
        if (transition == 0)
        {
            block(500);
        }
        return transition;
    }






   /* private class ResponseToIntroRequest extends ContractNetResponder
    {
        IntroConcepts introConcepts = (IntroConcepts) getDataStore().get(INTRO_COMPOSER_INSTANCE);

        public ResponseToIntroRequest()
        {
            super(agent,mt1andmt2);

        }

        protected ACLMessage handleCfp (ACLMessage cfp) throws NotUnderstoodException, RefuseException
        {

            System.out.println("Agent "+agent.getLocalName()+": CFP received from "+cfp.getSender().getName()+". Action is "+cfp.getContent());
            //get the elements of the score from the musician agent
            try {
                ContentElement content = agent.getContentManager().extractContent(cfp);
                Concept action = ((Action)content).getAction();
                if(action instanceof IntroConcepts)
                {
                    //assign the data
                    introInstance = (IntroConcepts) getDataStore().get(INTRO_COMPOSER_INSTANCE);
                    introInstance.setIntroLength(((IntroConcepts) action).getIntroLength());
                    introInstance.setIntroTempo(((IntroConcepts) action).getIntroTempo());
                    introInstance.setIntroNumerator(((IntroConcepts) action).getIntroNumerator());
                    introInstance.setIntroDenominator(((IntroConcepts) action).getIntroDenominator());
                    if(getDataStore().containsKey(INTRO_COMPOSER_INSTANCE))
                    {
                        getDataStore().remove(INTRO_COMPOSER_INSTANCE);
                        getDataStore().put(INTRO_COMPOSER_INSTANCE,introInstance);
                    }

                }

            } catch (Codec.CodecException e) {
                e.printStackTrace();
            } catch (OntologyException e) {
                e.printStackTrace();
            }

            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            getDataStore().put(CURRENT_MESSAGE,propose);
            //pass to the case 2
            transition = 1;
            return propose;
        }


    }*/


}
