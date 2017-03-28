package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

/**
 * Created by OscarAlfonso on 3/27/2017.
 */
public class WaitToGetLead extends OneShotBehaviour {

    private int transition = 37;
    private int firstTimeHere = 0;
    private int state = 0;
    private ACLMessage informToLeader = null;
    private MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
            MessageTemplate.MatchPerformative(ACLMessage.CFP));
    private MessageTemplate mt1Andmt2 = MessageTemplate.and(mt1,MessageTemplate.MatchConversationId("request-solo-protocol"));


    public WaitToGetLead(Agent a)
    {
        super(a);
    }

    public void action()
    {
        if(firstTimeHere < 1)
        {
            System.out.println("Wait For lead ");
            ResponseRequestSoloNegotiation responseTorequest = new ResponseRequestSoloNegotiation(myAgent,mt1Andmt2);
            responseTorequest.setDataStore(getDataStore());
            myAgent.addBehaviour(responseTorequest);
        }

        switch (state)
        {
            case 1:
                System.out.println("I going to request a solo");
                myAgent.send(informToLeader);
                transition = 14;
                break;

        }

    }

    public int onEnd()
    {
        firstTimeHere++;
        if (transition == 37)
        {
            block(500);
        }
        return transition;
    }

    private class ResponseRequestSoloNegotiation extends ContractNetResponder
    {
        public ResponseRequestSoloNegotiation(Agent a, MessageTemplate mt)
        {
            super(a,mt);
            System.out.println("Response to the protocol");
        }

        protected ACLMessage handleCfp (ACLMessage cfp) throws NotUnderstoodException, RefuseException
        {

            System.out.println("Agent "+myAgent.getLocalName()+": CFP received from "+cfp.getSender().getName());
            //We randomly decide either we want to play or not.
            if(deciteToPlay())
            {
                ACLMessage propose = cfp.createReply();
                System.out.println("I will propose to play the solo");
                propose.setPerformative(ACLMessage.PROPOSE);
                return propose;
            }else
            {
                throw new RefuseException("I wont play the solo");
            }


        }

        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException
        {
            System.out.println("Agent "+myAgent.getLocalName()+": Proposal accepted");
            ACLMessage inform = accept.createReply();
            informToLeader = accept.createReply();
            informToLeader.setPerformative(ACLMessage.INFORM);
            inform.setPerformative(ACLMessage.PROPAGATE);
            state = 1;
            return inform;
        }

        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            System.out.println("Agent "+myAgent.getLocalName()+": Proposal rejected");


        }




    }

    //Hacemos una simulación para que pueda dar que existe o no coche (sobre un 80% probab).
    private boolean deciteToPlay() {
        return (Math.random() * 100 > 10);
    }

}
