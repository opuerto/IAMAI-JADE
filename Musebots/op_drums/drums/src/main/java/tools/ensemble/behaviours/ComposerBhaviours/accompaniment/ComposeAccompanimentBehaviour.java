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
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ChordsAttributes;

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
    private Part ridePart = new Part("Drums ride", 0, 9);
    private Part snarePart = new Part("Drums snare", 0, 9);
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
                        //Store the next section to be played
                        if(getDataStore().containsKey(NEXT_SECTION_TO_PLAY))
                        {
                            getDataStore().remove(NEXT_SECTION_TO_PLAY);
                            getDataStore().put(NEXT_SECTION_TO_PLAY,s);
                        }else
                        {
                            getDataStore().put(NEXT_SECTION_TO_PLAY,s);
                        }
                        AccompanimentScore.empty();
                        snarePart.empty();
                        ridePart.empty();
                        switch (s)
                        {
                            case 'A':

                                AccompanimentScore.addPart(composeSectionA(snarePart.getTitle()));
                                AccompanimentScore.addPart(composeSectionA(ridePart.getTitle()));
                                break;

                            case 'B':

                                AccompanimentScore.addPart(composeSectionB(snarePart.getTitle()));
                                AccompanimentScore.addPart(composeSectionB(ridePart.getTitle()));
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
                       snarePart.empty();
                        ridePart.empty();
                        switch (s)
                        {
                            case 'A':
                            {

                                AccompanimentScore.addPart(composeSectionA(snarePart.getTitle()));
                                AccompanimentScore.addPart(composeSectionA(ridePart.getTitle()));
                                break;
                            }
                            case 'B':
                                AccompanimentScore.addPart(composeSectionB(snarePart.getTitle()));
                                AccompanimentScore.addPart(composeSectionB(ridePart.getTitle()));
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


    private Part composeSectionA(String partName)
    {
        if(partName.equals("Drums ride"))
        {
            int size = Musician.sectionAchords.size();
            for(int i = 0; i < size; i++)
            {
                ridePart.addPhrase(swingTime());

            }
            return ridePart;
        }
        int size = Musician.sectionAchords.size();
        for(int i = 0; i < size; i++)
        {
            snarePart.addPhrase(swingAccents());

        }
        return snarePart;

    }
    private Part composeSectionB(String partName)
    {
        if(partName.equals("Drums ride"))
        {
            int size = Musician.sectionBchords.size();
            for(int i = 0; i < size; i++)
            {
                ridePart.addPhrase(swingTime());

            }
            return ridePart;
        }
        int size = Musician.sectionBchords.size();
        for(int i = 0; i < size; i++)
        {
            snarePart.addPhrase(swingAccents());

        }
        return snarePart;
    }

    private Phrase swingTime() {
        // build the ride line
        Phrase phr = new Phrase();
        int ride = 51;
        phr.addNote(new Note(ride, C,50));
        phr.addNote(new Note(ride, 0.67,50));
        phr.addNote(new Note(ride, 0.33,50));
        phr.addNote(new Note(ride, C,50));
        phr.addNote(new Note(ride, C,50));
        return phr;
    }
    public static Phrase swingAccents() {
        // build the bass line from the rootPitch
        Phrase phr = new Phrase();
        int snare = 38;
        for (int i=0;i<4;i++) {
            phr.addNote(new Note(REST, 0.67,50));
            phr.addNote(new Note(snare, 0.33,
                    (int)(Math.random()*60)));
        }
        return phr;
    }


    //Compose the chords depending in the attributes.
    private  int[] getChordsForPhrase(int pitch,int extension, String type)
    {
        int[] pitchArray = new int[3];
        if(type.equals("m") && extension == 7)
        {
            int[] pitchArrayMinor7 = new int[4];
            pitchArrayMinor7[0] = pitch;
            pitchArrayMinor7[1] = pitch + 3;
            pitchArrayMinor7[2] = pitch + 7;
            pitchArrayMinor7[3] = pitch + 10;
            return pitchArrayMinor7;
        }
        else if(type.equals("M") && extension == 7)
        {
            int[] pitchArrayMajor7 = new int[4];
            pitchArrayMajor7[0] = pitch;
            pitchArrayMajor7[1] = pitch + 4;
            pitchArrayMajor7[2] = pitch + 7;
            pitchArrayMajor7[3] = pitch + 11;
            return pitchArrayMajor7;

        }
        else if(type.equals("D") && extension == 7)
        {
            int[] pitchArrayDominant7 = new int[4];
            pitchArrayDominant7[0] = pitch;
            pitchArrayDominant7[1] = pitch + 4;
            pitchArrayDominant7[2] = pitch + 7;
            pitchArrayDominant7[3] = pitch + 10;
            return pitchArrayDominant7;

        }
        else
        {
            pitchArray[0] = pitch;
            pitchArray[1] = rootPitch + 4;
            pitchArray[2] = rootPitch + 7;
        }

        return pitchArray;
    }



    public int onEnd()
    {
        firstTimeHere++;

        return transition;
    }


}
