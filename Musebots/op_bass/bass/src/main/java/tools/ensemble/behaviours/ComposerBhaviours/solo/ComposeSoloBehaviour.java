package tools.ensemble.behaviours.ComposerBhaviours.solo;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import tools.ensemble.agents.Composer;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ChordsAttributes;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by OscarAlfonso on 3/26/2017.
 */
public class ComposeSoloBehaviour extends OneShotBehaviour implements DataStoreComposer, JMC {

    private int transition = 8;
    private int firstTimeHere = 0;
    private Section sectionInfo;
    private String form;
    private Queue<Character> queueSections = new LinkedList<Character>();
    private Queue<Integer> queueSectionIndex = new LinkedList<Integer>();
    private Part bassPart = new Part("Bass PArt",BASS,1);
    private int rootPitch;

    public ComposeSoloBehaviour(Agent a)
    {
        super(a);
    }

     public void onStart()
     {
         System.out.println("on Start "+getBehaviourName());
         queueSections.clear();
         queueSectionIndex.clear();
         transition = 8;
         if(getDataStore().containsKey(FIRST_TIME_SOLO))
         {
             Composer.setFirstTimePlayingSolo((Integer) getDataStore().get(FIRST_TIME_SOLO));
         }
         form = Musician.getTuneForm();

         //We want omit the section that is currently playing and start to soloing from the next section.
         if(Composer.getFirstTimePlayingSolo() > 0)
         {
             //Reset the measure counter
             Composer.setMeasureCounter(0);
             System.out.println("Im first time solo");
             if (getDataStore().containsKey(SECTION_INSTANCE_FOR_SYN_SOLO))
             {
                 sectionInfo = (Section) getDataStore().get(SECTION_INSTANCE_FOR_SYN_SOLO);
             }
             for (int i = 0; i<form.length(); i++)
             {
                 if(i > sectionInfo.getSectionIndex())
                 {
                     queueSections.add(form.charAt(i));
                     queueSectionIndex.add(i);
                 }

             }
             System.out.println("Original queueSection "+queueSections);
             System.out.println("Original queueIndex "+queueSectionIndex);
             Composer.setFirstTimePlayingSolo(0);
         }
         else
         {
             System.out.println("Im not first time playing any more");

             for (int i = 0; i<form.length(); i++)
             {
                 queueSections.add(form.charAt(i));
                 queueSectionIndex.add(i);
             }
             System.out.println("Original queueSection in compose "+queueSections);
             System.out.println("Original queueIndex in compose"+queueSectionIndex);

         }


     }

    public void action()
    {
        if (firstTimeHere < 1)
        {



        }

        if(Composer.getHoldSoloComposition() < 1)
        {
            Composer.setHoldSoloPlayback(0);
            if(!queueSections.isEmpty())
            {
                Composer.NextSectionSoloCharacter = queueSections.remove();
                Composer.NextSectionSoloIndex = queueSectionIndex.remove();
                System.out.println("the section is in compose state " +  Composer.NextSectionSoloCharacter);
                System.out.println("the index is in compose" + Composer.NextSectionSoloIndex);
                System.out.println("the reminder in section"  +queueSections);

                Composer.getSoloScore().empty();
                bassPart.empty();
                switch (Composer.NextSectionSoloCharacter)
                {
                    case 'A':
                        Composer.getSoloScore().setTempo(Musician.getTempo());
                        Composer.getSoloScore().addPart(randomWalkSectionA());
                        break;
                    case 'B':
                        Composer.getSoloScore().setTempo(Musician.getTempo());
                        Composer.getSoloScore().addPart(randomWalkSectionB());
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
                System.out.println("fill it again");
                System.out.println("Original queueSection "+queueSections);
                System.out.println("Original queueIndex "+queueSectionIndex);
                Composer.NextSectionSoloCharacter = queueSections.remove();
                Composer.NextSectionSoloIndex = queueSectionIndex.remove();
                System.out.println("the section is " +  Composer.NextSectionSoloCharacter);
                System.out.println("the index is " + Composer.NextSectionSoloIndex);
                Composer.getSoloScore().empty();
                bassPart.empty();
                switch (Composer.NextSectionSoloCharacter)
                {
                    case 'A':
                        Composer.getSoloScore().setTempo(Musician.getTempo());
                        Composer.getSoloScore().addPart(randomWalkSectionA());
                        break;
                    case 'B':
                        Composer.getSoloScore().setTempo(Musician.getTempo());
                        Composer.getSoloScore().addPart(randomWalkSectionB());
                        break;
                }

            }

            Composer.holdSoloComposition = 1;
            if( !Musician.getLeader())
            {
                System.out.println("stop playing solo you are not a leader");
                transition = 18;
                System.out.println("Transition is "+transition);
            }else
            {
                transition = 9;
                System.out.println("go to play that section transition is "+transition);
            }
        }

    }

    public int onEnd()
    {
        firstTimeHere++;
        return transition;
    }

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
            rootPitch = chordAttrbute.getRootPitch();
            String noteType = chordAttrbute.getMajorOrMinor();
            int extension = chordAttrbute.getExtension();

            for(int j = 0; j < Musician.getTimeSignatureDenominator(); j++)
            {

                if(j<1)
                {
                    phrase.add(new Note(rootPitch,QUARTER_NOTE,60));

                }
                else
                {
                    phrase.add(new Note(rootPitch+3,QUARTER_NOTE,60));

                }

            }
        }


        bassPart.addPhrase(phrase);


