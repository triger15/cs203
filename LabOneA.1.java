import java.util.Scanner;
import java.io.FileReader;
import java.io.FileNotFoundException;

class LabOneA {

  /**
   * Event encapsulations information about the time an event is supposed to occur, and its type.
   */
  static class Event {
    public double time; // The time this event will occur
    public int eventType; // The type of event, indicates what should happen when an event occurs.
  }

  /**
   * We support two types of events for now, when a customer arrives, and when a customer leaves.
   */
  public static final int CUSTOMER_ARRIVE = 1;
  public static final int CUSTOMER_DONE = 2;


  /**
   * Simulator encapsulates all the information relevant to the current simulation.
   */
  static class Simulator {

    // The first two members are constants, used to configure the simulator.
    public static final int MAX_NUMBER_OF_EVENTS = 100; // Maximum number of events
    public static final double SERVICE_TIME = 1.0; // Time spent serving a customer

    // The next two members are used to store scheduled events
    public Event[] events; // Array of events, order of events not guaranteed.
    public int numOfEvents; // The number of events in the event array.

    // The next three members are used to record the states of the simulation
    public boolean customerBeingServed; // is a customer currently being served?
    public boolean customerWaiting; // is a customer currently waiting?
    public double timeStartedWaiting; // the time the current waiting customer started waiting

    // The next three members are used to keep track of simulation statistics
    public double totalWaitingTime; // total time everyone spent waiting
    public int totalNumOfServedCustomer; // how many customer has waited
    public int totalNumOfLostCustomer; // how many customer has been lost

    // The next three members are used to identify customer
    public int lastCustomerId; // starts from 0 and increases as customer arrives.
    public int servedCustomerId; // id of the customer being served, if any
    public int waitingCustomerId; // id of the customer currently waiting, if any
  }

  /**
   * The main method for LabOneA.
   * Reads arrival time from either stdin or a file and insert the arrival event into an array
   * in the simulator.  Then, run the simulator.
   */
  public static void main(String[] args) {
    Simulator sim = createSimulator();
    Scanner s = createScanner(args);
    if (s == null) {
      return;
    }

    // The input file consists of a sequence of arrival timestamp
    // (not necessary in order).
    while (s.hasNextDouble()) {
      Event e = createEvent(s.nextDouble(), CUSTOMER_ARRIVE);
      boolean ok = scheduleEventInSimulator(e, sim);
      if (!ok) {
        System.err.printf("warning: too many events.  Skipping the rest.");
        s.close();
        break;
      }
    }
    s.close();

    // Then run the simulator
    runSimulator(sim);

    // Print stats as three numbers:
    // <avg waiting time> <number of served customer> <number of lost customer>
    System.out.printf("%.3f %d %d\n", sim.totalWaitingTime / sim.totalNumOfServedCustomer,
        sim.totalNumOfServedCustomer, sim.totalNumOfLostCustomer);
  }

  /**
   * Create and return a scanner.  If a command line arguement is given,
   * treat the argument as a file, and open a scanner on the file.  Else,
   * open a scanner that reads from standard input.
   *
   * @return a scanner or `null` if a filename is given but cannot be open.
   */
  static Scanner createScanner(String[] args) {
    Scanner s = null;
    try {
      // Read from stdin if no filename is given, otherwise
      // read from the given file.
      if (args.length == 0) {
        s = new Scanner(System.in);
      } else {
        FileReader f = new FileReader(args[0]);
        s = new Scanner(f);
      }
    } catch (FileNotFoundException ex) {
      System.err.println("Unable to open file " + args[0] + " " + ex + "\n");
    } finally {
      return s;
    }
  }

  /**
   * Create a simulator, initialize the value and return it.
   *
   * @return A newly created simulator.
   */
  static Simulator createSimulator() {
    Simulator sim = new Simulator();
    sim.events = new Event[sim.MAX_NUMBER_OF_EVENTS];
    sim.numOfEvents = 0;
    sim.customerWaiting = false;
    sim.customerBeingServed = false;
    sim.lastCustomerId = 0;
    sim.servedCustomerId = -1;
    sim.waitingCustomerId = -1;
    return sim;
  }

  /**
   * Create an event and initialize it.
   *
   * @return A new event of type `type` that happens `when`
   */
  static Event createEvent(double when, int type) {
    Event e = new Event();
    e.time = when;
    e.eventType = type;
    return e;
  }

