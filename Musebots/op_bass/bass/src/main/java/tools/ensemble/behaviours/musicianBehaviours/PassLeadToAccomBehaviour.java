package tools.ensemble.behaviours.musicianBehaviours;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStorteMusicians;
import tools.ensemble.ontologies.musicians.vocabulary.actions.PlayIntroAction;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;


/**
 * Created by OscarAlfonso on 3/27/2017.
 */
public class PassLeadToAccomBehaviour extends OneShotBehaviour implements DataStorteMusicians {

    private int transition = 34;
    private int firstTimeHere = 0;
    private int state = 0;
    private AID candidateToSolo;
    //Get the musicians from the data store
    private Vector musicians = new Vector();
    //construct the list of musicians that will received our request to perform an intro
    private Vector receivers = new Vector();
    //count the number of responders that we receive in the contractNet protocol
    private int nResponders = 0;
    //get the message
    private ACLMessage msg;

    //state 2 first time
    int firstTimeHereState2 = 0;




    public PassLeadToAccomBehaviour(Agent a)
    {
        super(a);
    }

    public void onStart()
    {
        transition = 34;
        firstTimeHereState2 = 0;
        nResponders = 0;
        state = 0;
        System.out.println("Pass Lead To accompaniment");
    }

    public void action()
    {

        switch (state)
        {
            case 0:
                MessageTemplate mt1 = MessageTemplate.and(MessageTemplate.MatchConversationId("I-have-been-playing-enough"),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM)
                );
                ACLMessage message = myAgent.receive(mt1);
                if (message != null)
                {
                    state = 2;

                }
                else
                {
                    block();
                }
                break;
            case 2:
                if(firstTimeHereState2 < 1)
                {
                    System.out.println("Im going to request");
                    if(!receivers.isEmpty())
                    {
                        receivers.removeAllElements();
                    }
                    //Find a receiver
                    findAllReceivers();
                    constructACLMessage();
                    //myAgent.doWait(11000);
                    requestSoloNegotiation RequestSolo = new requestSoloNegotiation(myAgent,msg);
                    RequestSolo.setDataStore(getDataStore());
                    myAgent.addBehaviour(RequestSolo);
                    firstTimeHereState2++;

                }
                break;
            case 3:
                //Im not leader anymore Set the flag to false
                Musician.setLeader(false);
                transition = 39;
                //System.out.println("case 3");
                break;
            case 4:
                System.out.println("case 4");
                break;

        }
    }

    public int onEnd()
    {
        firstTimeHere++;

        if (transition == 34)
        {
            block(500);
        }
        return transition;
    }

    private void constructACLMessage()
    {

        msg = new ACLMessage(ACLMessage.CFP);

        for(int i = 0; i < receivers.size(); i++)
        {
            //Set the receiver of the message
            msg.addReceiver((AID)receivers.elementAt(i));

        }


        //Set the protocol that we gonna use
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        //We indicate the deadline of the reply
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        msg.setConversationId("request-solo-protocol");

    }

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
            System.out.println("receiver "+receivers);
        }
        nResponders = receivers.size();

    }

    private class requestSoloNegotiation extends ContractNetInitiator
    {
        AID responderSelectect = null;
        //Store the sender name
        String sender = null;
        //Store the last musician who passed me the lead
        String LastMusicianPassingMeTheLEad;
        //Store LastMusicians who I passed the lead
        String MusicianIPassedTheLead;
        //Check the we only accept the first musician that propose and accept the call for proposal
        boolean findMycandidate = false;
        boolean findAnotherCandidate = false;
        int rejectedCtn = 0;
        public requestSoloNegotiation(Agent a, ACLMessage msg)
        {
            super(a,msg);
            System.out.println("Negotiation Solo Started");
        }

        protected void handlePropose(ACLMessage propose, Vector v) {
            System.out.println("Agent "+propose.getSender().getName()+" proposed a solo" +propose.getContent());
        }

        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Agent "+refuse.getSender().getName()+" refused");
            nResponders--;
        }

        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                // FAILURE notification from the JADE runtime: the receiver
                // does not exist
                System.out.println("Responder does not exist");
            }
            else {
                System.out.println("Agent "+failure.getSender().getName()+" failed");
                state = 4;
            }
            // Immediate failure --> we will not receive a response from this agent
            nResponders--;
        }

        protected void handleAllResponses(Vector responses, Vector acceptances)
        {
            System.out.println(responses.size());
            if (responses.size() < nResponders) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
                //Try firstTimeHere from the beginning
                if (nResponders < 1)
                {
                    System.out.println("Non responder");
                    firstTimeHereState2 = 0;
                    state = 4;
                }
            }

            ACLMessage accept = null;
            //save the duration from the responder
            Enumeration e = responses.elements();
            while (e.hasMoreElements())
            {
                ACLMessage ms = (ACLMessage) e.nextElement();
                if (ms.getPerformative() == ACLMessage.PROPOSE)
                {
                    sender = ms.getSender().getLocalName();
                    //increment if when we got a propose performative
                    ACLMessage reply = ms.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);
                    //I only need to find one candidate
                    /*if(sender.equals("play_drums"))
                    {
                        accept = reply;
                    }*/
                    if (!findMycandidate)
                    {

                        if (receivers.size() > 1)
                        {
                            if (Musician.lastMusicianIpassedTheLeadership != null)
                            {
                                MusicianIPassedTheLead = Musician.lastMusicianIpassedTheLeadership.getLocalName();

                                if(MusicianIPassedTheLead.equals(sender))
                                {
                                    findAnotherCandidate = true;
                                }
                                else {findAnotherCandidate = false;}
                            }
                            if(Musician.getLastMusicianPassedMeTheLeadership() != null)
                            {
                                LastMusicianPassingMeTheLEad = Musician.getLastMusicianPassedMeTheLeadership().getLocalName();

                                if(LastMusicianPassingMeTheLEad.equals(sender) )
                                {
                                    findAnotherCandidate = true;
                                }else {

                                    System.out.println("last musician i pass "+Musician.getLastMusicianPassedMeTheLeadership());
                                    findAnotherCandidate = false;
                                }
                            }
                            if (!findAnotherCandidate)
                            {
                                accept = reply;
                                Musician.lastMusicianIpassedTheLeadership = ms.getSender();
                                responderSelectect = ms.getSender();
                                findMycandidate = true;

                            }
                        }
                        else
                        {
                            accept = reply;
                            Musician.lastMusicianIpassedTheLeadership = ms.getSender();
                            responderSelectect = ms.getSender();
                            findMycandidate = true;
                        }
                    }
                }
            }
            if(accept != null)
            {
                System.out.println("Accepting proposal from responder "+responderSelectect );
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            }else
            {
                //If there is no good proposal we try firstTimeHere from the beginning.
                System.out.println("All Rejected");
                state = 4;
            }

        }
        protected void handleInform(ACLMessage inform) {
            System.out.println("Im going to be accompaniment");
            state = 3;

        }



    }
}