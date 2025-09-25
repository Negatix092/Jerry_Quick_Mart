import java.math.BigDecimal;

public class Product {

    private final String name;
    private int quantity;
    private final BigDecimal regularPrice;
    private final BigDecimal memberPrice;
    private final TaxStatus taxStatus;

    public Product(String name, int quantity, BigDecimal regularPrice, BigDecimal memberPrice, TaxStatus taxStatus){
        this.name = name;
        this.quantity = quantity;
        this.regularPrice = regularPrice;
        this.memberPrice = memberPrice;
        this.taxStatus = taxStatus; 
    }

    public String getName(){
        return name;
    }

    public int getQuantity(){
        return quantity;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public BigDecimal getRegularPrice(){
        return regularPrice;
    }

    public BigDecimal getMemberPrice(){
        return memberPrice;
    }

    public TaxStatus getTaxStatus(){
        return taxStatus;
    }

    public BigDecimal getPriceFor(CustomerType customerType){
        if(customerType == CustomerType.MEMBER){
            return memberPrice;
        } else {
            return regularPrice;
        }
    }
    
}
