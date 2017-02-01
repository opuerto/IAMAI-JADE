package tools.ensemble.behaviours.musicianBehaviours;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.ensemble.agents.Musician;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ScoreElements;

/**
 * Created by OscarAlfonso on 1/31/2017.
 */
public class AccompanientGetStructure extends OneShotBehaviour {

    Agent agent;
    int transition;
    public AccompanientGetStructure (Agent a)
    {
        super(a);
        this.agent = a;
    }

    public void action()
    {
        receiveSongStructure();
    }



    public int onEnd()
    {
        return transition;
    }

    private void receiveSongStructure()
    {
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("score-elements"),MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msg = agent.receive(mt);
        if(msg != null)
        {
            try
            {
                ContentElement content = agent.getContentManager().extractContent(msg);
                Concept concept = ((Action)content).getAction();
                if(concept instanceof ScoreElements)
                {
                    int tempo = ((ScoreElements) concept).getTempo();
                    int numerator = ((ScoreElements) concept).getNumerator();
                    int denominaor = ((ScoreElements) concept).getDenominator();
                    String form  = ((ScoreElements) concept).getForm();
                    Musician.tempo = tempo;
                    Musician.timeSignatureNumerator = numerator;
                    Musician.timeSignatureDenominator = denominaor;
                    Musician.tuneForm = form;
                    System.out.println("this is the tempo "+Musician.tempo);
                    System.out.println("this is the timesignature "+Musician.timeSignatureNumerator);
                    System.out.println("this is the denominator " +  Musician.timeSignatureDenominator);
                    System.out.println("this is the structure " +  Musician.tuneForm);
                    ACLMessage msgConfirm = msg.createReply();
                    msgConfirm.setPerformative(ACLMessage.CONFIRM);
                    msgConfirm.setContent("I got the elements of the score");
                    agent.send(msgConfirm);
                    transition = 6;
                }

            }catch (Exception e) {
                e.printStackTrace();
            }

        }else
        {
            block();
            transition = 32;
        }


    }

}