  /**
   * Schedule the event with the simulator.  The simulator maintains
   * an array of event (in arbitrary order) and this method simply
   * appends the given event to the end of the array.
   *
   * @return true if the event is added successfully; false otherwise.
   */
  static boolean scheduleEventInSimulator(Event e, Simulator sim) {
    if (sim.numOfEvents >= sim.MAX_NUMBER_OF_EVENTS) {
      return false;
    } else {
      // append e as the last element in array sim.events.
      sim.events[sim.numOfEvents] = e;
      sim.numOfEvents++;
      return true;
    }
  }

  /**
   * Run the simulator until there is no more events scheduled.
   */
  static void runSimulator(Simulator sim) {
    while (sim.numOfEvents > 0) {
      Event e = getNextEarliestEvent(sim);
      simulateEvent(sim, e);
    }
  }

  /**
   * Find the next event with the earliest timestamp, breaking
   * ties arbitrarily.  The event is then deleted from the array.
   * This is an O(n) algorithm.  Better algorithm exists.  To be
   * improved in later labs using a min heap.
   *
   * @return the next event
   */
  static Event getNextEarliestEvent(Simulator sim) {
    int nextEventIndex = -1;

    // Scan linearly through the array to find the event
    // with earliest (smallest) timestamp.
    double minTime = Double.MAX_VALUE;
    for (int i = 0; i < sim.numOfEvents; i++) {
      if (sim.events[i].time < minTime) {
        minTime = sim.events[i].time;
        nextEventIndex = i;
      }
    }

    // Get the earliest event
    Event e = sim.events[nextEventIndex];

    // Replace the earliest event with the last element in
    // the array.
    sim.events[nextEventIndex] = sim.events[sim.numOfEvents - 1];
    sim.numOfEvents--;
    return e;
  }

  /**
   * Simulate the event based on event type.
   */
  static void simulateEvent(Simulator sim, Event e) {
    switch (e.eventType) {
      case CUSTOMER_ARRIVE:
        // A customer has arrived.  Increase the ID and assign it to this customer.
        sim.lastCustomerId++;
        System.out.printf("%6.3f %d arrives\n", e.time, sim.lastCustomerId);

        // If there is no customer currently being served.  Serve this one.
        int currentCustomer = sim.lastCustomerId;
        if (!sim.customerBeingServed) {
          serveCustomer(sim, e.time, currentCustomer);
        } else if (!sim.customerWaiting) {
          // If there is a customer currently being served, and noone is waiting, wait.
          makeCustomerWait(sim, e.time, currentCustomer);
        } else {
          // If there is a customer currently being served, and someone is waiting, the
          // customer just leaves and go elsewhere (maximum only one waiting customer).
          customerLeaves(sim, e.time, currentCustomer);
        }
        break;
      case CUSTOMER_DONE:
        // A customer is done being served.
        System.out.printf("%6.3f %d done\n", e.time, sim.servedCustomerId);
        if (sim.customerWaiting) {
          // Someone is waiting, serve this waiting someone.
          serveWaitingCustomer(sim, e.time);
        } else {
          // Server idle
          sim.customerBeingServed = false;
        }
        break;
      default:
        System.err.printf("Unknown event type %d\n", e.eventType);
    }
  }

  /**
   * Serve the current customer with given id at given time in the given simulator.
   * Precondition: noone must be served at this time.
   */
  static void serveCustomer(Simulator sim, double time, int id) {
    assert sim.customerBeingServed == false;
    sim.customerBeingServed = true;
    sim.servedCustomerId = id;
    System.out.printf("%6.3f %d served\n", time, id);
    boolean ok = scheduleEventInSimulator(createEvent(time + sim.SERVICE_TIME, CUSTOMER_DONE), sim);
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
  static void makeCustomerWait(Simulator sim, double time, int id) {
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
  static void serveWaitingCustomer(Simulator sim, double time) {
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
  static void customerLeaves(Simulator sim, double time, int id) {
    assert sim.customerBeingServed == true;
    assert sim.customerWaiting == true;
    System.out.printf("%6.3f %d leaves\n", time, id);
    sim.totalNumOfLostCustomer++;
    assert sim.customerBeingServed == true;
    assert sim.customerWaiting == true;
  }
}
