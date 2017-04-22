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
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;

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
                                snarePart.empty();
                                ridePart.empty();
                                switch (Composer.getNextsectionCharacter()) {
                                    case 'A':


                                        Composer.getAccompanimentScore().setTempo(Musician.getTempo());
                                        Composer.getAccompanimentScore().addPart(composeSectionA(snarePart.getTitle()));
                                        Composer.getAccompanimentScore().addPart(composeSectionA(ridePart.getTitle()));
                                        break;

                                    case 'B':

                                        Composer.getAccompanimentScore().setTempo(Musician.getTempo());
                                        Composer.getAccompanimentScore().addPart(composeSectionB(snarePart.getTitle()));
                                        Composer.getAccompanimentScore().addPart(composeSectionB(ridePart.getTitle()));
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
                        snarePart.empty();
                        ridePart.empty();
                        switch ( Composer.NextsectionCharacter)
                        {
                            case 'A':
                            {

                                Composer.getAccompanimentScore().setTempo(Musician.getTempo());
                                Composer.getAccompanimentScore().addPart(composeSectionA(snarePart.getTitle()));
                                Composer.getAccompanimentScore().addPart(composeSectionA(ridePart.getTitle()));
                                break;
                            }
                            case 'B':
                                Composer.getAccompanimentScore().setTempo(Musician.getTempo());
                                Composer.getAccompanimentScore().addPart(composeSectionB(snarePart.getTitle()));
                                Composer.getAccompanimentScore().addPart(composeSectionB(ridePart.getTitle()));
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
        phr.addNote(new Note(ride, C,80));
        phr.addNote(new Note(ride, 0.67,80));
        phr.addNote(new Note(ride, 0.33,80));
        phr.addNote(new Note(ride, C,80));
        phr.addNote(new Note(ride, C,80));
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










    public int onEnd()
    {
        firstTimeHere++;

        return transition;
    }


}
