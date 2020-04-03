package social_dist;
import com.sun.xml.internal.xsom.impl.scd.Iterators;
import sim.util.Double2D;
import sun.jvm.hotspot.runtime.DeadlockDetector;

import java.net.HttpURLConnection;
import java.util.*;

public class Test {

    public static void addAgents() {
        for (int x = 0; x < 10; x++) {
            Double2D loc = null;
            Human agent = null;
            loc = new Double2D(Math.random(), Math.random());
            agent = new Human("Human-" + x, loc);

            Random r = new Random();
            int i = r.nextInt(3);
            if(i == x) continue;
            System.out.println("Adding "+ agent.id + "  as contact to  agent "+i);
            Env.contacts.put("Human-"+i, agent);
        }
    }


    public static Collection<Human> fetch_contacts(String hu_id){
        Collection<Human> contacts = Env.contacts.get(hu_id);
        return contacts;

    }

    public static void main(String[] args) {
        addAgents();
        int i = 2;
        Collection<Human> contacts = fetch_contacts("Human-"+i);
        List<Human> mylist = new ArrayList<Human>();
        mylist.addAll(contacts);
        Collections.reverse(mylist);

        for(Human s : mylist.subList(mylist.size()-2, mylist.size()-1)) {
            System.out.println(s.id);
        }


    }

}
