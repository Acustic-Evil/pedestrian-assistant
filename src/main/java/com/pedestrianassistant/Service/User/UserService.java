package com.pedestrianassistant.Service.User;

import com.pedestrianassistant.Model.User.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing users.
 */
public interface UserService {

    /**
     * Retrieve all users.
     *
     * @return A list of all User objects.
     */
    List<User> findAll();

    /**
     * Retrieve a user by their ID.
     *
     * @param id The ID of the user.
     * @return An Optional containing the User object if found, or empty if not found.
     */
    Optional<User> findById(Long id);

    /**
     * Retrieve a user by their username.
     *
     * @param username The username of the user.
     * @return An Optional containing the User object if found, or empty if not found.
     */
    Optional<User> findByUsername(String username);

    /**
     * Save or update a user.
     *
     * @param user The User object to be saved or updated.
     * @return The saved or updated User object.
     */
    User save(User user);

    /**
     * Delete a user by their ID.
     *
     * @param id The ID of the user to delete.
     */
    void deleteById(Long id);
}
