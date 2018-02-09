/**
  * Customer encapsulates information about each customer and its methods.
  */
public class Customer {
  private int customerID;

  /**
   * Constructor class for customer object with customerID
   */
  public Customer(int _custID) {
    this.customerID = _custID;
  }

  // getter function for customer id
  public int getCustID() {
    return this.customerID;
  }
  
}