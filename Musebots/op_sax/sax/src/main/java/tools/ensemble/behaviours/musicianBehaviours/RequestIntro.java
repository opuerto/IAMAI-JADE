package tools.ensemble.behaviours.musicianBehaviours;

import jade.content.AgentAction;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
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

    //get the instance of the agent
    private Agent agent;
    //Get the language of the ACLMessage
    private Codec codec;
    //Get the Ontology of the ACLMessage
    private Ontology ontology;
    //get the timeHandler ontology
    private Ontology timeHandlerOntology;
    //manage the exit of the state
    private int transition;
    //get the number of measure that will determines the lenght of the intro
    private int numberOfMeasures = 6;
    //Flag that check if was the first time in this state
    private int counter = 0;
    // a class that extend the contract-net initiator
    private IntroNegotiation introNegotiation;
    //get the message
    private ACLMessage msg;
    //Get the musicians from the data store
    private Vector musicians = new Vector();
    //construct the list of musicians that will received our request to perform an intro
    private Vector receivers = new Vector();
    //get the duration of the intro base on the elements of the score and the lenght of the intro (number of measures)
    private long introDuration;
    //get the elements of the score
    private ScoreElements scoreElements;
    //Evaluate the case about which action the state should perform
    private int steps = 0;
    //handle the timeout on the ContractNet protocol if we didn't get a reply from the receiver we need to reset and try firstTimeHere
    private int timeout = 0;
    //count the number of responders that we receive in the contractNet protocol
    private int nResponders = 0;
    //Save the timestamp at the momment we got a confirm that the intro is already starting to play.
    private long introTimestamp;


    public RequestIntro(Agent a, Codec codec, Ontology ontology, Ontology timeHandler)
    {
        super(a);
        this.agent = a;
        this.codec = codec;
        this.ontology = ontology;
        this.timeHandlerOntology = timeHandler;

    }

    public void action()
    {
        //If case that something went wrong in the contract net interaction protocol that negotiate the intro request.
        if(timeout > 0 && timeout < 2)
        {
            timeout = 0;
            agent.removeBehaviour(introNegotiation);
            introNegotiation = null;
            counter = 0;


        }
        //If this is the very first time getting to this state or if the timeout reset the counter to 0
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
            introNegotiation.setDataStore(getDataStore());
            agent.addBehaviour(introNegotiation);
        }


        //Handle where I'm heading to from here.
        switch (steps)
        {
            case 0:
                transition = 29;
                break;
            case 1:
                agent.removeBehaviour(introNegotiation);
                introNegotiation = null;
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
         nResponders = receivers.size();

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
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));


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
            getDataStore().put(INTRO_DURATION,introDuration);
        }


    }


    /**
     * This private class will implement the ContractNet interaction protocol
     * that will arrange which musician will play the intro of the song
     */
    private class IntroNegotiation extends ContractNetInitiator
    {
        AID responderSelectect = null;
        //Check the we only accept the first musician that propose and accept the call for proposal
        int responderCtn = 0;
        public IntroNegotiation(Agent a, ACLMessage msg)
        {
            super(a,msg);
            System.out.println("Negotiation Intro Started");
        }

        protected void handlePropose(ACLMessage propose, Vector v) {
            System.out.println("Agent "+propose.getSender().getName()+" proposed ");
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
                timeout = 1;
            }
           // Immediate failure --> we will not receive a response from this agent
            nResponders--;
        }

        protected void handleAllResponses(Vector responses, Vector acceptances)
        {
            System.out.println(responses.size());
            if (responses.size() < nResponders) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing "+(nResponders - responses.size())+" responses");
                //Try firstTimeHere from the beginning
                if (nResponders < 1)
                {
                    System.out.println("Non responder");
                    timeout = 1;
                }

            }
            ACLMessage accept = null;
            //save the duration from the responder
            long getDuration = 0;
            Enumeration e = responses.elements();
            while (e.hasMoreElements())
            {

                ACLMessage ms = (ACLMessage) e.nextElement();

                ms.setLanguage(codec.getName());
                ms.setOntology(ontology.getName());
                if (ms.getPerformative() == ACLMessage.PROPOSE)
                {
                    //increment if when we got a propose performative
                    responderCtn++;
                    ACLMessage reply = ms.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                   try
                   {
                      ContentElement content = agent.getContentManager().extractContent(ms);
                       AgentAction action = (AgentAction) ((Action)content).getAction();
                       //Get the duration from the responder and save it into the variable
                       if (action instanceof PlayIntroAction){ getDuration = (long) ((PlayIntroAction) action).getDuration();
                       }

                   }catch (Exception ex) {
                       ex.printStackTrace();
                   }
                    acceptances.addElement(reply);

                   if(responderCtn == 1)
                   {
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
               //If there is no good proposal we try firstTimeHere from the beginning.
                System.out.println("All Rejected");
                timeout = 1;

            }


        }
        protected void handleInform(ACLMessage inform) {
            //Save the exact time that we got this confirmation. This will be used in the next state where we will negotiate the acompaniement
            introTimestamp = System.currentTimeMillis();
            System.out.println("duration: "+introDuration);
            getDataStore().put(INTRO_TIMESTAMP,introTimestamp);
            System.out.println("Intro timestamp: "+introTimestamp);
            System.out.println("Agent "+inform.getSender().getName()+" The intro has started to play");
            //Let know to the next state that is gonna be the first solo in the song
            getDataStore().put(FIRST_SOLO,true);
            steps = 1;
            //myAgent.doWait(introDuration);


            //Go to the next state;

        }


    }
}
