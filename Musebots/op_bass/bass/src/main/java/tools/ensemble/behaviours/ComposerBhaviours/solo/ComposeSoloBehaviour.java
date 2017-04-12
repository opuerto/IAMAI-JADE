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
    private Part saxPart = new Part("Sax PArt",PIANO,1);
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
                saxPart.empty();
                switch (Composer.NextSectionSoloCharacter)
                {
                    case 'A':
                        Composer.getSoloScore().setTempo(Musician.getTempo());
                        Composer.getSoloScore().addPart(composeSectionA());
                        break;
                    case 'B':
                        Composer.getSoloScore().setTempo(Musician.getTempo());
                        Composer.getSoloScore().addPart(composeSectionB());
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
                saxPart.empty();
                switch (Composer.NextSectionSoloCharacter)
                {
                    case 'A':
                        Composer.getSoloScore().setTempo(Musician.getTempo());
                        Composer.getSoloScore().addPart(composeSectionA());
                        break;
                    case 'B':
                        Composer.getSoloScore().setTempo(Musician.getTempo());
                        Composer.getSoloScore().addPart(composeSectionB());
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
                //phrase.add(new Note(rootPitch-2,C,80));
                //phrase.add(new Note(rootPitch-3,C,80));
                //phrase.add(new Note(rootPitch-5,C,80));
            }
        }


        saxPart.addPhrase(phrase);
        /*double now = System.currentTimeMillis();
        double durationSong;
        durationSong = (now - TimeManager.alive)/60;
        double durationS = durationSong/1000;

        double numberofMeasure = (Musician.tempo * durationS)/Musician.timeSignatureNumerator;
        System.out.println("number of measure "+numberofMeasure);
        phrase.setStartTime(numberofMeasure);
        TimeManager.MainScore.setTempo(Musician.tempo);
        TimeManager.SAXPART.addPhrase(phrase);*/

        return saxPart;
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
                //TimeManager.MainPhrase.add(new Note(rootPitch,QUARTER_NOTE,60));
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

        saxPart.addPhrase(phrase);
        /*double now = System.currentTimeMillis();
        double durationSong;
        durationSong = (now - TimeManager.alive)/60;
        double durationS = durationSong/1000;

        double numberofMeasure = (Musician.tempo * durationS)/Musician.timeSignatureNumerator;
        System.out.println("number of measure "+numberofMeasure);
        phrase.setStartTime(numberofMeasure);
        TimeManager.MainScore.setTempo(Musician.tempo);
        TimeManager.SAXPART.addPhrase(phrase);*/
        return saxPart;

    }


}
