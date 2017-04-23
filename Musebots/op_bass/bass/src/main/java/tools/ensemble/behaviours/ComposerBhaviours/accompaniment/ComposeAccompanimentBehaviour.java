package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jm.JMC;
import jm.music.data.*;
import tools.ensemble.agents.Composer;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ChordsAttributes;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;

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
    private Part AccompanimentBassPart = new Part("Accompaniment Piano",BASS,1);
    private String form;
    private int rootPitch;
    private Queue<Character> queueSections = new LinkedList<Character>();
    private Queue<Integer> queueSectionIndex = new LinkedList<Integer>();
    private int sIndex;
    private Section section;




    public ComposeAccompanimentBehaviour(Agent a)
    {
        super(a);
    }

    public void onStart()
    {
        System.out.println("in Start "+getBehaviourName());
        transition = 2;
        firstTimeHere = 0;
        queueSections.clear();
        queueSectionIndex.clear();
    }

    public void action()
    {

        if (firstTimeHere < 1 )
        {

            form = Musician.getTuneForm();
            //AccompanimentScore.setTempo(Musician.tempo);
            Composer.getAccompanimentScore().setTempo(Musician.getTempo());

            if(Musician.getIsFromLeadingToSupport())
            {
                System.out.println("Im coming from being solo to support the new soloist (state Compose accompaniment)");
                section = (Section) getDataStore().get(SECTION_INSTANCE_FOR_SYN_ACCOMP);
                for (int i = 0; i<form.length(); i++)
                {
                    if(i > section.getSectionIndex())
                    {
                        queueSections.add(form.charAt(i));
                        queueSectionIndex.add(i);
                    }

                }

                System.out.println("Original queueSection "+queueSections);
                System.out.println("Original queueIndex "+queueSectionIndex);

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
            }


        }


            if(Composer.getHoldComposition() < 1)
            {
                Composer.setHoldPlay(0);
                if (Composer.getHoldComposition() < 1)
                {
                    if(!queueSections.isEmpty()) {



                        Composer.setNextsectionCharacter(queueSections.remove());
                        Composer.setNextSectionIndex(queueSectionIndex.remove());
                                System.out.println("the section is " + Composer.getNextsectionCharacter());
                                System.out.println("the index is " + Composer.getNextsectionIndex());

                                Composer.getAccompanimentScore().empty();
                                AccompanimentBassPart.empty();
                                switch (Composer.getNextsectionCharacter()) {
                                    case 'A':


                                        Composer.getAccompanimentScore().setTempo(Musician.getTempo());
                                        Composer.getAccompanimentScore().add(composeSectionA());
                                        break;

                                    case 'B':

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

                        Composer.setNextsectionCharacter(queueSections.remove());
                        Composer.setNextSectionIndex(queueSectionIndex.remove());
                        System.out.println("the section is "+Composer.getNextsectionCharacter() );
                        System.out.println("the index is "+Composer.getNextsectionIndex());

                        Composer.getAccompanimentScore().empty();
                        AccompanimentBassPart.empty();
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

                    }

                    Composer.setHoldComposition(1);

                        if(Musician.getLeader())
                        {
                            Composer.getAccompanimentScore().empty();

                            transition = 3;
                            System.out.println("From Composer reset and go to wait for accompaniment request");
                            System.out.println("Transition "+transition);
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
        //Part bassPart = new Part("Bass Part",BASS,3);
        Phrase phrase = new Phrase();

        int size = Musician.sectionAchords.size();
        for(int i = 0; i < size; i++)
        {
            //rootPitch = ((Long) Musician.sectionAchords.get(i)).intValue();
            ChordsAttributes chordAttrbute = (ChordsAttributes) Musician.getSectionAchords().get(i);
            //System.out.println(chordAttrbute);
            rootPitch = chordAttrbute.getRootPitch()-24;
            String noteType = chordAttrbute.getMajorOrMinor();
            int extension = chordAttrbute.getExtension();

            for(int j = 0; j < Musician.getTimeSignatureDenominator(); j++)
            {


                int[] p = getNewInterval(rootPitch,extension,noteType);

                if(p.length == 3)
                {
                    if(j < 3)
                    {
                        phrase.add(new Note(p[j],QUARTER_NOTE,60));
                    }
                    else
                    {
                        System.out.println("We need to ad a beat here");
                        phrase.add(new Note(rootPitch,QUARTER_NOTE,60));
                    }


                }
                else
                {
                    phrase.add(new Note(p[j],QUARTER_NOTE,60));
                }

            }
        }


        AccompanimentBassPart.addPhrase(phrase);


        return AccompanimentBassPart;
    }




   private Part composeSectionB()
    {

        //Part bassPart = new Part("Bass Part",BASS,3);
        Phrase phrase = new Phrase();

        int size = Musician.getSectionBchords().size();
        for(int i = 0; i < size; i++)
        {
            //rootPitch = ((Long) Musician.sectionAchords.get(i)).intValue();
            ChordsAttributes chordAttrbute = (ChordsAttributes) Musician.getSectionBchords().get(i);
            //System.out.println(chordAttrbute);
            rootPitch = chordAttrbute.getRootPitch()-24;
            String noteType = chordAttrbute.getMajorOrMinor();
            int extension = chordAttrbute.getExtension();

            for(int j = 0; j < Musician.getTimeSignatureDenominator(); j++)
            {
                int[] p = getNewInterval(rootPitch,extension,noteType);

                if(p.length == 3)
                {
                    if(j < 3)
                    {
                        phrase.add(new Note(p[j],QUARTER_NOTE,60));
                    }
                    else
                    {
                        System.out.println("We need to ad a beat here");
                        phrase.add(new Note(rootPitch,QUARTER_NOTE,60));
                    }


                }
                else
                {
                    phrase.add(new Note(p[j],QUARTER_NOTE,60));
                }


            }
        }

        AccompanimentBassPart.addPhrase(phrase);

        return AccompanimentBassPart;


    }





    private int[] getNewInterval(int pitch, int extension, String type)
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
