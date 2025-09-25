import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


public class Cart {
    private final Map<String, CartItem> lines = new LinkedHashMap<>();

    
    public void addItem(Product p, int qty, CustomerType type) {
        if (qty <= 0) {
            return;
        }
        
        String key = p.getName().toLowerCase(Locale.ROOT);
        CartItem existing = lines.get(key);

        if (existing == null) {
            lines.put(key, new CartItem(p, type, qty));
        } else {
            existing.setQuantity(existing.getQuantity() + qty);
        }
    }


    public boolean removeItem(String name) {
        return lines.remove(name.toLowerCase(Locale.ROOT)) != null;
    }

    public void clear() { 
        lines.clear(); 
    }

    public Collection<CartItem> items() { 
        return lines.values(); 
    }

    public boolean decrementItem(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        CartItem existing = lines.get(key);

        if (existing == null) {
            return false; 
        }

        if (existing.getQuantity() > 1) {
            existing.setQuantity(existing.getQuantity() - 1);
            return true;
        } else {
            return removeItem(name);
        }
    }
    

    public int getTotalUnits() {
        return lines.values().stream().mapToInt(CartItem::getQuantity).sum();
    }


    public BigDecimal getSubtotal() {
        return lines.values().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public BigDecimal getTax(BigDecimal taxRate) {
        BigDecimal taxBase = lines.values().stream()
                .filter(it -> it.getTaxStatus() == TaxStatus.TAXABLE)
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return taxBase.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal(BigDecimal taxRate) {
        return getSubtotal()
                .add(getTax(taxRate))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isEmpty() { 
        return lines.isEmpty(); 
    }
    
    public BigDecimal getSavings(CustomerType type) {
        if (type != CustomerType.MEMBER) {
            return BigDecimal.ZERO;
        }
        return lines.values().stream()
                .map(item -> item.getProduct().getRegularPrice().subtract(item.getProduct().getMemberPrice()).multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}