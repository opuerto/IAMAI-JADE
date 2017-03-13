package tools.ensemble.behaviours.ComposerBhaviours.accompaniment;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jm.music.data.Score;
import jm.util.Play;
import jm.util.View;
import tools.ensemble.interfaces.DataStoreComposer;

/**
 * Created by OscarAlfonso on 3/7/2017.
 */
public class PlayAccompanimentBehaviour extends OneShotBehaviour implements DataStoreComposer {

    private int transition = 5;
    private int firstTimeHere = 0;
    private Agent agent;

    public PlayAccompanimentBehaviour(Agent a)
    {
       this.agent = a;
    }

    public void action()
    {
        if(getDataStore().containsKey(HOLD_PLAYBACK))
        {
            int holdPlay = (Integer) getDataStore().get(HOLD_PLAYBACK);
            if(holdPlay < 1)
            {
                System.out.println("play after head");
                PlayScore play = new PlayScore((Long) getDataStore().get(INTRO_TIME_LEFT), (Score) getDataStore().get(ACCOMPANIMENT_SCORE));
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
                System.out.println("this is the "+getBehaviourName() +"play!!");
                if(getDataStore().containsKey(INTRO_TIME_LEFT))
                {
                    System.out.println("play after intro");
                    PlayScore play = new PlayScore((Long) getDataStore().get(INTRO_TIME_LEFT), (Score) getDataStore().get(ACCOMPANIMENT_SCORE));
                    play.setDataStore(getDataStore());
                    agent.addBehaviour(play);


                }

            }



        }


        /*if(firstTimeHere < 1)
        {
            System.out.println("this is the "+getBehaviourName() +"play!!");
            if(getDataStore().containsKey(INTRO_TIME_LEFT))
            {
                System.out.println("play introoo");
                PlayScore play = new PlayScore((Long) getDataStore().get(INTRO_TIME_LEFT), (Score) getDataStore().get(ACCOMPANIMENT_SCORE));
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
        public PlayScore(long timeLeft, Score score)
        {
            super(agent,timeLeft);
            this.accompanimentScore = score;

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
            double lengofSection = (betPerMeasure*numberOfMeasure/tempo)*60*1000;
            long currentTimes = System.currentTimeMillis();
            System.out.println("current Time "+currentTimes);
            long transcurrentTime =  currentTimes - timeStarted;
            System.out.println("transcurrent time :"+transcurrentTime);

            long timeLeft = (long) (lengofSection - transcurrentTime);
            System.out.println("time left: "+timeLeft);
            getDataStore().remove(INTRO_TIME_LEFT);
            getDataStore().put(INTRO_TIME_LEFT,timeLeft);
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

            transition = 6;


        }

    }
}
