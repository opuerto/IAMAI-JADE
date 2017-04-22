package tools.ensemble.behaviours.timeManagerBehaviours;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Intro;

import java.util.Vector;

/**
 * Created by OscarAlfonso on 2/3/2017.
 */
public class RequestIntroDataInitiator extends AchieveREInitiator implements DataStoreTimeManager {


    public RequestIntroDataInitiator(Agent a, ACLMessage msg)
    {

        super(a,msg);

    }



    protected void handleAgree(ACLMessage agree) {
        System.out.println("Im in behaviour "+getBehaviourName());
        System.out.println("The agent "+agree.getSender().getName() +" agree");
    }

    protected void handleRefuse(ACLMessage refuse) {
        System.out.println("Im in behaviour "+getBehaviourName());
        System.out.println("The agent "+refuse.getSender().getName() +" refuse");

    }

    protected void handleNotUnderstood(ACLMessage notUnderstood) {
        System.out.println("The agent "+notUnderstood.getSender().getName() +"not Understood");

    }

    protected void handleInform(ACLMessage inform) {
        System.out.println("The agent "+inform.getSender().getName() +" inform");
        try
        {
            ContentElement content = myAgent.getContentManager().extractContent(inform);
            Concept concept = ((Action)content).getAction();
            if(concept instanceof Intro)
            {
                if(((Intro) concept).getIntroLenght() != 0 && ((Intro) concept).getIntroStartedAt().getTime() != 0)
                {

                    if(getDataStore().containsKey(INTRO_INSTANCE))
                    {

                        Intro intro = (Intro) getDataStore().get(INTRO_INSTANCE);
                        intro.setIntroLenght(((Intro) concept).getIntroLenght());
                        intro.setIntroStartedAt(((Intro) concept).getIntroStartedAt());
                        getDataStore().remove(INTRO_INSTANCE);
                        getDataStore().put(INTRO_INSTANCE,intro);
                        System.out.println("Intro instance $$ "+intro.getIntroLenght());


                    }
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void handleFailure(ACLMessage fail) {
        System.out.println(myAgent.getLocalName()+" : "+" Something went wrong");

    }

    protected void handleAllResponses(Vector responses, Vector acceptances)
    {

        System.out.println(responses.size());
        if (responses.size() < 1) {
            // Some responder didn't reply within the specified timeout
            System.out.println("Timeout expired: missing responses");
        }
    }
}
