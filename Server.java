/**
  * Server encapsulations information about the methods performed by a server on a customer.
  */
public class Server {
    public int serverID;

    /**
     * Create a server and initialize it.
     */
    public Server(int _id) {
        this.serverID = _id;

    }

    /**
    * Serve the current customer with given id at given time in the given simulator.
    * Precondition: noone must be served at this time.
    */
    public void serveCustomer(Simulator sim, double time, int id) {
        assert sim.customerBeingServed == false;
        sim.customerBeingServed = true;
        sim.servedCustomerId = id;
        System.out.printf("%6.3f %d served\n", time, id);
        boolean ok = sim.scheduleEventInSimulator(new Event(time + sim.serviceTime, Simulator.CUSTOMER_DONE));
        if (!ok) {
            System.err.println("Warning: too many events.  Simulation result will not be correct.");
        }
        sim.totalNumOfServedCustomer++;
        assert sim.customerBeingServed == true;
    }

    /**
     * Make the current customer with given id wait starting at given time in the given simulator.
     * Precondition: someone is being served but noone is waiting
     * Postcondition: someone is being served, and someone is waiting
     */
    public void makeCustomerWait(Simulator sim, double time, int id) {
        assert sim.customerBeingServed == true;
        assert sim.customerWaiting == false;
        sim.waitingCustomerId = id;
        System.out.printf("%6.3f %d waits\n", time, id);
        sim.customerWaiting = true;
        sim.timeStartedWaiting = time;
        assert sim.customerBeingServed == true;
        assert sim.customerWaiting == true;
    }

    /**
     * Make the current customer with given id wait, starting at given time in the given simulator.
     * Precondition: someone must be waiting, and noone is being served.
     * Postcondition: noone is waiting, and someone is being served.
     */
    public void serveWaitingCustomer(Simulator sim, double time) {
        assert sim.customerBeingServed == false;
        assert sim.customerWaiting == true;
        sim.customerWaiting = false;
        serveCustomer(sim, time, sim.waitingCustomerId);
        sim.totalWaitingTime += (time - sim.timeStartedWaiting);
        assert sim.customerBeingServed == true;
        assert sim.customerWaiting == false;
    }

    /**
     * Make the current customer with given id leave, at given time in the given simulator.
     * Precondition: someone must be waiting, and someone is being served.
     * Postcondition: someone must be waiting, and someone is being served.
     */
    public void customerLeaves(Simulator sim, double time, int id) {
        assert sim.customerBeingServed == true;
        assert sim.customerWaiting == true;
        System.out.printf("%6.3f %d leaves\n", time, id);
        sim.totalNumOfLostCustomer++;
        assert sim.customerBeingServed == true;
        assert sim.customerWaiting == true;
    }

}