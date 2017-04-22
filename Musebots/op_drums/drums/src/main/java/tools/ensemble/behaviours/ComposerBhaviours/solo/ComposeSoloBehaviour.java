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
    private Part ridePart = new Part("Drums ride", 0, 9);
    private Part snarePart = new Part("Drums snare", 0, 9);

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
                snarePart.empty();
                ridePart.empty();
                switch (Composer.NextSectionSoloCharacter)
                {
                    case 'A':
                        Composer.getSoloScore().setTempo(Musician.getTempo());
                        Composer.getSoloScore().addPart(composeSectionA(snarePart.getTitle()));
                        Composer.getSoloScore().addPart(composeSectionA(ridePart.getTitle()));
                        break;
                    case 'B':
                        Composer.getSoloScore().setTempo(Musician.getTempo());
                        Composer.getSoloScore().addPart(composeSectionB(snarePart.getTitle()));
                        Composer.getSoloScore().addPart(composeSectionB(ridePart.getTitle()));
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
                snarePart.empty();
                ridePart.empty();
                switch (Composer.NextSectionSoloCharacter)
                {
                    case 'A':
                        Composer.getSoloScore().setTempo(Musician.getTempo());
                        Composer.getSoloScore().addPart(composeSectionA(snarePart.getTitle()));
                        Composer.getSoloScore().addPart(composeSectionA(ridePart.getTitle()));
                        break;
                    case 'B':
                        Composer.getSoloScore().setTempo(Musician.getTempo());
                        Composer.getSoloScore().addPart(composeSectionB(snarePart.getTitle()));
                        Composer.getSoloScore().addPart(composeSectionB(ridePart.getTitle()));
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

    private Part composeSectionA(String partName)
    {
        if(partName.equals("Drums ride"))
        {
            int size = Musician.sectionAchords.size();
            int x;
            double[] pattern;
            double[] pattern0 = {C, 0.67, 0.33, C, C};
            double[] pattern1 = {0.67, 0.33, C, 0.33, 0.67,C};
            double[] pattern2 = {C, C, C, 0.67, 0.33};
            double[] pattern3 = {0.33, 0.67, 0.33, C, 0.67,0.67,0.33};
            for(int i = 0; i < size; i++)
            {
                // choose one of the patterns at random
                x = (int)(Math.random()*4);
                switch (x)
                {
                    case 0:
                        pattern = new double[pattern0.length];
                        pattern = pattern0;
                        break;
                    case 1:
                        pattern = new double[pattern1.length];
                        pattern = pattern1;
                        break;
                    case 2:
                        pattern = new double[pattern2.length];
                        pattern = pattern2;
                        break;
                    case 3:
                        pattern = new double[pattern3.length];
                        pattern = pattern3;
                        break;
                    default:
                        pattern = new double[pattern0.length];
                        pattern = pattern0;
                }
                ridePart.addPhrase(swingTime(pattern));

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
            int x;
            double[] pattern;
            double[] pattern0 = {C, 0.67, 0.33, C, C};
            double[] pattern1 = {0.67, 0.33, C, 0.33, 0.67,C};
            double[] pattern2 = {C, C, C, 0.67, 0.33};
            double[] pattern3 = {0.33, 0.67, 0.33, C, 0.67,0.67,0.33};
            for(int i = 0; i < size; i++)
            {
                // choose one of the patterns at random
                x = (int)(Math.random()*4);
                switch (x)
                {
                    case 0:
                        pattern = new double[pattern0.length];
                        pattern = pattern0;
                        break;
                    case 1:
                        pattern = new double[pattern1.length];
                        pattern = pattern1;
                        break;
                    case 2:
                        pattern = new double[pattern2.length];
                        pattern = pattern2;
                        break;
                    case 3:
                        pattern = new double[pattern3.length];
                        pattern = pattern3;
                        break;
                    default:
                        pattern = new double[pattern0.length];
                        pattern = pattern0;
                }
                ridePart.addPhrase(swingTime(pattern));


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

    private Phrase swingTime(double[] pattern) {
        // build the ride line
        Phrase phr = new Phrase();

        int ride = 51;
        for (int i = 0; i< pattern.length; i++)
        {
            Note note = new Note(ride,pattern[i],60);
            phr.addNote(note);
        }
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



}