        return bassPart;
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
            rootPitch = chordAttrbute.getRootPitch();
            String noteType = chordAttrbute.getMajorOrMinor();
            int extension = chordAttrbute.getExtension();

            for(int j = 0; j < Musician.getTimeSignatureDenominator(); j++)
            {
                phrase.add(new Note(rootPitch,QUARTER_NOTE,60));

            }
        }

        bassPart.addPhrase(phrase);

        return bassPart;

    }


    private Part randomWalkSectionA()
    {
        Phrase phrase = new Phrase();
        int size = Musician.sectionAchords.size();
        for(int i = 0; i < size; i++)
        {
            ChordsAttributes chordAttribute = (ChordsAttributes) Musician.getSectionAchords().get(i);
            rootPitch = chordAttribute.getRootPitch()-24;
            String noteType = chordAttribute.getMajorOrMinor();
            int extension = chordAttribute.getExtension();
            // build the rhythms
            double[] rhythm1 = {0.30,0.70,0.30,0.70,0.30,0.70,0.30};
            double[] rhythm2 = {0.30,0.70,0.30,0.70,1.34};
            double[] rhythm3 = {1.0,0.30,0.70,0.30,1.0};
            double[] rhythm4 = {0.30,0.70,1.0,1.34};
            int temp = 0;
            boolean ok = false;
            int rhythmNumb = (int)(Math.random() *4);
            int rhythmLength = 0;
            int intervalNumb = 0;
            //choose a rhythm to use for the phrase
            if (rhythmNumb == 0) rhythmLength = rhythm1.length;
            if (rhythmNumb == 1) rhythmLength = rhythm2.length;
            if (rhythmNumb == 2) rhythmLength = rhythm3.length;
            if (rhythmNumb == 3) rhythmLength = rhythm4.length;

            for(int j = 0; j < Musician.getTimeSignatureDenominator(); j++)
            {
                phrase.addNote(new Note(REST, 0.70));
                for (int k = 0; k < rhythmLength; k++)
                {
                    while (ok == false)
                    {
                        int intervalLenght = getNewInterval(rootPitch,extension,noteType).length;
                        int[] p = new int[intervalLenght];
                        p =  getNewInterval(rootPitch,extension,noteType);
                        intervalNumb = (int)(Math.random()*intervalLenght);
                        if (temp != p[intervalNumb])
                        {
                            temp = p[intervalNumb];
                            ok = true;
                            break;
                        }

                    }
                    //add the next note to the phrase
                    if (rhythmNumb == 0) phrase.addNote(
                            new Note(temp, rhythm1[k]));
                    if (rhythmNumb == 1) phrase.addNote(
                            new Note(temp, rhythm2[k]));
                    if (rhythmNumb == 2) phrase.addNote(
                            new Note(temp, rhythm3[k]));
                    if (rhythmNumb == 3) phrase.addNote(
                            new Note(temp, rhythm4[k]));
                    ok = false;
                }


            }

        }
        bassPart.add(phrase);
        return bassPart;
    }

    private Part randomWalkSectionB()
    {
        Phrase phrase = new Phrase();
        int size = Musician.sectionBchords.size();
        for(int i = 0; i < size; i++)
        {
            ChordsAttributes chordAttribute = (ChordsAttributes) Musician.getSectionAchords().get(i);
            rootPitch = chordAttribute.getRootPitch()-24;
            String noteType = chordAttribute.getMajorOrMinor();
            int extension = chordAttribute.getExtension();
            // build the rhythms
            double[] rhythm1 = {0.30,0.70,0.30,0.70,0.30,0.70,0.30};
            double[] rhythm2 = {0.30,0.70,0.30,0.70,1.34};
            double[] rhythm3 = {1.0,0.30,0.70,0.30,1.0};
            double[] rhythm4 = {0.30,0.70,1.0,1.34};
            int temp = 0;
            boolean ok = false;
            int rhythmNumb = (int)(Math.random() *4);
            int rhythmLength = 0;
            int intervalNumb = 0;
            //choose a rhythm to use for the phrase
            if (rhythmNumb == 0) rhythmLength = rhythm1.length;
            if (rhythmNumb == 1) rhythmLength = rhythm2.length;
            if (rhythmNumb == 2) rhythmLength = rhythm3.length;
            if (rhythmNumb == 3) rhythmLength = rhythm4.length;

            for(int j = 0; j < Musician.getTimeSignatureDenominator(); j++)
            {
                phrase.addNote(new Note(REST, 0.66));
                for (int k = 0; k < rhythmLength; k++)
                {
                    while (ok == false)
                    {
                        int intervalLenght = getNewInterval(rootPitch,extension,noteType).length;
                        int[] p = new int[intervalLenght];
                        p =  getNewInterval(rootPitch,extension,noteType);
                        intervalNumb = (int)(Math.random()*intervalLenght);
                        if (temp != p[intervalNumb])
                        {
                            temp = p[intervalNumb];
                            ok = true;
                            break;
                        }

                    }
                    //add the next note to the phrase
                    if (rhythmNumb == 0) phrase.addNote(
                            new Note(temp, rhythm1[k]));
                    if (rhythmNumb == 1) phrase.addNote(
                            new Note(temp, rhythm2[k]));
                    if (rhythmNumb == 2) phrase.addNote(
                            new Note(temp, rhythm3[k]));
                    if (rhythmNumb == 3) phrase.addNote(
                            new Note(temp, rhythm4[k]));
                    ok = false;
                }


            }

        }
        bassPart.add(phrase);
        return bassPart;
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


}
