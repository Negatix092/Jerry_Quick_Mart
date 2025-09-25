import java.util.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Inventory {

    private final Map<String, Product> products = new LinkedHashMap<>();

    public static Inventory load(Path path) throws IOException{
        
        Inventory inventory = new Inventory();
        
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        for (String raw : lines) {

            String line = raw.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Name: qty, $reg, $member, Taxable|Exempt
            String[] parts = line.split(":");

            if (parts.length != 2) {
                continue;
            }

            String name = parts[0].trim();
            String[] rest = parts[1].split(",");

            if (rest.length != 4) {
                continue;
            }


            int qty = Integer.parseInt(rest[0].trim());
            BigDecimal reg = parseMoney(rest[1]);
            BigDecimal mem = parseMoney(rest[2]);
            TaxStatus tax = parseTax(rest[3].trim());


            Product p = new Product(name, qty, reg, mem, tax);
            inventory.products.put(name.toLowerCase(Locale.ROOT), p);
        }
        return inventory;
    }

    public void save(Path path) throws IOException {
        List<String> out = new ArrayList<>();
        for (Product p : products.values()) {
            String taxStr = (p.getTaxStatus() == TaxStatus.TAXABLE) ? "Taxable" : "Tax-Exempt";
            out.add(String.format(Locale.US, "%s: %d, $%.2f, $%.2f, %s",
                    p.getName(),
                    p.getQuantity(),
                    p.getRegularPrice(),
                    p.getMemberPrice(),
                    taxStr));
        }
        Files.write(path, out, StandardCharsets.UTF_8);
    }

    private static BigDecimal parseMoney(String s) {
        String cleaned = s.replace("$", "").trim();
        return new BigDecimal(cleaned);
    }


    private static TaxStatus parseTax(String s) {
        String t = s.toLowerCase(Locale.ROOT);
        if (t.contains("taxable")) return TaxStatus.TAXABLE;
        return TaxStatus.EXEMPT;
    }


    public Optional<Product> find(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(products.get(name.toLowerCase(Locale.ROOT)));
    }


    public Collection<Product> all() { return products.values(); }
    
}
