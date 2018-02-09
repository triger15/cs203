/**
  * Event encapsulations information about the time an event is supposed to occur, and its type.
  */
public class Event {
  public double time; // The time this event will occur
  public int eventType; // The type of event, indicates what should happen when an event occurs.

  /**
   * Create an event and initialize it.
   */
  public Event(double when, int type) {
    this.time = when;
    this.eventType = type;
  }

}