package tools.ensemble.behaviours.ComposerBhaviours.solo;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.util.Play;
import jm.util.View;
import tools.ensemble.agents.Composer;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;

import java.util.Date;

/**
 * Created by OscarAlfonso on 3/26/2017.
 */
public class PlaySoloBehaviour extends OneShotBehaviour implements DataStoreComposer, JMC {

    private int transition = 10;
    private int firstTimeHere = 0;
    private Ontology timeHandlerOntology;
    private Codec Language;
    private long timeLeft;
    private Section section;
    private Date sectionTimeLeft;
    private Date sectionStartedAt;
    private long Now;
    private char theSection;
    private int theIndexSection;

    public PlaySoloBehaviour(Agent a, Ontology ont,Codec lang)
    {
        super(a);
        timeHandlerOntology = ont;
        Language = lang;
    }

    public void action()
    {
        if (firstTimeHere < 1)
        {




        }

        if (Composer.getHoldSoloPlayback() < 1)
        {
            if (getDataStore().containsKey(SECTION_INSTANCE_FOR_SYN_SOLO) && Composer.getTimeLeftInCurrentsection() == null)
            {
                section = (Section) getDataStore().get(SECTION_INSTANCE_FOR_SYN_SOLO);
                sectionTimeLeft = section.getTimeLeft();
                sectionStartedAt = section.getSectionStartedAt();
                Now = System.currentTimeMillis();
                long timeElapsed = Now - sectionStartedAt.getTime();
                timeLeft = sectionTimeLeft.getTime() - timeElapsed;
                if (timeLeft < 0)
                {
                    //We didn't have time to play, then go to request the section again.
                    transition = 13;
                }
                else
                {
                    transition = 12;
                }
            }else
            {
                timeLeft = Composer.getTimeLeftInCurrentsection();
                if (timeLeft < 0)
                {
                    transition = 13;
                }
                else
                {
                    transition = 12;
                }
            }
            theSection = Composer.getNextSectionSoloCharacter();
            theIndexSection = Composer.getNextSectionSoloIndex();

            if (timeLeft > 0)
            {
                myAgent.doWait(timeLeft);
            }

            //Play the solo

            play();
            Composer.setHoldSoloPlayback(1);

            //Go to compose another section of solo
            //transition = 12;
        }
    }

    public int onEnd()
    {
        firstTimeHere++;

        return transition;
    }

    private void play()
    {

        System.out.println("Im in playing");
        Play.midi(Composer.getSoloPianoScore(),false,false,6,0);
        View.print(Composer.getSoloPianoScore());


        sectionStartedAt = new Date();
        long timeStartedAt = sectionStartedAt.getTime();

        //Calculate the lenght of the solosection
        double betPerMeasure = Composer.getSoloPianoScore().getNumerator();
        double numberOfMeasure = Composer.getSoloPianoScore().getEndTime()/betPerMeasure;
        double tempo = Composer.getSoloPianoScore().getTempo();
        double lengthOfSection = (betPerMeasure*numberOfMeasure/tempo)*60*1000;
        long currentTimes = System.currentTimeMillis();
        System.out.println("current Time "+currentTimes);
        long transcurrentTime =  currentTimes - timeStartedAt;
        System.out.println("transcurrent time :"+transcurrentTime);

        long timeLeft = (long) (lengthOfSection - transcurrentTime);
        System.out.println("time left: "+timeLeft);
        System.out.println("the tempo "+ Musician.getTempo());
        System.out.println("playing section "+theSection);
        System.out.println("playing index "+theIndexSection);
        //Update the time left.
        Composer.setTimeLeftInCurrentsection(timeLeft);
        //Set the rule to 0 so it can compose the next part of the solo.
        Composer.setHodSoloComposition(0);
        //UpdateTheSynWithSectionInfo(theSection,timeLeft,theIndexSection);
    }
    private void UpdateTheSynWithSectionInfo(char sec, Long time, int Index)
    {
        char currentSection = sec;
        Long timeLeft = time;
        int sIndex = Index;
        ACLMessage messageForSyn;
        Section section = new Section();
        String theSection = String.valueOf(currentSection);
        section.setAccompanimentCurrentSection(theSection);
        Date theTimeLeft = new Date(timeLeft);
        section.setTimeLeft(theTimeLeft);
        section.setSectionIndex(sIndex);
        section.setSectionStartedAt(sectionStartedAt);
        messageForSyn = new ACLMessage(ACLMessage.INFORM);
        messageForSyn.setConversationId("update-syn-what-section-is-played");
        messageForSyn.setOntology(timeHandlerOntology.getName());
        messageForSyn.setLanguage(Language.getName());
        if (getDataStore().containsKey(COMPOSER_MY_INTERNAL_SYNCHRONIZER))
        {
            AID receiver = (AID) getDataStore().get(COMPOSER_MY_INTERNAL_SYNCHRONIZER);
            try {
                myAgent.getContentManager().fillContent(messageForSyn, new Action(receiver, section));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            messageForSyn.addReceiver(receiver);
        }else
        {
            AID receiver = myAgent.getAID();
            //Find the internal TimeManager Agent
            DFAgentDescription template_2 = new DFAgentDescription();
            ServiceDescription sd_2 = new ServiceDescription();
            sd_2.setType("InternalTimeManager");
            try {
                sd_2.setOwnership(myAgent.getContainerController().getContainerName());
            } catch (ControllerException e) {
                e.printStackTrace();
            }
            template_2.addServices(sd_2);
            //Now get the internal time manager
            DFAgentDescription[] resultSearchTimeManager = new DFAgentDescription[0];
            try {
                resultSearchTimeManager = DFService.search(myAgent,template_2);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
            for (int i=0; i<resultSearchTimeManager.length; i++)
            {
                receiver = resultSearchTimeManager[i].getName();
            }
            try {
                myAgent.getContentManager().fillContent(messageForSyn, new Action(receiver, section));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            messageForSyn.addReceiver(receiver);


        }

        myAgent.send(messageForSyn);
    }
}
