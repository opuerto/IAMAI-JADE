package tools.ensemble.behaviours.musicianBehaviours;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.ensemble.interfaces.DataStorteMusicians;
import tools.ensemble.ontologies.musicians.vocabulary.actions.PlayAccompaniementAction;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by OscarAlfonso on 1/25/2017.
 */
public class LeaderRequestAccompaniement extends OneShotBehaviour implements DataStorteMusicians {

    //Save the instance of the agent
    private Agent agent;
    //Catalogue of musician that didn't get the change to start playing
    private Hashtable musiciansNotplaying = new Hashtable();
    //Save the ontology
    private Ontology ontology;
    //save the language
    private Codec codec;
    private int step = 0;
    //a variable that will decide what will bet the next state going to
    private int transition;
    //Save the list of musicians in the ensemble
    private Vector musiciansList = new Vector();
    //Save the list of receivers and exclude the sender from that list
    private Vector receivers = new Vector();
    //save the timestamp when the intro started to played from the Data Store
    private long IntroStartedAt;
    //save the current that this state started to work
    private static final long currentTime = System.currentTimeMillis();
    //Duration Time of the intro;
    private long introDuration;
    //time left to start playing the accompaniement
    private long timeLeftToPlay;
    //Number of responders
    int nResponders;
    //Check if is the first time on the state
    int firstTime = 0;
    //ACLMessage
    private ACLMessage requestAccompanientMSG;
    //Message Template
    private MessageTemplate mt; // The template to receive replies

    public LeaderRequestAccompaniement(Agent a, Ontology onto, Codec codec)
    {
        super(a);
        this.agent = a;
        this.ontology = onto;
        this.codec = codec;


    }

    public void action()
    {


        switch (step)
        {
            case 0:
                getIntroTimestamp();
                findAllReceivers();
                constructACLMessage();
                agent.send(requestAccompanientMSG);
                mt = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("request-accompaniement"),
                        MessageTemplate.MatchInReplyTo(requestAccompanientMSG.getReplyWith()));
                step = 1;
                //Return to this state with transition 28
                transition = 31;
                break;
        }


    }

    public int onEnd() {

        //firstTime++;
        return transition;
    } //Exit with the transition value to the corresponding state.

    protected void getIntroTimestamp ()
    {
        if(getDataStore().containsKey(INTRO_DURATION)){introDuration = (Long)getDataStore().get(INTRO_DURATION);}

        if(getDataStore().containsKey(INTRO_TIMESTAMP))
        {
            System.out.println(getDataStore().get(INTRO_TIMESTAMP));
            IntroStartedAt = (Long)getDataStore().get(INTRO_TIMESTAMP);
        }

    }

    private void findAllReceivers()
    {
        //IF actually there is something where we can search
        if(getDataStore().containsKey(MUSICIAN_LIST))
        {
            //We take the list from the data store
            musiciansList = (Vector) getDataStore().get(MUSICIAN_LIST);

            //exclude myself from the list
            for (int i = 0; i<musiciansList.size(); i++)
            {
                if(!musiciansList.get(i).equals(myAgent.getAID()))
                {
                    receivers.add(musiciansList.get(i));
                }
            }
        }
        nResponders = receivers.size();

    }

    //Calculate the time left to start playing the intro.
    private long getTimeLeft(long startedAt, long currentTime)
    {
        long timeLeft = 0;
        long transcurrentTime =  startedAt - currentTime;
        timeLeft = introDuration - transcurrentTime;

        return timeLeft;
    }

    private void constructACLMessage()
    {
        requestAccompanientMSG = new ACLMessage(ACLMessage.CFP);
        requestAccompanientMSG.setLanguage(codec.getName());
        requestAccompanientMSG.setOntology(ontology.getName());
        long timeLeft = getTimeLeft(IntroStartedAt,currentTime);
        System.out.println("Time left is: "+timeLeft);
        PlayAccompaniementAction playAccompaAction = new PlayAccompaniementAction();
        playAccompaAction.setTimeleft(timeLeft);
        for(int i = 0; i < receivers.size(); i++)
        {
            try
            {
                //fill the content using the Ontology concept
                myAgent.getContentManager().fillContent(requestAccompanientMSG,new Action((AID) receivers.elementAt(i),playAccompaAction));
            }catch (Exception ex) { ex.printStackTrace(); }
            requestAccompanientMSG.addReceiver((AID)receivers.elementAt(i));
            requestAccompanientMSG.setConversationId("request-accompaniement");
            requestAccompanientMSG.setReplyWith("CFP"+System.currentTimeMillis());
            requestAccompanientMSG.setReplyByDate(new Date(System.currentTimeMillis() + timeLeft));
        }

    }
}
