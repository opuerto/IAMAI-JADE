package tools.ensemble.behaviours.musicianBehaviours;

import jade.content.AgentAction;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jm.JMC;
import tools.ensemble.interfaces.DataStorteMusicians;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ScoreElements;
import tools.ensemble.ontologies.musicians.vocabulary.actions.PlayIntroAction;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by OscarAlfonso on 1/20/2017.
 * Request Intro Behaviour
 */
public class RequestIntro extends OneShotBehaviour implements DataStorteMusicians {

    private Agent agent;
    private Codec codec;
    private Ontology ontology;
    private int transition;
    private int numberOfMeasures = 4;
    private int counter = 0;
    private AID receiverSelected;
    private IntroNegotiation introNegotiation;
    private ACLMessage msg;
    private Vector musicians = new Vector();
    private Vector receivers = new Vector();
    private long introDuration;
    private ScoreElements scoreElements;
    private int steps = 0;
    private int timeout = 0;
    private int nResponders = 0;

    public RequestIntro(Agent a, Codec codec, Ontology ontology)
    {
        super(a);
        this.agent = a;
        this.codec = codec;
        this.ontology = ontology;

    }

    public void action()
    {

        if(timeout > 0 && timeout < 2)
        {
            timeout = 0;
            agent.removeBehaviour(introNegotiation);
            counter = 0;


        }
        //If this is the very first time getting to this state
        if(counter < 1)
        {
            //Clean the list of receivers in case there is something
            if(!receivers.isEmpty())
            receivers.removeAllElements();
            //Calculate the duration of the intro
            calculateDurationIntro();
            //Find a receiver
            findAllReceivers();
            constructACLMessage();
            introNegotiation = new IntroNegotiation(agent,msg);
            agent.addBehaviour(introNegotiation);
        }



        switch (steps)
        {
            case 0:
                transition = 29;
                break;
            case 1:
                agent.removeBehaviour(introNegotiation);
                transition = 17;

        }



    }
    public int onEnd() {

        counter++;
        return transition;
    } //Exit with the transition value to the corresponding state.


    private void findAllReceivers()
    {
        //IF actually there is something where we can search
        if(getDataStore().containsKey(MUSICIAN_LIST))
        {
            //We take the list from the data store
            musicians = (Vector) getDataStore().get(MUSICIAN_LIST);

            //exclude myself from the list
            for (int i = 0; i<musicians.size(); i++)
            {
                if(!musicians.get(i).equals(myAgent.getAID()))
                {
                    receivers.add(musicians.get(i));
                }
            }
        }
        System.out.println("receiver "+receivers);
        nResponders = receivers.size();
        System.out.println("responder number "+nResponders);

    }

    private void constructACLMessage()
    {



        //We fill the object of the ontology
        PlayIntroAction playIntroObject = new PlayIntroAction();
        playIntroObject.setLenght(numberOfMeasures);
        playIntroObject.setNow(true);
        playIntroObject.setDuration(-1);
        msg = new ACLMessage(ACLMessage.CFP);
        msg.setLanguage(codec.getName());
        msg.setOntology(ontology.getName());
        for(int i = 0; i < receivers.size(); i++)
        {
            try
            {
                //fill the content using the Ontology concept
                myAgent.getContentManager().fillContent(msg,new Action((AID)receivers.elementAt(i),playIntroObject));
            }catch (Exception ex) { ex.printStackTrace(); }
            //Set the receiver of the message
            msg.addReceiver((AID)receivers.elementAt(i));

        }

        //Set the protocol that we gonna use
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        //We indicate the deadline of the reply
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 3000));


    }

    private void calculateDurationIntro()
    {
        if(getDataStore().containsKey(SCORE_ELEMENTS))
        {
            scoreElements = (ScoreElements) getDataStore().get(SCORE_ELEMENTS);
            double beatPerMeasure = scoreElements.getNumerator();
            double measures = numberOfMeasures;
            double tempo = scoreElements.getTempo();
            introDuration = (long) ((beatPerMeasure*measures/tempo)*60*1000);
            System.out.println("The intro duration is "+introDuration);
        }


    }


    /**
     * This private class will implement the ContractNet interaction protocol
     * that will arrange which musician will play the intro of the song
     */
    private class IntroNegotiation extends ContractNetInitiator
    {
        AID responderSelectect = null;
        int responderCtn = 0;
        public IntroNegotiation(Agent a, ACLMessage msg)
        {
            super(a,msg);
            System.out.println("Contract Net Initiator ready");
        }

        protected void handlePropose(ACLMessage propose, Vector v) {
            System.out.println("Agent "+propose.getSender().getName()+" proposed "+propose.getContent());
        }

        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Agent "+refuse.getSender().getName()+" refused");

        }

        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                // FAILURE notification from the JADE runtime: the receiver
                // does not exist
                System.out.println("Responder does not exist");
            }
            else {
                System.out.println("Agent "+failure.getSender().getName()+" failed");
            }
            //Try again from the beginning
            timeout = 1;
            // Immediate failure --> we will not receive a response from this agent
            nResponders--;
        }

        protected void handleAllResponses(Vector responses, Vector acceptances)
        {
            System.out.println(responses.size());
            if (responses.size() < nResponders) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing "+(nResponders - responses.size())+" responses");
                //Try again from the beginning
                timeout = 1;
            }
            ACLMessage accept = null;
            long getDuration = 0;
            Enumeration e = responses.elements();
            while (e.hasMoreElements())
            {

                ACLMessage ms = (ACLMessage) e.nextElement();

                ms.setLanguage(codec.getName());
                ms.setOntology(ontology.getName());
                if (ms.getPerformative() == ACLMessage.PROPOSE)
                {
                    responderCtn++;
                    ACLMessage reply = ms.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                   try
                   {
                      ContentElement content = agent.getContentManager().extractContent(ms);
                       AgentAction action = (AgentAction) ((Action)content).getAction();
                       if (action instanceof PlayIntroAction){ getDuration = (long) ((PlayIntroAction) action).getDuration();
                       }

                   }catch (Exception ex) {
                       ex.printStackTrace();
                   }
                    acceptances.addElement(reply);
                   if(introDuration == getDuration && responderCtn == 1)
                   {
                       System.out.println("Same duration");
                       responderSelectect =  ms.getSender();
                       accept = reply;


                   }

                 }
            }

            //Accept the proposal
            if(accept != null)
            {
                System.out.println("Accepting proposal from responder "+responderSelectect );
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            }else
            {
                timeout = 1;
            }


        }
        protected void handleInform(ACLMessage inform) {
            System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
            myAgent.doWait(introDuration);
            System.out.println("BYE BYE");
            //Go to the next state;
            steps = 1;
        }


    }
}
