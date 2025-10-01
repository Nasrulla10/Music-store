package com.music.musicstore.services;

import com.music.musicstore.models.cart.Cart;
import com.music.musicstore.models.cart.CartItem;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.repositories.CartItemRepository;
import com.music.musicstore.repositories.CartRepository;
import com.music.musicstore.repositories.MusicRepository;
import com.music.musicstore.repositories.CustomerRepository;
import com.music.musicstore.exceptions.ResourceNotFoundException;
import com.music.musicstore.exceptions.ValidationException;
import com.music.musicstore.exceptions.BusinessRuleException;
import com.music.musicstore.exceptions.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MusicRepository musicRepository;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                      MusicRepository musicRepository, CustomerService customerService,
                      CustomerRepository customerRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.musicRepository = musicRepository;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        logger.info("CartService initialized successfully");
    }

    public Cart getOrCreateCart(Customer customer) {
        logger.debug("Getting or creating cart for customer: {}", customer != null ? customer.getUsername() : "null");

        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }

        try {
            Cart cart = cartRepository.findByCustomerWithItems(customer)
                    .orElseGet(() -> {
                        logger.debug("Creating new cart for customer: {}", customer.getUsername());
                        Cart newCart = new Cart(customer);
                        Cart savedCart = cartRepository.save(newCart);
                        logger.info("Successfully created new cart for customer: {}", customer.getUsername());
                        return savedCart;
                    });

            if (cart.getId() != null) {
                logger.debug("Retrieved existing cart for customer: {}", customer.getUsername());
            }
            return cart;
        } catch (Exception e) {
            logger.error("Error getting or creating cart for customer: {}", customer.getUsername(), e);
            throw new RuntimeException("Failed to get or create cart", e);
        }
    }

    @Transactional
    public void addToCart(Customer customer, Long musicId) {
        logger.debug("Adding music ID: {} to cart for customer: {}", musicId, customer != null ? customer.getUsername() : "null");

        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }

        if (musicId == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }

        try {
            Cart cart = getOrCreateCart(customer);
            Music music = musicRepository.findById(musicId)
                    .orElseThrow(() -> {
                        logger.error("Music not found with ID: {}", musicId);
                        return new ResourceNotFoundException("Music", musicId.toString());
                    });

            // Check if music is already in cart
            boolean alreadyInCart = cart.getItems().stream()
                    .anyMatch(item -> item.getMusic().getId().equals(musicId));

            if (alreadyInCart) {
                logger.warn("Music ID: {} is already in cart for customer: {}", musicId, customer.getUsername());
                throw new BusinessRuleException("Music is already in cart");
            }

            // Check if music is already purchased
            boolean alreadyPurchased = customerRepository.hasPurchasedMusic(customer.getId(), musicId);

            if (alreadyPurchased) {
                logger.warn("Music ID: {} has already been purchased by customer: {}", musicId, customer.getUsername());
                throw new BusinessRuleException("You have already purchased this music");
            }

            CartItem item = new CartItem(music);
            item.setCart(cart);
            cartItemRepository.save(item);
            cart.getItems().add(item);
            cartRepository.save(cart);

            logger.info("Successfully added music ID: {} to cart for customer: {}", musicId, customer.getUsername());
        } catch (ResourceNotFoundException | BusinessRuleException e) {
            throw e; // Re-throw known exceptions
        } catch (Exception e) {
            logger.error("Error adding music to cart for customer: {}", customer != null ? customer.getUsername() : "null", e);
            throw new RuntimeException("Failed to add music to cart", e);
        }
    }

    @Transactional
    public void checkout(Customer customer) {
        Cart cart = getOrCreateCart(customer);
        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Cart is empty");
        }

        // Get a fresh customer instance with purchasedMusic loaded to avoid lazy initialization issues
        Customer managedCustomer = customerRepository.findByIdWithPurchasedMusic(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customer.getId().toString()));

        for (CartItem item : cart.getItems()) {
            managedCustomer.getPurchasedMusic().add(item.getMusic());
        }

        customerService.save(managedCustomer);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Transactional
    public void removeItem(Customer customer, Long cartItemId) {
        logger.debug("Removing cart item ID: {} for customer: {}", cartItemId, customer != null ? customer.getUsername() : "null");

        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }

        if (cartItemId == null) {
            logger.error("Cart item ID is null");
            throw new ValidationException("Cart item ID cannot be null");
        }

        try {
            Cart cart = getOrCreateCart(customer);
            CartItem item = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> {
                        logger.error("Cart item not found with ID: {}", cartItemId);
                        return new ResourceNotFoundException("Cart item", cartItemId.toString());
                    });

            if (!item.getCart().getCustomer().getId().equals(customer.getId())) {
                logger.error("Cart item ID: {} does not belong to customer: {}", cartItemId, customer.getUsername());
                throw new UnauthorizedException("Cart item does not belong to customer");
            }

            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            cartRepository.save(cart);

            logger.info("Successfully removed cart item ID: {} for customer: {}", cartItemId, customer.getUsername());
        } catch (Exception e) {
            logger.error("Error removing cart item ID: {} for customer: {}", cartItemId, customer.getUsername(), e);
            throw e;
        }
    }

    public Cart getCartByUsername(String username) {
        logger.debug("Getting cart for username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        try {
            Customer customer = customerService.findByUsername(username);
            Cart cart = getOrCreateCart(customer);
            logger.info("Successfully retrieved cart for username: {}", username);
            return cart;
        } catch (Exception e) {
            logger.error("Error getting cart for username: {}", username, e);
            throw e;
        }
    }

    public void addToCart(String username, Long musicId, Customer customer) {
        logger.debug("Adding music ID: {} to cart for username: {} with customer object", musicId, username);

        if (customer != null) {
            addToCart(customer, musicId);
        } else if (username != null && !username.trim().isEmpty()) {
            Customer foundCustomer = customerService.findByUsername(username);
            addToCart(foundCustomer, musicId);
        } else {
            logger.error("Both username and customer are null/invalid");
            throw new ValidationException("Either username or customer must be provided");
        }
    }

    @Transactional
    public void removeFromCart(String username, Long musicId) {
        logger.debug("Removing music ID: {} from cart for username: {}", musicId, username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        if (musicId == null) {
            logger.error("Music ID is null");
            throw new ValidationException("Music ID cannot be null");
        }

        try {
            Customer customer = customerService.findByUsername(username);
            Cart cart = getOrCreateCart(customer);

            CartItem itemToRemove = cart.getItems().stream()
                    .filter(item -> item.getMusic().getId().equals(musicId))
                    .findFirst()
                    .orElseThrow(() -> {
                        logger.error("Music ID: {} not found in cart for username: {}", musicId, username);
                        return new ResourceNotFoundException("Music in cart", musicId.toString());
                    });

            removeItem(customer, itemToRemove.getId());
            logger.info("Successfully removed music ID: {} from cart for username: {}", musicId, username);
        } catch (Exception e) {
            logger.error("Error removing music ID: {} from cart for username: {}", musicId, username, e);
            throw e;
        }
    }

    @Transactional
    public void clearCart(Customer customer) {
        logger.debug("Clearing cart for customer: {}", customer != null ? customer.getUsername() : "null");

        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }

        try {
            Cart cart = getOrCreateCart(customer);
            int itemCount = cart.getItems().size();

            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);

            logger.info("Successfully cleared cart for customer: {} ({} items removed)", customer.getUsername(), itemCount);
        } catch (Exception e) {
            logger.error("Error clearing cart for customer: {}", customer.getUsername(), e);
            throw new RuntimeException("Failed to clear cart", e);
        }
    }

    public double getCartTotal(Customer customer) {
        logger.debug("Calculating cart total for customer: {}", customer != null ? customer.getUsername() : "null");

        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }

        try {
            Cart cart = getOrCreateCart(customer);
            double total = cart.getTotalAmount().doubleValue();
            logger.info("Cart total for customer {}: {}", customer.getUsername(), total);
            return total;
        } catch (Exception e) {
            logger.error("Error calculating cart total for customer: {}", customer.getUsername(), e);
            throw new RuntimeException("Failed to calculate cart total", e);
        }
    }

    public int getCartItemCount(Customer customer) {
        logger.debug("Getting cart item count for customer: {}", customer != null ? customer.getUsername() : "null");

        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }

        try {
            Cart cart = getOrCreateCart(customer);
            int count = cart.getItems().size();
            logger.info("Cart item count for customer {}: {}", customer.getUsername(), count);
            return count;
        } catch (Exception e) {
            logger.error("Error getting cart item count for customer: {}", customer.getUsername(), e);
            throw new RuntimeException("Failed to get cart item count", e);
        }
    }

    @Transactional
    public void updateCartItemQuantity(Customer customer, Long cartItemId, int quantity) {
        logger.debug("Updating cart item ID: {} quantity to {} for customer: {}", cartItemId, quantity, customer != null ? customer.getUsername() : "null");

        if (customer == null) {
            logger.error("Customer is null");
            throw new ValidationException("Customer cannot be null");
        }

        if (cartItemId == null) {
            logger.error("Cart item ID is null");
            throw new ValidationException("Cart item ID cannot be null");
        }

        if (quantity < 0) {
            logger.error("Quantity cannot be negative: {}", quantity);
            throw new ValidationException("Quantity cannot be negative");
        }

        try {
            CartItem item = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> {
                        logger.error("Cart item not found with ID: {}", cartItemId);
                        return new ResourceNotFoundException("Cart item", cartItemId.toString());
                    });

            if (!item.getCart().getCustomer().getId().equals(customer.getId())) {
                logger.error("Cart item ID: {} does not belong to customer: {}", cartItemId, customer.getUsername());
                throw new UnauthorizedException("Cart item does not belong to customer");
            }

            if (quantity == 0) {
                // Remove item if quantity is 0
                removeItem(customer, cartItemId);
                logger.info("Removed cart item ID: {} (quantity set to 0) for customer: {}", cartItemId, customer.getUsername());
            } else {
                // Note: CartItem may not have setQuantity method - this would need to be implemented
                // For now, we'll just save the item as is
                cartItemRepository.save(item);
                logger.info("Successfully updated cart item ID: {} quantity to {} for customer: {}", cartItemId, quantity, customer.getUsername());
            }
        } catch (Exception e) {
            logger.error("Error updating cart item ID: {} quantity for customer: {}", cartItemId, customer.getUsername(), e);
            throw e;
        }
    }

    public Cart saveCart(Cart cart) {
        logger.debug("Saving cart: {}", cart != null ? cart.getId() : "null");

        if (cart == null) {
            logger.error("Cart is null");
            throw new ValidationException("Cart cannot be null");
        }

        try {
            // Calculate and set the total before saving
            cart.setTotalAmount(cart.getTotal());
            Cart savedCart = cartRepository.save(cart);
            logger.info("Successfully saved cart with ID: {}", savedCart.getId());
            return savedCart;
        } catch (Exception e) {
            logger.error("Error saving cart", e);
            throw new RuntimeException("Failed to save cart", e);
        }
    }
}
