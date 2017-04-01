package run;

import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;

import java.util.Random;

/**
 * Created by OscarAlfonso on 3/26/2017.
 */
public class MainScore extends Agent {

    public void setup()
    {
        FSMBehaviour finalFsm = new FSMBehaviour(this);

        finalFsm.registerFirstState(new First(this),"FirstState");
        finalFsm.registerState(new second(this),"SecondState");
        finalFsm.registerLastState(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("Last State");
            }
        },"LastState");

        finalFsm.registerTransition("FirstState","FirstState",0);
        finalFsm.registerTransition("FirstState","SecondState",1);
        finalFsm.registerTransition("SecondState","FirstState",3,new String[]{"FirstState",
                "SecondState"});
        finalFsm.registerTransition("SecondState","LastState",4);

        addBehaviour(finalFsm);

    }

    private class First extends OneShotBehaviour
    {
        private int transition = 0;
        private int state = 0;
        private int firstTime = 0;
        public First(Agent a)
        {
           super(a);
        }


        public void onStart()
        {
            System.out.println("First time here");
            hello h = new hello(myAgent);
            myAgent.addBehaviour(h);

        }

        public void action()
        {
            switch (state)
            {
                case 0:
                    if (firstTime < 1)
                    {
                        System.out.println("State 1");
                    }

                    break;
                case 2:
                    System.out.println("State 2");
                    transition = 1;
                    break;
            }
        }

        public int onEnd ()
        {
            firstTime++;
            return transition;
        }

        private class hello extends SimpleBehaviour
        {
            private boolean out = false;
            public hello(Agent a)
            {
              super(a);
            }

            public void onStart()
            {
                System.out.println("Hello");
            }

            public void action()
            {
                if (Math.random()*5 >2)
                {
                    out = true;
                    state = 2;
                }

            }

            public boolean done() {

                if (out)
                {
                    return true;
                }
                return false;
            }
        }
    }

    private class second extends OneShotBehaviour
    {
        public second(Agent a)
        {
            super(a);
        }
        int timeHere = 0;
        int transition = 3;

        public void onStart()
        {
            System.out.println("Second state");

        }

        public void action()
        {
            if(Math.random()*100>98)
            {
                transition = 4;
            }
        }

        public int onEnd()
        {
            timeHere++;
            return transition;
        }
    }
}
