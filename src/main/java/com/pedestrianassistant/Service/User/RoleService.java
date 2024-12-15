package com.pedestrianassistant.Service.User;

import com.pedestrianassistant.Model.User.Role;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing user roles.
 */
public interface RoleService {

    /**
     * Retrieve all roles.
     *
     * @return A list of all Role objects.
     */
    List<Role> findAll();

    /**
     * Retrieve a role by its ID.
     *
     * @param id The ID of the role.
     * @return An Optional containing the Role object if found, or empty if not found.
     */
    Optional<Role> findById(Long id);

    /**
     * Save or update a role.
     *
     * @param role The Role object to be saved or updated.
     * @return The saved or updated Role object.
     */
    Role save(Role role);

    /**
     * Delete a role by its ID.
     *
     * @param id The ID of the role to delete.
     */
    void deleteById(Long id);
}
