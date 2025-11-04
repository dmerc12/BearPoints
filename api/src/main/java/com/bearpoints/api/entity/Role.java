package com.bearpoints.api.entity;

/**
 * Defines user authorization roles within the system.
 * <p>Valid roles:
 * <ul>
 *      <li>Student</li>
 *      <li>Teacher</li>
 *      <li>Admin</li>
 * </ul>
 *
 * @see User
 * @version 1.0
 * @author Dylan Mercer
 */
public enum Role {
    /** Standard student account */
    STUDENT,

    /** Teacher account with classroom privileges */
    TEACHER,

    /** Administrative system account */
    ADMIN
}
