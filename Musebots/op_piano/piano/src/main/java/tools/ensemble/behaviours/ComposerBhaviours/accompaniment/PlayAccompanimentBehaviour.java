package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import jm.music.data.Score;
import jm.util.Play;
import tools.ensemble.agents.Composer;
import tools.ensemble.agents.Musician;
import tools.ensemble.interfaces.DataStoreComposer;
import tools.ensemble.interfaces.DataStoreTimeManager;
import tools.ensemble.ontologies.composer.vocabulary.concepts.AccompanimentConcepts;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;

import java.util.Date;

/**
 * Created by OscarAlfonso on 3/7/2017.
 */
public class PlayAccompanimentBehaviour extends OneShotBehaviour implements DataStoreComposer {

    private int transition = 5;
    private int firstTimeHere = 0;
    private Agent agent;
    private Ontology timeHandlerOntology;
    private Codec Language;
    private  Score accompanimentScore;
    private char theSection;
    private int theIndexSection;
    private Date sectionStartedAt;

    public PlayAccompanimentBehaviour(Agent a, Ontology timehandlerOntology, Codec langugage)
    {
       this.agent = a;
        this.timeHandlerOntology = timehandlerOntology;
        this.Language = langugage;
    }

    public void action()
    {
        if(Composer.holdPlay < 1)
        {
            //int holdPlay = (Integer) getDataStore().get(HOLD_PLAYBACK);
            int holdPlay = Composer.holdPlay ;
            if(holdPlay < 1)
            {
                //theSection = (Character) getDataStore().get(NEXT_SECTION_TO_PLAY);
                //theIndexSection = (Integer) getDataStore().get(NEXT_SECTION_INDEX);
                theSection = Composer.NextsectionCharacter;
                theIndexSection = Composer.NextsectionIndex;
                System.out.println("play after head");
                System.out.println("next section "+theSection);
                //PlayScore play = new PlayScore((Long) getDataStore().get(PLAY_TIME_LEFT), (Score) getDataStore().get(ACCOMPANIMENT_SCORE),section,sIndex);
                //play.setDataStore(getDataStore());
                //agent.addBehaviour(play);
                /*

                Send the agent to sleep until the current section played end.
                */

               //myAgent.doWait((Long) getDataStore().get(PLAY_TIME_LEFT));
                myAgent.doWait(Composer.sectionPlayLeft);
                //Call the function that play and calculate the lenght of the section
               if(Musician.getLeader())
               {
                   transition = 11;
               } else
               {
                   transition = 6;
               }
               play();
                //holdPlay = 1;
                Composer.holdPlay = 1;
                //getDataStore().remove(HOLD_PLAYBACK);
                //getDataStore().put(HOLD_PLAYBACK,holdPlay);

            }
        }
       /* else
        {
            if(firstTimeHere < 0)
            {
                Character section = (Character) getDataStore().get(NEXT_SECTION_TO_PLAY);
                Integer sIndex = (Integer) getDataStore().get(NEXT_SECTION_INDEX);
                System.out.println("this is the "+getBehaviourName() +"play!!");
                if(getDataStore().containsKey(PLAY_TIME_LEFT))
                {
                    System.out.println("play after intro");

                }

            }



        }*/




    }

    private void play()
    {

        //accompanimentScore = (Score) getDataStore().get(ACCOMPANIMENT_SCORE);
        accompanimentScore = Composer.accompanimentScore;

        if (!Musician.getLeader())
        Play.midi(Composer.accompanimentScore,false,false,2,0);
        System.out.println("TIMESTAMP START PLAYING NOW = "+System.currentTimeMillis());
            Composer.measureCounter++;



        //View.print(accompanimentScore);
        sectionStartedAt = new Date();
        long timeStarted = sectionStartedAt.getTime();

        //Calculate the lenght of the intro.
        double betPerMeasure = accompanimentScore.getNumerator();
        double numberOfMeasure = accompanimentScore.getEndTime()/betPerMeasure;
        double tempo = accompanimentScore.getTempo();
        double lengthOfSection = (betPerMeasure*numberOfMeasure/tempo)*60*1000;
        long currentTimes = System.currentTimeMillis();
        System.out.println("current Time "+currentTimes);
        long transcurrentTime =  currentTimes - timeStarted;
        System.out.println("transcurrent time :"+transcurrentTime);

        long timeLeft = (long) (lengthOfSection - transcurrentTime);
        System.out.println("time left: "+timeLeft);
        System.out.println("the tempo "+ Musician.tempo);
        //getDataStore().remove(PLAY_TIME_LEFT);
        //getDataStore().put(PLAY_TIME_LEFT,timeLeft);
        Composer.sectionPlayLeft = timeLeft;
       /* if(getDataStore().containsKey(FROM_PLAY_TO_COMPOSE))
        {
            getDataStore().remove(FROM_PLAY_TO_COMPOSE);
            getDataStore().put(FROM_PLAY_TO_COMPOSE,true);
        }else
        {
            getDataStore().put(FROM_PLAY_TO_COMPOSE,true);
        }*/

       /* if(getDataStore().containsKey(HOLD_COMPOSITION))
        {
            getDataStore().remove(HOLD_COMPOSITION);
            int holdComposition = 0;
            getDataStore().put(HOLD_COMPOSITION,holdComposition);
        }
        else
        {
            int holdComposition = 0;
            getDataStore().put(HOLD_COMPOSITION,holdComposition);
        }*/
        Composer.holdComposition = 0;
        //send the message to the synchronizer
        UpdateTheSynWithSectionInfo(theSection,timeLeft,theIndexSection);
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
                sd_2.setOwnership(agent.getContainerController().getContainerName());
            } catch (ControllerException e) {
                e.printStackTrace();
            }
            template_2.addServices(sd_2);
            //Now get the internal time manager
            DFAgentDescription[] resultSearchTimeManager = new DFAgentDescription[0];
            try {
                resultSearchTimeManager = DFService.search(agent,template_2);
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

    public int onEnd()
    {

        firstTimeHere++;

        return transition;
    }






}
