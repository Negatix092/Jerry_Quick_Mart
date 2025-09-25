import java.math.BigDecimal;

public class CartItem {
    
    private final Product product; 
    private final String name; 
    private final TaxStatus taxStatus;
    private final BigDecimal unitPrice; 
    private int quantity;


    public CartItem(Product product, CustomerType type, int quantity) {
        this.product = product;
        this.name = product.getName();
        this.taxStatus = product.getTaxStatus();
        this.unitPrice = product.getPriceFor(type);
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public String getName() { 
        return name; 
    }

    public TaxStatus getTaxStatus() { 
        return taxStatus; 
    }

    public BigDecimal getUnitPrice() { 
        return unitPrice; 
    }

    public int getQuantity() { 
        return quantity; 
    }

    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}