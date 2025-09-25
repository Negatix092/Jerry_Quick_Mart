import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.065");
    private static final AtomicLong TX_COUNTER = new AtomicLong(1 + ReceiptPrinter.getLastTransactionNumber(Path.of("receipts")));

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        
        if (args.length < 1) {
            System.out.println("Usage: java Main <path/to/inventory.txt>");
            return;
        }
        
        Path inventoryPath = Path.of(args[0]);
        Inventory inventory = Inventory.load(inventoryPath);
        Scanner sc = new Scanner(System.in);

        System.out.println("\nGRAND OPENING OF JERRY'S QUICK MART!\n");
        
        while(true) {
            CustomerType customerType = chooseCustomerType(sc);
            Cart cart = new Cart();

            boolean transactionActive = true;
            while (transactionActive) {
                printMenu();
                String op = sc.nextLine().trim();
                switch (op) {
                    case "1" -> listInventory(inventory);
                    case "2" -> addToCart(sc, inventory, cart, customerType);
                    case "3" -> removeAdjustItem(sc, cart); 
                    case "4" -> {
                        // if cart is empty
                        if (cart.isEmpty()){
                            System.out.println("\nCart is already empty.\n"); 
                            break; 
                        }
                        else {
                            // confirm before emptying
                            System.out.print("\nAre you sure you want to empty the cart (y/n)? ");
                            String confirm = sc.nextLine().trim().toLowerCase();
                            if (!confirm.equals("y") && !confirm.equals("yes")) {
                                System.out.println("\nCart not emptied.\n");
                                break;
                            } else {
                                cart.clear(); 
                                System.out.println("\nCart emptied.\n");
                            }
                        } 
                         
                    }
                    case "5" -> {
                        // if cart is empty
                        if (cart.isEmpty()){
                            System.out.println("\nCart is empty.\n"); 
                            break; 
                        }
                        else {
                            viewCart(cart, customerType); 
                        }
                    }
                    case "6" -> {
                        if (cart.isEmpty()){ 
                            System.out.println("\nCart is empty.\n"); 
                            break; 
                        }
                        boolean ok = checkout(sc, cart, inventory, inventoryPath, customerType);
                        if (ok){
                            transactionActive = false;
                        } 
                    }
                    case "7" -> { 
                        System.out.println("\nTransaction canceled. Returning to main menu.\n"); 
                        transactionActive = false; 
                    }
                    default -> System.out.println("\nInvalid option. Please, try again.\n");
                }
            }
            
            System.out.print("\nStart another transaction (y/n)? ");
            String another = sc.nextLine().trim().toLowerCase();
            if (!another.equals("y") && !another.equals("yes")) {
                System.out.println("\nThank you and have a good day. Goodbye!\n");
                return; 
            }
        }
    }

    private static CustomerType chooseCustomerType(Scanner sc) {
        while (true) {
            System.out.println("\nCustomer Type:\n 1 - REGULAR\n 2 - MEMBER\n");
            String s = sc.nextLine().trim();
            
            if ("1".equals(s)){
                return CustomerType.REGULAR;
            }
            if ("2".equals(s)){ 
                return CustomerType.MEMBER;
            }
            
            System.out.println("\nInvalid option. Please, try again.\n");
        }
    }

    private static void printMenu() {
        System.out.println("\nPlease choose an option:\n");
        System.out.println("1 - Check inventory");
        System.out.println("2 - Add to cart");
        System.out.println("3 - Remove/Adjust item from cart");
        System.out.println("4 - Empty cart");
        System.out.println("5 - View cart");
        System.out.println("6 - Checkout");
        System.out.println("7 - Cancel transaction");
        System.out.print("\nSelect an option: ");
    }

    private static void listInventory(Inventory inventory) {
        System.out.printf("\n%-20s %-6s %-10s %-10s %-8s%n", "ITEM", "STOCK", "REGULAR", "MEMBER", "TAX");
        for (Product p : inventory.all()) {
            System.out.printf(Locale.US, "%-20s %-6d $%-9.2f $%-9.2f %-8s%n", 
            p.getName(), 
            p.getQuantity(), 
            p.getRegularPrice(), 
            p.getMemberPrice(), 
            p.getTaxStatus());
        }
    }

    private static void addToCart(Scanner sc, Inventory inventory, Cart cart, CustomerType type) {
        System.out.print("\nProduct name: ");
        String name = sc.nextLine().trim();
        Optional<Product> opt = inventory.find(name);
        if (opt.isEmpty()) { System.out.println("Not found in inventory"); return; }
        Product p = opt.get();
        System.out.print("Quantity: ");
        int qty;
        try {
            qty = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) { 
            System.out.println("\nInvalid quantity."); 
            return; 
        }
        if (qty <= 0) { 
            System.out.println("\nQuantity must be greater than 0."); 
            return; 
        }
        if (qty > p.getQuantity()) { 
            System.out.println("\nInsufficient stock. Available: " + p.getQuantity()); 
            return; 
        }
        cart.addItem(p, qty, type);
        System.out.println("\nAdded: " + p.getName() + " x" + qty + " at $" + p.getPriceFor(type));
    }
    
    private static void removeAdjustItem(Scanner sc, Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("\nCart is empty.\n");
            return;
        }

        String name;
        Optional<CartItem> itemInCart = Optional.empty(); // Initialize to an empty Optional

        while (true) {
            System.out.print("\nEnter item name to remove or adjust: ");
            String finalName = sc.nextLine().trim(); // Use a new variable that's effectively final

            if (finalName.equalsIgnoreCase("cancel")) {
                System.out.println("\nOperation canceled.\n");
                return;
            }

            itemInCart = cart.items().stream()
                    .filter(item -> item.getName().equalsIgnoreCase(finalName))
                    .findFirst();

            if (itemInCart.isPresent()) {
                // Assign the name for later use and break the loop
                name = finalName;
                break;
            } else {
                System.out.println("\nItem not found in cart. Please try again or type 'cancel'.");
            }
        }

        System.out.print("Enter quantity to remove (or 'all' to remove completely): ");
        String input = sc.nextLine().trim();

        if (input.equalsIgnoreCase("all")) {
            if (cart.removeItem(name)) { // Use the now-final name variable
                System.out.println("\nAll units of " + name + " removed from cart.");
            }
        } else {
            try {
                int qtyToRemove = Integer.parseInt(input);
                if (qtyToRemove <= 0) {
                    System.out.println("\nQuantity to remove must be greater than 0.");
                    return;
                }

                if (itemInCart.isPresent()) {
                    int currentQty = itemInCart.get().getQuantity();
                    if (qtyToRemove >= currentQty) {
                        cart.removeItem(name);
                        System.out.println("\nAll units of " + name + " removed from cart.");
                    } else {
                        itemInCart.get().setQuantity(currentQty - qtyToRemove);
                        System.out.println(qtyToRemove + " units of " + name + " removed. New quantity is " + itemInCart.get().getQuantity() + ".");
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input. Please enter a number or 'all'.");
            }
        }
    }

    private static void viewCart(Cart cart, CustomerType type) {
        System.out.printf("\n%-20s %-6s %-10s %-10s%n", "ITEM", "QTY", "UNIT", "TOTAL");
        for (CartItem it : cart.items()) {
            System.out.printf(Locale.US, "%-20s %-6d $%-9.2f $%-9.2f%n", 
                it.getName(), 
                it.getQuantity(), 
                it.getUnitPrice(), 
                it.getLineTotal());
        }
        System.out.println("************************");
        System.out.printf(Locale.US, "SUB-TOTAL: $%.2f%n", cart.getSubtotal());
        System.out.printf(Locale.US, "TAX: $%.2f%n", cart.getTax(TAX_RATE));
        System.out.printf(Locale.US, "TOTAL: $%.2f%n", cart.getTotal(TAX_RATE));

        BigDecimal savings = cart.getSavings(type);
        if (savings.compareTo(BigDecimal.ZERO) > 0) {
            System.out.printf(Locale.US, "YOU WILL SAVE: $%.2f!%n", savings);
        }
    }
    
    
    private static boolean checkout(Scanner sc, Cart cart, Inventory inventory, Path inventoryPath, CustomerType type) {
        viewCart(cart, type); 
        BigDecimal total = cart.getTotal(TAX_RATE);

        BigDecimal cash;
        while (true) {
            System.out.print("Cash received: $");
            try { 
                cash = new BigDecimal(sc.nextLine().trim()); 
                if (cash.compareTo(total) < 0) { 
                    System.out.println("\nNot enough cash provided. Please try again.");
                } else {
                    break; 
                }
            } catch (Exception e) { 
                System.out.println("\nInvalid amount. Please enter a number."); 
            }
        }

        BigDecimal change = cash.subtract(total);
        System.out.printf("\nChange: $%.2f%n", change);

        for (CartItem it : cart.items()) {
            inventory.find(it.getName()).ifPresent(p -> p.setQuantity(p.getQuantity() - it.getQuantity()));
        }

        try {
            inventory.save(inventoryPath);
            long tx = TX_COUNTER.getAndIncrement();
            var when = LocalDateTime.now();
            var receipt = ReceiptPrinter.print(tx, when, cart, TAX_RATE, cash.setScale(2, RoundingMode.HALF_UP), Path.of("receipts"), type);
            System.out.println("\nReceipt created: " + receipt.toAbsolutePath());
            return true;
        } catch (IOException e) {
            System.out.println("\nError saving receipt or inventory " + e.getMessage());
            return false;
        }
    }
}