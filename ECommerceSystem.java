import java.util.*;

interface Shippable {
    String getName();
    double getWeight();
}

abstract class Product {
    String name;
    double price;
    int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void reduceQuantity(int amount) {
        this.quantity -= amount;
    }

    public abstract boolean isExpired();

    public abstract boolean requiresShipping();
}

class NonExpiringProduct extends Product {
    public NonExpiringProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean requiresShipping() {
        return false;
    }
}

class ExpiringProduct extends Product implements Shippable {
    double weight;
    boolean expired;

    public ExpiringProduct(String name, double price, int quantity, double weight, boolean expired) {
        super(name, price, quantity);
        this.weight = weight;
        this.expired = expired;
    }

    @Override
    public boolean isExpired() {
        return expired;
    }

    @Override
    public boolean requiresShipping() {
        return true;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}

class ShippableProduct extends Product implements Shippable {
    double weight;

    public ShippableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean requiresShipping() {
        return true;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}

class CartItem {
    Product product;
    int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
}

class Cart {
    List<CartItem> items = new ArrayList<>();

    public void add(Product product, int quantity) {
        if (quantity <= product.getQuantity()) {
            items.add(new CartItem(product, quantity));
        } else {
            throw new IllegalArgumentException("Quantity exceeds available stock.");
        }
    }

    public List<CartItem> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

class Customer {
    String name;
    double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public void deduct(double amount) {
        this.balance -= amount;
    }

    public double getBalance() {
        return balance;
    }

    public String getName() {
        return name;
    }
}

class ShippingService {
    public static void ship(List<Shippable> items) {
        System.out.println("** Shipment notice **");
        double totalWeight = 0;
        for (Shippable item : items) {
            System.out.printf("%s  %.0fg\n", item.getName(), item.getWeight() * 1000);
            totalWeight += item.getWeight();
        }
        System.out.printf("Total package weight %.1fkg\n\n", totalWeight);
    }
}

public class ECommerceSystem {
    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Error: Cart is empty.");
            return;
        }

        double subtotal = 0;
        double shippingFee = 0;
        List<Shippable> shippables = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            Product product = item.product;
            if (product.isExpired()) {
                System.out.println("Error: Product " + product.getName() + " is expired.");
                return;
            }
            if (item.quantity > product.getQuantity()) {
                System.out.println("Error: Insufficient stock for product " + product.getName() + ".");
                return;
            }
            subtotal += product.getPrice() * item.quantity;
            if (product.requiresShipping() && product instanceof Shippable) {
                for (int i = 0; i < item.quantity; i++) {
                    shippables.add((Shippable) product);
                }
            }
        }

        if (!shippables.isEmpty()) {
            shippingFee = 30; // Flat shipping fee
            ShippingService.ship(shippables);
        }

        double totalAmount = subtotal + shippingFee;

        if (customer.getBalance() < totalAmount) {
            System.out.println("Error: Insufficient customer balance.");
            return;
        }

        customer.deduct(totalAmount);

        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.getItems()) {
            System.out.printf("%dx %s\t%.0f\n", item.quantity, item.product.getName(), item.product.getPrice() * item.quantity);
            item.product.reduceQuantity(item.quantity);
        }
        System.out.println("----------------------");
        System.out.printf("Subtotal\t%.0f\n", subtotal);
        System.out.printf("Shipping\t%.0f\n", shippingFee);
        System.out.printf("Amount\t%.0f\n", totalAmount);
        System.out.printf("Balance\t%.0f\n", customer.getBalance());
    }

    public static void main(String[] args) {
        Product cheese = new ExpiringProduct("Cheese", 100, 5, 0.2, false);
        Product biscuits = new ExpiringProduct("Biscuits", 150, 2, 0.7, false);
        Product scratchCard = new NonExpiringProduct("Scratch Card", 50, 10);

        Customer customer = new Customer("John Doe", 1000);

        Cart cart = new Cart();
        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(scratchCard, 1);

        checkout(customer, cart);
    }
}
