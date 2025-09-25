# Jerry's Quick Mart - Exercise for Deft

This is my solution to your challenge. I decided to use Java since OOP was the main concern about this project. 

### Assumptions

  * The app assumes `inventory.txt` is formatted as expected and shown in the instructions set.
  * The sales tax rate is fixed 6.5% as shown in the instructions set.

### Solution

  * `Product` holds an item's details while `CartItem` represents a specific item in a customer's cart, tracking quantity.
  * `Inventory` it's responsible for reading and writing data to the `inventory.txt` file.
  * `Cart` manages customer items for a transaction. It handles calculations, including subtotals, tax, and total. It also provides methods for adjusting item quantities.
  * `ReceiptPrinter` is dedicated to generating the receipt file. It formats the transaction data and writes it to a `.txt` file. It also ensures receipts are numbered sequentially across numerous transactions.
  * `Main` manages the user interface and coordinates `Inventory`, `Cart`, and `ReceiptPrinter` classes to guide through a transaction.
  * I used enums (`CustomerType` and `TaxStatus`) to define a fixed set of valid values.

### Instructions to Execute

1.  All the `.java` files are inside the `src` folder. `inventory.txt` file is in the root directory, alongside the `src` and `receipts` folders.

2.  Open a terminal and navigate to the project's directory and compile the source files.

    ```bash
    # inside the root directory, where src and receipts folders are placed
    javac src/*.java
    ```

3.  From the same directory run the app.

    ```bash
    java -cp src Main inventory.txt
    ```

4.  The application will go through the process. A new receipt `.txt` file will be generated in the `receipts` directory after an ok checkout.
