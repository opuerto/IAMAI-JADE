package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

import com.sun.org.apache.regexp.internal.RE;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jm.JMC;
import jm.music.data.*;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreComposer;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by OscarAlfonso on 3/6/2017.
 */
public class ComposeAccompanimentBehaviour extends OneShotBehaviour implements DataStoreComposer, JMC {

    private int transition = 2;
    private int firstTimeHere = 0;
    private MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchConversationId("request-accompaniment-conversation-CONFIRM"),
            MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
    );
   // private MessageTemplate mt1andmt2;

    private Score AccompanimentScore = new Score("Accompaniment Score");
    private Phrase AccompanimentPhrase = new Phrase("Accompaniment Phrase");
    private Part AccompanimentPianoPart = new Part("Accompaniment Piano",PIANO,2);
    private String form;
    private Queue<Character> queueSections = new LinkedList<Character>();
    private int rootPitch = C4;


    public ComposeAccompanimentBehaviour(Agent a)
    {
        super(a);
    }

    public void action()
    {
       /* if (firstTimeHere < 1 )
        {

             ACLMessage lastMessage = (ACLMessage) getDataStore().get(CURRENT_MESSAGE);
             MessageTemplate mt1andmt2 = MessageTemplate.and(mt1,MessageTemplate.MatchInReplyTo(lastMessage.getReplyWith()));;

        }*/
        if (firstTimeHere < 1 )
        {
            form = Musician.tuneForm;
            AccompanimentScore.setTempo(Musician.tempo);

            for (char c : form.toCharArray())
            {
                queueSections.add(c);
            }
            System.out.println("Original queueSection "+queueSections);

        }
        if(getDataStore().containsKey(FROM_PLAY_TO_COMPOSE))
        {


            if(getDataStore().containsKey(HOLD_COMPOSITION))
            {
                int hold = (Integer) getDataStore().get(HOLD_COMPOSITION);
                int holdPlay = 0;
                if (hold < 1)
                {
                    //composeAccompanimentB();
                  //  AccompanimentPianoPart.empty();
                   // AccompanimentPhrase.empty();
                   // AccompanimentScore.empty();
                    //composeChordProgression();
                    if(!queueSections.isEmpty())
                    {

                        Character s = queueSections.remove();
                        System.out.println("the section is "+s);
                        AccompanimentScore.empty();
                        AccompanimentPianoPart.empty();
                        switch (s)
                        {
                            case 'A':
                            {
                                if(rootPitch < C3)
                                {
                                    System.out.println("menor que C3");
                                    rootPitch = C4;
                                }
                                AccompanimentScore.addPart(composeSectionA());
                                break;
                            }
                            case 'B':

                                AccompanimentScore.addPart(composeSectionB());
                                break;
                        }
                        //AccompanimentScore.addPart(CP.getNextSection(s));
                        //AccompanimentScore.addPart(AccompanimentPianoPart);

                    }
                    else
                    {
                        for (char c : form.toCharArray())
                        {
                            queueSections.add(c);
                        }
                        System.out.println("Original queueSection "+queueSections);
                        Character s = queueSections.remove();
                        System.out.println("the section is "+s);
                        AccompanimentScore.empty();
                        AccompanimentPianoPart.empty();
                        switch (s)
                        {
                            case 'A':
                            {
                                if(rootPitch < C3)
                                {
                                    System.out.println("menor que C3");
                                    rootPitch = C4;
                                }
                                AccompanimentScore.addPart(composeSectionA());
                                break;
                            }
                            case 'B':
                                AccompanimentScore.addPart(composeSectionB());
                                break;
                        }
                        //AccompanimentScore.addPart(CP.getNextSection(s));
                    }
                    getDataStore().remove(ACCOMPANIMENT_SCORE);
                    getDataStore().put(ACCOMPANIMENT_SCORE,AccompanimentScore);
                    hold = 1;
                    getDataStore().remove(HOLD_COMPOSITION);
                    getDataStore().put(HOLD_COMPOSITION,hold);
                    if(getDataStore().containsKey(HOLD_PLAYBACK))
                    {
                        getDataStore().remove(HOLD_PLAYBACK);
                        getDataStore().put(HOLD_PLAYBACK,holdPlay);
                    }else
                    {
                        getDataStore().put(HOLD_PLAYBACK,holdPlay);
                    }
                    transition = 4;

                }
            }

        }


        ACLMessage replyConfirm = myAgent.receive(mt1);
        if(replyConfirm != null)
        {
            System.out.println("Agent confirm I will Compose");
            ACLMessage replyConfirmToMusician = replyConfirm.createReply();
            replyConfirmToMusician.setPerformative(ACLMessage.INFORM);
            replyConfirmToMusician.setConversationId("request-accompaniment-conversation-INFORM");
            replyConfirmToMusician.setReplyWith(replyConfirm.getSender().getLocalName()+System.currentTimeMillis());
            myAgent.send(replyConfirmToMusician);
            //AccompanimentPianoPart.empty();
            //AccompanimentPhrase.empty();
            //AccompanimentScore.empty();
            //composeChordProgression();
            //getDataStore().put(ACCOMPANIMENT_SCORE,AccompanimentScore);
            if(getDataStore().containsKey(FROM_PLAY_TO_COMPOSE))
            {
                getDataStore().remove(FROM_PLAY_TO_COMPOSE);
                getDataStore().put(FROM_PLAY_TO_COMPOSE,true);
            }else
            {
                getDataStore().put(FROM_PLAY_TO_COMPOSE,true);
            }

            if(getDataStore().containsKey(HOLD_COMPOSITION))
            {
                getDataStore().remove(HOLD_COMPOSITION);
                int holdComposition = 0;
                getDataStore().put(HOLD_COMPOSITION,holdComposition);
            }
            else
            {
                int holdComposition = 0;
                getDataStore().put(HOLD_COMPOSITION,holdComposition);
            }

            //transition = 4;

        }//else {block();}


    }


    private Part composeSectionA()
    {
        Part pianoPart = new Part("Piano Part",PIANO,2);
        CPhrase chord = new CPhrase();
        for (int i=0; i < 4; i++)
        {
            int[] pitchArray = new int[3];

            //add chord to the part
            if(i>0)
            {
                pitchArray[0] = REST;
                pitchArray[1] = REST;
                pitchArray[2] = REST;
                chord.addChord(pitchArray, C, 50);
            }else
            {
                pitchArray[0] = rootPitch;
                pitchArray[1] = rootPitch + 4;
                pitchArray[2] = rootPitch + 7;
                chord.addChord(pitchArray, C, 50);
            }

        }
        pianoPart.addCPhrase(chord);

        return pianoPart;
    }

    private Part composeSectionB()
    {
        rootPitch -=7;
        Part pianoPart = new Part("Piano Part",PIANO,2);
        CPhrase chord = new CPhrase();
        for (int i=0; i < 4; i++)
        {
            /*if()
            {
                int[] pitchArray = new int[3];
                pitchArray[0] = REST;
                pitchArray[1] = REST;
                pitchArray[2] = REST;
                chord.addChord(pitchArray, C, 50);

            }else
            {
                int[] pitchArray = new int[3];
                pitchArray[0] = rootPitch;
                pitchArray[1] = rootPitch + 4;
                pitchArray[2] = rootPitch + 7;
                chord.addChord(pitchArray, C, 50);
            }*/

            //add chord to the part
            int[] pitchArray = new int[3];
            pitchArray[0] = rootPitch;
            pitchArray[1] = rootPitch + 4;
            pitchArray[2] = rootPitch + 7;
            chord.addChord(pitchArray, C, 50);

        }
        pianoPart.addCPhrase(chord);
        rootPitch += 5;
        return pianoPart;

    }


    public int onEnd()
    {
        firstTimeHere++;

        return transition;
    }


}
