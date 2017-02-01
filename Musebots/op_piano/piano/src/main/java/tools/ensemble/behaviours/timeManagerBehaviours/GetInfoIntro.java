package tools.ensemble.behaviours.timeManagerBehaviours;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Intro;

/**
 * Created by OscarAlfonso on 1/31/2017.
 */
public class GetInfoIntro extends Behaviour implements DataStoreTimeManager {
    private MessageTemplate mt;
    private boolean exit = false;
    private Intro introInstance = null;
    private int step = 0;
    public void action()
    {
        receiveMessage();

    }

    public boolean done()
    {
        return exit;
    }

    private void receiveMessage()
    {
        mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId("intro-interaction-ITM-1"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        ACLMessage msg = myAgent.receive(mt);
        if(msg != null)
        {
            try
            {
                ContentElement content = myAgent.getContentManager().extractContent(msg);
                Concept concept = ((Action)content).getAction();
                if (concept instanceof Intro)
                {
                    if(getDataStore().containsKey(INTRO_INSTANCE))
                    {
                        introInstance = (Intro) getDataStore().get(INTRO_INSTANCE);
                    }
                    System.out.println(((Intro) concept).getIntroLenght());
                    System.out.println(((Intro) concept).getIntroStartedAt());
                    introInstance.setIntroLenght(((Intro) concept).getIntroLenght());
                    introInstance.setIntroStartedAt(((Intro) concept).getIntroStartedAt());
                    //update intro instance
                    if(introInstance != null)
                    {
                        getDataStore().remove(INTRO_INSTANCE);
                        getDataStore().put(INTRO_INSTANCE,introInstance);
                    }
                    System.out.println("intro instance: "+getDataStore().get(INTRO_INSTANCE));
                    System.out.println("lenght: "+introInstance.getIntroLenght());
                    System.out.println("started at "+(long)introInstance.getIntroStartedAt());
                    ACLMessage msgConfirm = msg.createReply();
                    msgConfirm.setPerformative(ACLMessage.CONFIRM);

                    myAgent.send(msgConfirm);
                    exit = true;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }else
        {
            block();
        }

    }

   /* private void receiveMessages()
    {
        System.out.println(step);
        switch (step)
        {
            case 0:

                mt = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("intro-interaction-ITM"),
                        MessageTemplate.MatchPerformative(ACLMessage.CFP)
                );
                ACLMessage msg = myAgent.receive(mt);
                if(msg != null)
                {
                    System.out.println("I got a message from "+msg.getSender().getName()+ "calling for proposal");
                    msg.createReply();
                    msg.setPerformative(ACLMessage.PROPOSE);
                    myAgent.send(msg);
                    step = 1;
                }else
                {
                    block();
                }

                break;
            case 1:
                mt = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("intro-interaction-ITM"),
                        MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL)
                );
                ACLMessage sendConfirm = myAgent.receive(mt);
                if(sendConfirm != null)
                {
                    try
                    {
                        ContentElement content = myAgent.getContentManager().extractContent(sendConfirm);
                        Concept concept = ((Action)content).getAction();
                        if (concept instanceof Intro)
                        {
                            if(getDataStore().containsKey(INTRO_INSTANCE))
                            {
                                introInstance = (Intro) getDataStore().get(INTRO_INSTANCE);
                            }
                            System.out.println(((Intro) concept).getIntroLenght());
                            System.out.println(((Intro) concept).getIntroStartedAt());
                            introInstance.setIntroLenght(((Intro) concept).getIntroLenght());
                            introInstance.setIntroStartedAt(((Intro) concept).getIntroStartedAt());
                            //update intro instance
                            if(introInstance != null)
                            {
                                getDataStore().remove(INTRO_INSTANCE);
                                getDataStore().put(INTRO_INSTANCE,introInstance);
                            }
                            System.out.println("intro instance: "+getDataStore().get(INTRO_INSTANCE));
                            System.out.println("lenght: "+introInstance.getIntroLenght());
                            System.out.println("started at "+(long)introInstance.getIntroStartedAt());
                            ACLMessage msgConfirm = sendConfirm.createReply();
                            msgConfirm.setPerformative(ACLMessage.CONFIRM);
                            msgConfirm.setContent("We saved the intro data");
                            myAgent.send(msgConfirm);
                            exit = true;
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                }else
                {
                    block();
                }

                break;



        }
    }*/
}
