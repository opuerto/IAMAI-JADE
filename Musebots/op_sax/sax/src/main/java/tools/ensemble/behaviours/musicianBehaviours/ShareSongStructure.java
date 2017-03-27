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
import tools.ensemble.interfaces.SongStructure;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ChordsAttributes;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ScoreElements;


import java.util.Date;
import java.util.Vector;

/**
 * Created by OscarAlfonso on 1/18/2017.
 *
 */
public class ShareSongStructure extends OneShotBehaviour implements SongStructure,DataStorteMusicians {

    private Agent agent;
    private Vector musicianList;
    private Codec codec;
    private Ontology ontology;
    private ScoreElements scoreElements;
    private Vector musiciansReceivers = new Vector();
    private Vector musicians = new Vector();
    //public static final String MUSICIAN_LIST = "musicianList";
    private int repliesCnt = 0; // The counter of replies from musicians agents
    private MessageTemplate mt; // The template to receive replies
    private int step = 0;
    private int transition;


    public ShareSongStructure(Agent a, Codec codec, Ontology onto)
    {
        super(a);
        this.agent = a;
        this.codec = codec;
        this.ontology = onto;

    }

    public void action()
    {

        switch (step)
        {
            case 0:
                //First set the vector musicianReceivers
                if(getDataStore().containsKey(MUSICIAN_LIST)){musicians = (Vector) getDataStore().get(MUSICIAN_LIST);}

                //Iterate trought all the musicians an exclude yourself from the list of receivers.
                for(int i=0; i<musicians.size();i++)
                {
                    if(!musicians.get(i).equals(myAgent.getAID())){musiciansReceivers.add(musicians.get(i));}

                }
                System.out.println(musiciansReceivers);
                //Get the elements of the song structure
                if(getDataStore().containsKey(SCORE_ELEMENTS)){scoreElements = (ScoreElements) getDataStore().get(SCORE_ELEMENTS);}
                // Send the Inform to all musicians
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setLanguage(codec.getName());
                msg.setOntology(ontology.getName());
                for(int i = 0; i < musiciansReceivers.size(); i++)
                {
                    try
                    {
                        //fill the content using the Ontology concept
                        myAgent.getContentManager().fillContent(msg,new Action((AID)musiciansReceivers.elementAt(i),scoreElements));
                    }catch (Exception ex) { ex.printStackTrace(); }
                    //Set the list of the receivers
                    msg.addReceiver((AID) musiciansReceivers.get(i));
                }
                msg.setConversationId("score-elements");
                msg.setReplyWith("inform"+System.currentTimeMillis());

                msg.setReplyByDate(new Date(System.currentTimeMillis() + 3000));
                myAgent.doWait(3000);
                agent.send(msg);
                // Prepare the template to get proposals
                mt = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("score-elements"),
                        MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
                step = 1;
                //Return to this state with transition 28
                transition = 28;
                break;
            case 1:
                // Receive all confirms from agents musicians
                ACLMessage reply = agent.receive(mt);
                if (reply != null)
                {
                    // Reply received
                    if (reply.getPerformative() == ACLMessage.CONFIRM)
                    {
                        //we add to the counter
                        repliesCnt++;
                        System.out.println(repliesCnt);
                        System.out.println(reply.getContent()+ " from " + reply.getSender().getLocalName());
                    }

                    if(repliesCnt >= musiciansReceivers.size())
                    {
                        //We got all the replies from the musician we can then go to the next step
                        step = 2;

                    }

                    else
                    {

                        transition = 4;


                    }

                }else{block();}
                transition = 28;
                break;
            case 2:
                // we complete the task in this state, now we are ready to go to the next state with transition = 4.
                transition = 4;
                break;
        }



    }

    public int onEnd() {

        if(transition == 28)
        {
            block(500);
        }
        return transition;
    } //Exit with the transition value to the corresponding state.


}
