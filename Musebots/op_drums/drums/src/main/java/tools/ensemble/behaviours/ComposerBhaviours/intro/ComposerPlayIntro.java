package tools.ensemble.behaviours.ComposerBhaviours.intro;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jm.JMC;
import jm.music.data.Score;
import jm.util.Play;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.ontologies.composer.vocabulary.concepts.IntroConcepts;

import java.util.Date;

/**
 * Created by OscarAlfonso on 3/5/2017.
 */
public class ComposerPlayIntro extends OneShotBehaviour implements DataStoreComposer, JMC {

    private Agent agent;
    private int transition = 4;
    private int firstTimeHere = 0;
    private Score introScore;
    private MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchConversationId("introInteraction-musician-composer-CONFIRM"),
            MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
    );
    private IntroConcepts introInstanceConceps;
    private ACLMessage messageInform;
    private MessageTemplate mt1andmt2;
    public ComposerPlayIntro(Agent a)
    {
        super(a);
        this.agent = a;
    }

    public void action()
    {
        if(firstTimeHere < 1) {
            if (getDataStore().containsKey(CURRENT_MESSAGE)) {
                ACLMessage cm = (ACLMessage) getDataStore().get(CURRENT_MESSAGE);
                mt1andmt2 = MessageTemplate.and(mt1, MessageTemplate.MatchInReplyTo(cm.getReplyWith()));
            }

        }
        messageInform = agent.receive(mt1andmt2);
        if(messageInform != null)
        {
            System.out.println(messageInform);
            ACLMessage replyMessageInform = messageInform.createReply();
            replyMessageInform.setPerformative(ACLMessage.INFORM);
            replyMessageInform.setConversationId("introInteraction-musician-composer-INFORM");
            replyMessageInform.setReplyWith(messageInform.getSender().getLocalName()+System.currentTimeMillis());
            System.out.println("Agent "+messageInform.getSender().getName()+" confirmed ");
            if(getDataStore().containsKey(INTRO_SCORE))
            {
                introScore = (Score) getDataStore().get(INTRO_SCORE);
                if(getDataStore().containsKey(INTRO_COMPOSER_INSTANCE))
                {
                    introInstanceConceps = (IntroConcepts) getDataStore().get(INTRO_COMPOSER_INSTANCE);
                    Play.midi(introScore,false,false,5,0);
                    introInstanceConceps.setIntroStartedAt(new Date());
                    try
                    {
                        //fill the content using the Ontology concept
                        myAgent.getContentManager().fillContent(replyMessageInform,new Action(messageInform.getSender(),introInstanceConceps));
                    }catch (Exception ex) { ex.printStackTrace(); }
                    agent.send(replyMessageInform);
                }

            }
            transition = 5;
        }else{block();}
    }

    public int onEnd()
    {
        return transition;
    }
}
