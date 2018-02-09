/**
  * Simulator encapsulates all the information relevant to the current simulation.
  */
public class Simulator {
  private int maxEvents;
  public double serviceTime;

  // Simulator needs a slave
  private Server slave1;

  /**
  * We support two types of events for now, when a customer arrives, and when a customer leaves.
  */
  public static final int CUSTOMER_ARRIVE = 1;
  public static final int CUSTOMER_DONE = 2;

  // The next two members are used to store scheduled events
  private Event[] events; // Array of events, order of events not guaranteed.
  private int numOfEvents; // The number of events in the event array.

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

  // Constructor class for Simulator
  public Simulator(int max_events, double service_time) {
    this.maxEvents = max_events;
    this.serviceTime = service_time;
    this.events = new Event[maxEvents];
    this.numOfEvents = 0;
    this.customerWaiting = false;
    this.customerBeingServed = false;
    this.lastCustomerId = 0;
    this.servedCustomerId = -1;
    this.waitingCustomerId = -1;

    // Create slave object
    slave1 = new Server(0);
  }

  /**
   * Schedule the event with the simulator.  The simulator maintains
   * an array of event (in arbitrary order) and this method simply
   * appends the given event to the end of the array.
   *
   * @return true if the event is added successfully; false otherwise.
   */
  public boolean scheduleEventInSimulator(Event e) {
    if (this.numOfEvents >= this.maxEvents) {
      return false;
    } else {
      // append e as the last element in array sim.events.
      this.events[this.numOfEvents] = e;
      this.numOfEvents++;
      return true;
    }
  }

  /**
   * Run the simulator until there is no more events scheduled.
   */
  public void runSimulator() {
    while (this.numOfEvents > 0) {
      Event e = getNextEarliestEvent();
      simulateEvent(e);
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
  private Event getNextEarliestEvent() {
    int nextEventIndex = -1;

    // Scan linearly through the array to find the event
    // with earliest (smallest) timestamp.
    double minTime = Double.MAX_VALUE;
    for (int i = 0; i < this.numOfEvents; i++) {
      if (this.events[i].time < minTime) {
        minTime = this.events[i].time;
        nextEventIndex = i;
      }
    }

    // Get the earliest event
    Event e = this.events[nextEventIndex];

    // Replace the earliest event with the last element in
    // the array.
    this.events[nextEventIndex] = this.events[this.numOfEvents - 1];
    this.numOfEvents--;
    return e;
  }

  /**
  * Simulate the event based on event type.
  */
  private void simulateEvent(Event e) {
    switch (e.eventType) {
    case CUSTOMER_ARRIVE:
      // A customer has arrived.  Increase the ID and assign it to this customer.
      this.lastCustomerId++;
      System.out.printf("%6.3f %d arrives\n", e.time, this.lastCustomerId);

      // If there is no customer currently being served.  Serve this one.
      int currentCustomer = this.lastCustomerId;
      if (!this.customerBeingServed) {
        slave1.serveCustomer(this, e.time, currentCustomer);
      } else if (!this.customerWaiting) {
        // If there is a customer currently being served, and noone is waiting, wait.
        slave1.makeCustomerWait(this, e.time, currentCustomer);
      } else {
        // If there is a customer currently being served, and someone is waiting, the
        // customer just leaves and go elsewhere (maximum only one waiting customer).
        slave1.customerLeaves(this, e.time, currentCustomer);
      }
      break;
    case CUSTOMER_DONE:
      // A customer is done being served.
      System.out.printf("%6.3f %d done\n", e.time, this.servedCustomerId);
      if (this.customerWaiting) {
        // Someone is waiting, serve this waiting someone.
        slave1.serveWaitingCustomer(this, e.time);
      } else {
        // Server idle
        this.customerBeingServed = false;
      }
      break;
    default:
      System.err.printf("Unknown event type %d\n", e.eventType);
    }
  }

}