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

    public PlayAccompanimentBehaviour(Agent a, Ontology timehandlerOntology, Codec langugage)
    {
       this.agent = a;
        this.timeHandlerOntology = timehandlerOntology;
        this.Language = langugage;
    }

    public void action()
    {
        if(getDataStore().containsKey(HOLD_PLAYBACK))
        {
            int holdPlay = (Integer) getDataStore().get(HOLD_PLAYBACK);
            if(holdPlay < 1)
            {
                Character section = (Character) getDataStore().get(NEXT_SECTION_TO_PLAY);
                Integer sIndex = (Integer) getDataStore().get(NEXT_SECTION_INDEX);
                System.out.println("play after head");
                System.out.println("next section "+section);
                PlayScore play = new PlayScore((Long) getDataStore().get(PLAY_TIME_LEFT), (Score) getDataStore().get(ACCOMPANIMENT_SCORE),section,sIndex);
                play.setDataStore(getDataStore());
                agent.addBehaviour(play);
                holdPlay = 1;
                getDataStore().remove(HOLD_PLAYBACK);
                getDataStore().put(HOLD_PLAYBACK,holdPlay);
            }
        }
        else
        {
            if(firstTimeHere < 0)
            {
                Character section = (Character) getDataStore().get(NEXT_SECTION_TO_PLAY);
                Integer sIndex = (Integer) getDataStore().get(NEXT_SECTION_INDEX);
                System.out.println("this is the "+getBehaviourName() +"play!!");
                if(getDataStore().containsKey(PLAY_TIME_LEFT))
                {
                    System.out.println("play after intro");
                    PlayScore play = new PlayScore((Long) getDataStore().get(PLAY_TIME_LEFT), (Score) getDataStore().get(ACCOMPANIMENT_SCORE), section,sIndex);
                    play.setDataStore(getDataStore());
                    agent.addBehaviour(play);


                }

            }



        }


        /*if(firstTimeHere < 1)
        {
            System.out.println("this is the "+getBehaviourName() +"play!!");
            if(getDataStore().containsKey(PLAY_TIME_LEFT))
            {
                System.out.println("play introoo");
                PlayScore play = new PlayScore((Long) getDataStore().get(PLAY_TIME_LEFT), (Score) getDataStore().get(ACCOMPANIMENT_SCORE));
                play.setDataStore(getDataStore());
                agent.addBehaviour(play);


            }
            if(!getDataStore().containsKey(FROM_PLAY_TO_COMPOSE))
            {
                getDataStore().put(FROM_PLAY_TO_COMPOSE,true);
                System.out.println("tran");
            }

        }*/
        //transition = 6;

    }

    public int onEnd()
    {

        firstTimeHere++;

        return transition;
    }

    private class PlayScore extends WakerBehaviour
    {
        private Score accompanimentScore;
        private char section;
        private int sIndex;
        public PlayScore(long timeLeft, Score score,char section, int sIndex)
        {
            super(agent,timeLeft);
            this.accompanimentScore = score;
            this.section = section;
            this.sIndex = sIndex;

        }

        protected void onWake()
        {
            Play.midi(accompanimentScore,false,false,3,0);
            //View.print(accompanimentScore);
            long timeStarted = System.currentTimeMillis();
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
            getDataStore().remove(PLAY_TIME_LEFT);
            getDataStore().put(PLAY_TIME_LEFT,timeLeft);
            if(getDataStore().containsKey(FROM_PLAY_TO_COMPOSE))
            {
               getDataStore().remove(FROM_PLAY_TO_COMPOSE);
                getDataStore().put(FROM_PLAY_TO_COMPOSE,true);
            }else
            {
                getDataStore().put(FROM_PLAY_TO_COMPOSE,true);
            }

            if(getDataStore().containsKey(HOLD_COMPOSITION))
            {
                getDataStore().remove(HOLD_COMPOSITION);
                int holdComposition = 0;
                getDataStore().put(HOLD_COMPOSITION,holdComposition);
            }
            else
            {
                int holdComposition = 0;
                getDataStore().put(HOLD_COMPOSITION,holdComposition);
            }

            UpdateSynchronizerConversation sendInforSyn = new UpdateSynchronizerConversation(myAgent,section,timeLeft,sIndex);
            sendInforSyn.setDataStore(getDataStore());
            myAgent.addBehaviour(sendInforSyn);

            transition = 6;
            block();


        }






    }

    private class UpdateSynchronizerConversation extends OneShotBehaviour
    {
        private char currentSection;
        private Long timeLeft;
        private Section section;
        private int sIndex;
        private ACLMessage messageForSyn;
        public UpdateSynchronizerConversation(Agent a, char section, Long timeLeft, int sIndex)
        {
            super(a);
            this.currentSection = section;
            this.timeLeft = timeLeft;
            this.sIndex = sIndex;
        }

        public void action()
        {

            section = new Section();
            String theSection = String.valueOf(currentSection);
            section.setAccompanimentCurrentSection(theSection);
            Date theTimeLeft = new Date(timeLeft);
            section.setTimeLeft(theTimeLeft);
            section.setSectionIndex(sIndex);
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
            block();

        }
    }


}
