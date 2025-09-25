import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File; 

public class ReceiptPrinter {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMMM d, uuuu", Locale.US);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static Path print(long txNumber, LocalDateTime when, Cart cart, BigDecimal taxRate, BigDecimal cash, Path outDir, CustomerType customerType) throws IOException {
        if (!Files.exists(outDir)) Files.createDirectories(outDir);
        String txId = String.format(Locale.US, "%06d", txNumber);
        String fileName = String.format(Locale.US, "tx_%s_%s.txt", txId, when.format(TS));
        Path file = outDir.resolve(fileName);

        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal tax = cart.getTax(taxRate);
        BigDecimal total = subtotal.add(tax);
        BigDecimal change = cash.subtract(total);

        BigDecimal savings = cart.getSavings(customerType);

        List<String> lines = new ArrayList<>();
        lines.add(when.format(DATE));
        lines.add("TRANSACTION: " + txId);
        lines.add("");
        lines.add(String.format("%-20s %-10s %-12s %-10s", "ITEM", "QTY", "UNIT PRICE", "TOTAL"));
        for (CartItem it : cart.items()) {
            lines.add(String.format(Locale.US, "%-20s %-10d $%-11.2f $%-10.2f",
            it.getName(), it.getQuantity(), it.getUnitPrice(), it.getLineTotal()));
        }
        lines.add("************************");
        lines.add("TOTAL NUMBER OF ITEMS SOLD: " + cart.getTotalUnits());
        lines.add(String.format(Locale.US, "SUB-TOTAL: $%.2f", subtotal));
        lines.add(String.format(Locale.US, "TAX (%.1f%%): $%.2f", taxRate.multiply(BigDecimal.valueOf(100)), tax));
        lines.add(String.format(Locale.US, "TOTAL: $%.2f", total));
        lines.add(String.format(Locale.US, "CASH: $%.2f", cash));
        lines.add(String.format(Locale.US, "CHANGE: $%.2f", change));
        lines.add("************************");
        if (savings.compareTo(BigDecimal.ZERO) > 0) {
            lines.add("");
            lines.add(String.format(Locale.US, "YOU SAVED: $%.2f!", savings));
        }

        Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return file;
    }
    
    public static long getLastTransactionNumber(Path receiptsDir) {
        File dir = receiptsDir.toFile();
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }

        long maxTxNumber = 0;
        Pattern pattern = Pattern.compile("tx_(\\d{6})_.*\\.txt");

        for (File file : dir.listFiles()) {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.matches()) {
                try {
                    long txNumber = Long.parseLong(matcher.group(1));
                    if (txNumber > maxTxNumber) {
                        maxTxNumber = txNumber;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return maxTxNumber;
    }
}