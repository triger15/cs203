/**
  * Simulator encapsulates all the information relevant to the current simulation.
  */
public class Simulator {
  private int maxEvents;
  private double serviceTime;

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
    
  }

}