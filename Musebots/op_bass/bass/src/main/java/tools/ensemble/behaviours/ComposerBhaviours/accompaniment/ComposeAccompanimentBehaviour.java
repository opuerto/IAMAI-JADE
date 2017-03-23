package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jm.JMC;
import jm.music.data.*;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ChordsAttributes;

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
    private Part AccompanimentBassPart = new Part("Accompaniment Bass",BASS,3);
    private String form;
    private Queue<Character> queueSections = new LinkedList<Character>();
    private Queue<Integer> queueSectionIndex = new LinkedList<Integer>();
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

            for(int i = 0; i < form.length(); i++)
            {
                queueSections.add(form.charAt(i));
                queueSectionIndex.add(i);
            }
            System.out.println("Original queueSection "+queueSections);
            System.out.println("Original queueIndex "+queueSectionIndex);


        }
        if(getDataStore().containsKey(FROM_PLAY_TO_COMPOSE))
        {


            if(getDataStore().containsKey(HOLD_COMPOSITION))
            {
                //This is a flag that allows to avoid this process while we are playing a section
                int hold = (Integer) getDataStore().get(HOLD_COMPOSITION);
                //We set to zero this flat and will be trigger the process of play this composition in the next state
                int holdPlay = 0;
                if (hold < 1)
                {

                    if(!queueSections.isEmpty())
                    {

                        Character s = queueSections.remove();
                        Integer sIndex = queueSectionIndex.remove();
                        System.out.println("the section is "+s);
                        System.out.println("the index is "+sIndex);
                        //Store the next section to be played
                        if(getDataStore().containsKey(NEXT_SECTION_TO_PLAY) && getDataStore().containsKey(NEXT_SECTION_TO_PLAY))
                        {
                            getDataStore().remove(NEXT_SECTION_TO_PLAY);
                            getDataStore().put(NEXT_SECTION_TO_PLAY,s);
                            getDataStore().remove(NEXT_SECTION_INDEX);
                            getDataStore().put(NEXT_SECTION_INDEX,sIndex);
                        }else
                        {
                            getDataStore().put(NEXT_SECTION_TO_PLAY,s);
                            getDataStore().put(NEXT_SECTION_INDEX,sIndex);
                        }
                        AccompanimentScore.empty();
                        AccompanimentBassPart.empty();
                        switch (s)
                        {
                            case 'A':

                                AccompanimentScore.addPart(composeSectionA());
                                break;

                            case 'B':

                                AccompanimentScore.addPart(composeSectionB());
                                break;
                        }
                        //AccompanimentScore.addPart(CP.getNextSection(s));
                        //AccompanimentScore.addPart(AccompanimentBassPart);

                    }
                    else
                    {
                        for(int i = 0; i < form.length(); i++)
                        {
                            queueSections.add(form.charAt(i));
                            queueSectionIndex.add(i);
                        }
                        System.out.println("Original queueSection "+queueSections);
                        Character s = queueSections.remove();
                        Integer sIndex = queueSectionIndex.remove();
                        System.out.println("the section is "+s);
                        System.out.println("the index is "+sIndex);
                        if(getDataStore().containsKey(NEXT_SECTION_TO_PLAY) && getDataStore().containsKey(NEXT_SECTION_TO_PLAY))
                        {
                            getDataStore().remove(NEXT_SECTION_TO_PLAY);
                            getDataStore().put(NEXT_SECTION_TO_PLAY,s);
                            getDataStore().remove(NEXT_SECTION_INDEX);
                            getDataStore().put(NEXT_SECTION_INDEX,sIndex);
                        }else
                        {
                            getDataStore().put(NEXT_SECTION_TO_PLAY,s);
                            getDataStore().put(NEXT_SECTION_INDEX,sIndex);
                        }
                        AccompanimentScore.empty();
                        AccompanimentBassPart.empty();
                        switch (s)
                        {
                            case 'A':
                            {
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


       /* ACLMessage replyConfirm = myAgent.receive(mt1);
        if(replyConfirm != null)
        {
            System.out.println("Agent confirm I will Compose");
            ACLMessage replyConfirmToMusician = replyConfirm.createReply();
            replyConfirmToMusician.setPerformative(ACLMessage.INFORM);
            replyConfirmToMusician.setConversationId("request-accompaniment-conversation-INFORM");
            replyConfirmToMusician.setReplyWith(replyConfirm.getSender().getLocalName()+System.currentTimeMillis());
            myAgent.send(replyConfirmToMusician);
            //AccompanimentBassPart.empty();
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

        }else {block();}*/


    }


    private Part composeSectionA()
    {
        //Part bassPart = new Part("Bass Part",BASS,3);
        Phrase phrase = new Phrase();
        
        int size = Musician.sectionAchords.size();
        for(int i = 0; i < size; i++)
        {
            //rootPitch = ((Long) Musician.sectionAchords.get(i)).intValue();
            ChordsAttributes chordAttrbute = (ChordsAttributes) Musician.sectionAchords.get(i);
            System.out.println(chordAttrbute);
            rootPitch = chordAttrbute.getRootPitch()-24;
            String noteType = chordAttrbute.getMajorOrMinor();
            int extension = chordAttrbute.getExtension();

            for(int j = 0; j < Musician.timeSignatureDenominator; j++)
            {

                if(j<1)
                {
                    phrase.add(new Note(rootPitch,C,60));
                }
                else
                {
                    phrase.add(new Note(rootPitch+3,C,60));
                }
                //phrase.add(new Note(rootPitch-2,C,80));
                //phrase.add(new Note(rootPitch-3,C,80));
                //phrase.add(new Note(rootPitch-5,C,80));
            }
        }

        AccompanimentBassPart.addPhrase(phrase);

        return AccompanimentBassPart;
    }
    private Part composeSectionB()
    {

        Part bassPart = new Part("Bass Part",BASS,3);
        Phrase phrase = new Phrase();

        int size = Musician.sectionBchords.size();
        for(int i = 0; i < size; i++)
        {
            //rootPitch = ((Long) Musician.sectionAchords.get(i)).intValue();
            ChordsAttributes chordAttrbute = (ChordsAttributes) Musician.sectionBchords.get(i);
            System.out.println(chordAttrbute);
            rootPitch = chordAttrbute.getRootPitch()-24;
            String noteType = chordAttrbute.getMajorOrMinor();
            int extension = chordAttrbute.getExtension();

            for(int j = 0; j < Musician.timeSignatureDenominator; j++)
            {
                phrase.add(new Note(rootPitch,C,60));
                //if(j<1)
                //{
                //    phrase.add(new Note(rootPitch,C,80));
               // }
               // else
                //{
                //    phrase.add(new Note(REST,C,80));
               // }

                //phrase.add(new Note(rootPitch+4,C,80));
                //phrase.add(new Note(rootPitch+7,C,80));
                //phrase.add(new Note(rootPitch+4,C,80));

            }
        }

        AccompanimentBassPart.addPhrase(phrase);
        return AccompanimentBassPart;

    }






    public int onEnd()
    {
        firstTimeHere++;

        return transition;
    }


}
