package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jm.JMC;
import jm.music.data.*;
import jm.util.Play;
import tools.ensemble.agents.Composer;
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
            //AccompanimentScore.setTempo(Musician.tempo);
            Composer.accompanimentScore.setTempo(Musician.tempo);

            for(int i = 0; i < form.length(); i++)
            {
                queueSections.add(form.charAt(i));
                queueSectionIndex.add(i);
            }
            System.out.println("Original queueSection "+queueSections);
            System.out.println("Original queueIndex "+queueSectionIndex);


        }
       /* if(getDataStore().containsKey(FROM_PLAY_TO_COMPOSE))
        {*/


        if(Composer.holdComposition < 1)
        {
            //This is a flag that allows to avoid this process while we are playing a section
            //int hold = (Integer) getDataStore().get(HOLD_COMPOSITION);
            //We set to zero this flat and will be trigger the process of play this composition in the next state
            //int holdPlay = 0;
            Composer.holdPlay = 0;
            if (Composer.holdComposition < 1)
            {
                if(!queueSections.isEmpty()) {


                    //Character s = queueSections.remove();
                    // sIndex = queueSectionIndex.remove();
                    Composer.NextsectionCharacter = queueSections.remove();
                    Composer.NextsectionIndex = queueSectionIndex.remove();
                    System.out.println("the section is " + Composer.NextsectionCharacter);
                    System.out.println("the index is " + Composer.NextsectionIndex);
                    //Store the next section to be played
                                /*if (getDataStore().containsKey(NEXT_SECTION_TO_PLAY) && getDataStore().containsKey(NEXT_SECTION_TO_PLAY)) {
                                    getDataStore().remove(NEXT_SECTION_TO_PLAY);
                                    getDataStore().put(NEXT_SECTION_TO_PLAY, s);
                                    getDataStore().remove(NEXT_SECTION_INDEX);
                                    getDataStore().put(NEXT_SECTION_INDEX, sIndex);
                                } else {
                                    getDataStore().put(NEXT_SECTION_TO_PLAY, s);
                                    getDataStore().put(NEXT_SECTION_INDEX, sIndex);
                                }*/
                    //AccompanimentScore.empty();
                    Composer.accompanimentScore.empty();
                    AccompanimentBassPart.empty();
                    switch (Composer.NextsectionCharacter) {
                        case 'A':

                            //AccompanimentScore.setTempo(Musician.tempo);
                            //AccompanimentScore.addPart(composeSectionA());
                            Composer.accompanimentScore.setTempo(Musician.tempo);
                            Composer.accompanimentScore.add(composeSectionA());
                            break;

                        case 'B':
                            //AccompanimentScore.setTempo(Musician.tempo);
                            //AccompanimentScore.addPart(composeSectionB());
                            Composer.accompanimentScore.setTempo(Musician.tempo);
                            Composer.accompanimentScore.addPart(composeSectionB());
                            break;
                    }
                }
                else
                {

                    for(int i = 0; i < form.length(); i++)
                    {
                        queueSections.add(form.charAt(i));
                        queueSectionIndex.add(i);
                    }
                    System.out.println("Original queueSection "+queueSections);
                    System.out.println("Original queueIndex "+queueSectionIndex);
                    //Character s = queueSections.remove();
                    //sIndex = queueSectionIndex.remove();
                    Composer.NextsectionCharacter = queueSections.remove();
                    Composer.NextsectionIndex = queueSectionIndex.remove();
                    System.out.println("the section is "+Composer.NextsectionCharacter );
                    System.out.println("the index is "+Composer.NextsectionIndex);
                       /* if(getDataStore().containsKey(NEXT_SECTION_TO_PLAY) && getDataStore().containsKey(NEXT_SECTION_TO_PLAY))
                        {
                            getDataStore().remove(NEXT_SECTION_TO_PLAY);
                            getDataStore().put(NEXT_SECTION_TO_PLAY,s);
                            getDataStore().remove(NEXT_SECTION_INDEX);
                            getDataStore().put(NEXT_SECTION_INDEX,sIndex);
                        }else
                        {
                            getDataStore().put(NEXT_SECTION_TO_PLAY,s);
                            getDataStore().put(NEXT_SECTION_INDEX,sIndex);
                        }*/
                    Composer.accompanimentScore.empty();
                    AccompanimentBassPart.empty();
                    switch ( Composer.NextsectionCharacter)
                    {
                        case 'A':
                        {

                            Composer.accompanimentScore.setTempo(Musician.tempo);
                            Composer.accompanimentScore.add(composeSectionA());
                            break;
                        }
                        case 'B':
                            Composer.accompanimentScore.setTempo(Musician.tempo);
                            Composer.accompanimentScore.add(composeSectionB());
                            break;
                    }
                    //AccompanimentScore.addPart(CP.getNextSection(s));
                }
                //getDataStore().remove(ACCOMPANIMENT_SCORE);
                //getDataStore().put(ACCOMPANIMENT_SCORE,AccompanimentScore);
                //hold = 1;
                Composer.holdComposition = 1;
                //getDataStore().remove(HOLD_COMPOSITION);
                //getDataStore().put(HOLD_COMPOSITION,hold);
                // if(getDataStore().containsKey(HOLD_PLAYBACK))
                //{
                //   getDataStore().remove(HOLD_PLAYBACK);
                //   getDataStore().put(HOLD_PLAYBACK,holdPlay);
                //}else
                // {
                //    getDataStore().put(HOLD_PLAYBACK,holdPlay);
                // }

                //If I'm a leader I cant continue playing accompaniment.
                if(Composer.NextsectionIndex == 0 && Musician.leader)
                {
                    transition = 3;
                }else
                {
                    transition = 4;
                }




            }
        }

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
