package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

import com.sun.org.apache.regexp.internal.RE;
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

    //private Score AccompanimentScore = new Score("Accompaniment Score");

    private Phrase AccompanimentPhrase = new Phrase("Accompaniment Phrase");
    private Part AccompanimentPianoPart = new Part("Accompaniment Piano",PIANO,2);
    private String form;
    private int rootPitch;
    private Queue<Character> queueSections = new LinkedList<Character>();
    private Queue<Integer> queueSectionIndex = new LinkedList<Integer>();
    private int sIndex;



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

            form = Musician.getTuneForm();
            //AccompanimentScore.setTempo(Musician.tempo);
            Composer.getAccompanimentScore().setTempo(Musician.getTempo());

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


            if(Composer.getHoldComposition() < 1)
            {
                //This is a flag that allows to avoid this process while we are playing a section
                //int hold = (Integer) getDataStore().get(HOLD_COMPOSITION);
                //We set to zero this flat and will be trigger the process of play this composition in the next state
                //int holdPlay = 0;
                Composer.setHoldPlay(0);
                if (Composer.getHoldComposition() < 1)
                {
                    if(!queueSections.isEmpty()) {


                        //Character s = queueSections.remove();
                        // sIndex = queueSectionIndex.remove();
                        Composer.setNextsectionCharacter(queueSections.remove());
                        Composer.setNextSectionIndex(queueSectionIndex.remove());
                                System.out.println("the section is " + Composer.getNextsectionCharacter());
                                System.out.println("the index is " + Composer.getNextsectionIndex());
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
                                Composer.getAccompanimentScore().empty();
                                AccompanimentPianoPart.empty();
                                switch (Composer.getNextsectionCharacter()) {
                                    case 'A':

                                        //AccompanimentScore.setTempo(Musician.tempo);
                                        //AccompanimentScore.addPart(composeSectionA());
                                        Composer.getAccompanimentScore().setTempo(Musician.getTempo());
                                        Composer.getAccompanimentScore().add(composeSectionA());
                                        break;

                                    case 'B':
                                        //AccompanimentScore.setTempo(Musician.tempo);
                                        //AccompanimentScore.addPart(composeSectionB());
                                        Composer.getAccompanimentScore().setTempo(Musician.getTempo());
                                        Composer.getAccompanimentScore().addPart(composeSectionB());
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
                        Composer.setNextsectionCharacter(queueSections.remove());
                        Composer.setNextSectionIndex(queueSectionIndex.remove());
                        System.out.println("the section is "+Composer.getNextsectionCharacter() );
                        System.out.println("the index is "+Composer.getNextsectionIndex());
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
                        Composer.getAccompanimentScore().empty();
                        AccompanimentPianoPart.empty();
                        switch ( Composer.NextsectionCharacter)
                        {
                            case 'A':
                            {

                                Composer.getAccompanimentScore().setTempo(Musician.getTempo());
                                Composer.getAccompanimentScore().addPart(composeSectionA());
                                break;
                            }
                            case 'B':
                                Composer.getAccompanimentScore().setTempo(Musician.getTempo());
                                Composer.getAccompanimentScore().addPart(composeSectionB());
                                break;
                        }
                        //AccompanimentScore.addPart(CP.getNextSection(s));
                    }
                    //getDataStore().remove(ACCOMPANIMENT_SCORE);
                    //getDataStore().put(ACCOMPANIMENT_SCORE,AccompanimentScore);
                    //hold = 1;
                    Composer.setHoldComposition(1);
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
                        if(Musician.getLeader())
                        {
                            Composer.getAccompanimentScore().empty();
                            transition = 3;
                        }else
                        {
                            transition = 4;
                        }




                }
            }

        }


    //}


    private Part composeSectionA()
    {
        Part pianoPart = new Part("Piano Part",PIANO,2);
        CPhrase chord = new CPhrase();
        int size = Musician.getSectionAchords().size();
        for(int i = 0; i < size; i++)
        {
            //rootPitch = ((Long) Musician.sectionAchords.get(i)).intValue();
            ChordsAttributes chordAttrbute = (ChordsAttributes) Musician.getSectionAchords().get(i);
            //System.out.println(chordAttrbute);
            rootPitch = chordAttrbute.getRootPitch();
            String noteType = chordAttrbute.getMajorOrMinor();
            int extension = chordAttrbute.getExtension();

            for(int j = 0; j < Musician.getTimeSignatureDenominator(); j++)
            {
                if(j>1)
                {
                    int[] restArray = new int[3];
                    restArray[0] = REST;
                    restArray[1] = REST;
                    restArray[2] = REST;
                    chord.addChord(restArray, C, 50);
                }else
                {
                    chord.addChord(getChordsForPhrase(rootPitch,extension,noteType), C, 50);
                }
            }
        }

        pianoPart.addCPhrase(chord);

        return pianoPart;
    }




   private Part composeSectionB()
    {

        Part pianoPart = new Part("Piano Part",PIANO,2);
        CPhrase chord = new CPhrase();
        int size = Musician.getSectionBchords().size();
        for(int i = 0; i < size; i++)
        {
            //rootPitch = ((Long) Musician.sectionAchords.get(i)).intValue();
            ChordsAttributes chordAttrbute = (ChordsAttributes) Musician.getSectionBchords().get(i);
            //System.out.println(chordAttrbute);
            rootPitch = chordAttrbute.getRootPitch();
            String noteType = chordAttrbute.getMajorOrMinor();
            int extension = chordAttrbute.getExtension();

            for(int j = 0; j < Musician.getTimeSignatureDenominator(); j++)
            {
                if(j>0)
                {
                    int[] restArray = new int[3];
                    restArray[0] = REST;
                    restArray[1] = REST;
                    restArray[2] = REST;
                    chord.addChord(restArray, C, 50);

                }else
                {
                    chord.addChord(getChordsForPhrase(rootPitch,extension,noteType), C, 50);
                }

            }
        }

        pianoPart.addCPhrase(chord);
        return pianoPart;

    }

    private Part composeSectionAs()
    {
        Part pianoPart = new Part("Piano Part",PIANO,2);
        //Part bassPart = new Part("Bass Part",BASS,3);
        Phrase phrase = new Phrase();

        int size = Musician.getSectionAchords().size();
        for(int i = 0; i < size; i++)
        {
            //rootPitch = ((Long) Musician.sectionAchords.get(i)).intValue();
            ChordsAttributes chordAttrbute = (ChordsAttributes) Musician.getSectionAchords().get(i);
            System.out.println(chordAttrbute);
            rootPitch = chordAttrbute.getRootPitch()-24;
            String noteType = chordAttrbute.getMajorOrMinor();
            int extension = chordAttrbute.getExtension();

            for(int j = 0; j < Musician.getTimeSignatureDenominator(); j++)
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

        pianoPart.addPhrase(phrase);

        return  pianoPart;
    }
    private Part composeSectionBs()
    {

        Part pianoPart = new Part("Piano Part",PIANO,2);
        Phrase phrase = new Phrase();

        int size = Musician.getSectionBchords().size();
        for(int i = 0; i < size; i++)
        {
            //rootPitch = ((Long) Musician.sectionAchords.get(i)).intValue();
            ChordsAttributes chordAttrbute = (ChordsAttributes) Musician.getSectionBchords().get(i);
            System.out.println(chordAttrbute);
            rootPitch = chordAttrbute.getRootPitch()-24;
            String noteType = chordAttrbute.getMajorOrMinor();
            int extension = chordAttrbute.getExtension();

            for(int j = 0; j < Musician.getTimeSignatureDenominator(); j++)
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

        pianoPart.addPhrase(phrase);
        return pianoPart;

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
        else if (type.equals("Db") && extension == 7)
        {
            int[] pitchArrayDominantB7 = new int[4];
            pitchArrayDominantB7[0] = pitch-1;
            pitchArrayDominantB7[1] = pitch + 3;
            pitchArrayDominantB7[2] = pitch + 6;
            pitchArrayDominantB7[3] = pitch + 9;
            return pitchArrayDominantB7;
        }
        else if(type.equals("M") && extension == 0)
        {
            int[] pitchArrayTriad = new int[3];
            pitchArrayTriad[0] = pitch ;
            pitchArrayTriad[1] = pitch + 4;
            pitchArrayTriad[2] = pitch + 7;
            return pitchArrayTriad;
        }
        else if (type.equals("m") && extension == 0)
        {
            int[] pitchArrayTriad = new int[3];
            pitchArrayTriad[0] = pitch ;
            pitchArrayTriad[1] = pitch + 3;
            pitchArrayTriad[2] = pitch + 7;
            return pitchArrayTriad;
        }else if(type.equals("Dsus"))
        {
            int[] pitchArrayTriad = new int[3];
            pitchArrayTriad[0] = pitch ;
            pitchArrayTriad[1] = pitch + 7;
            pitchArrayTriad[2] = pitch + 10;
            return pitchArrayTriad;
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
