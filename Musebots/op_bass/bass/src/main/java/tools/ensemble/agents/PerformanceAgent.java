package tools.ensemble.agents;

import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Created by OscarAlfonso on 3/24/2017.
 */
public class PerformanceAgent extends Agent {
    protected void setup()
    {
        FSMBehaviour behaviour = new FSMBehaviour(this);
        behaviour.registerFirstState(new Hello(),"hello");
        behaviour.registerLastState(new Hello2(),"hello2");
        behaviour.registerTransition("hello","hello2",0);
        behaviour.registerTransition("hello","hello",1);
        addBehaviour(behaviour);
    }

    private class Hello extends OneShotBehaviour
    {
        int x = 0;
        public void action()
        {
            if(x < 1)
            {
                System.out.println("una vez");
                FSMBehaviour subb = new FSMBehaviour(myAgent);
                subb.registerFirstState(new c1(),"1");
                subb.registerLastState(new c2(),"2");
                subb.registerTransition("1","1",0);
                subb.registerTransition("1","2",1);
                addBehaviour(subb);


            }

        }

        private class c1 extends OneShotBehaviour
        {
            public void action()
            {
                doSuspend();
            }
            public int onEnd()
            {
                return 0;
            }
        }
        private class c2 extends OneShotBehaviour
        {
            public void action()
            {
                System.out.println("adios");
            }

        }

        public int onEnd()
        {
            x++;
            return 0;
        }
    }
    private class Hello2 extends OneShotBehaviour
    {
        public void action()
        {
            System.out.println("by bye ");
        }

        public int onEnd()
        {
            return 2;
        }
    }
}
