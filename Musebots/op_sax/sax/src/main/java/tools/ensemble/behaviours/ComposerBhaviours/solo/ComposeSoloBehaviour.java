package tools.ensemble.behaviours.ComposerBhaviours.solo;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import tools.ensemble.agents.Composer;
import tools.ensemble.agents.Musician;
import tools.ensemble.agents.TimeManager;
import tools.ensemble.behaviours.ComposerBhaviours.accompaniment.ComposeAccompanimentBehaviour;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ChordsAttributes;
import tools.ensemble.ontologies.timemanager.TimeHandler;
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
    private Part saxPart = new Part("Sax PArt",SAXOPHONE,1);
    private int rootPitch;

    public ComposeSoloBehaviour(Agent a)
    {
        super(a);
    }

    public void action()
    {
        if (firstTimeHere < 1)
        {

            if(getDataStore().containsKey(FIRST_TIME_SOLO))
            {
                Composer.firstTimePlayingSolo = (Integer) getDataStore().get(FIRST_TIME_SOLO);
            }
            form = Musician.tuneForm;

           //We want omit the section that is currently playing and start to soloing from the next section.
            if(Composer.firstTimePlayingSolo > 0)
            {
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
                Composer.firstTimePlayingSolo = 0;
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

        if(Composer.holdSoloComposition < 1)
        {
            Composer.holdSoloPlayback = 0;
            if(!queueSections.isEmpty())
            {
                Composer.NextSectionSoloCharacter = queueSections.remove();
                Composer.NextSectionSoloIndex = queueSectionIndex.remove();
                System.out.println("the section is in compose state " +  Composer.NextSectionSoloCharacter);
                System.out.println("the index is in compose" + Composer.NextSectionSoloIndex);
                System.out.println("the reminder in section"  +queueSections);

                Composer.SoloSaxScore.empty();
                saxPart.empty();
                switch (Composer.NextSectionSoloCharacter)
                {
                    case 'A':
                        Composer.SoloSaxScore.setTempo(Musician.getTempo());
                        Composer.SoloSaxScore.addPart(composeSectionA());
                        break;
                    case 'B':
                        Composer.SoloSaxScore.setTempo(Musician.getTempo());
                        Composer.SoloSaxScore.addPart(composeSectionB());
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
                Composer.SoloSaxScore.empty();
                saxPart.empty();
                switch (Composer.NextSectionSoloCharacter)
                {
                    case 'A':
                        Composer.SoloSaxScore.setTempo(Musician.getTempo());
                        Composer.SoloSaxScore.addPart(composeSectionA());
                        break;
                    case 'B':
                        Composer.SoloSaxScore.setTempo(Musician.getTempo());
                        Composer.SoloSaxScore.addPart(composeSectionB());
                        break;
                }

            }

            Composer.holdSoloComposition = 1;
            transition = 9;
            //If Im not the leader I cant continue playing the solo
            /*if(Composer.NextSectionSoloIndex == 0 && !Musician.leader)
            {
                transition = 18;
            }
            else
            {
                transition = 9;
            }*/
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
