package com.rental.exception;

/**
 * User-defined (checked) exception.
 * Thrown when a business rule fails: date conflicts, vehicle not available,
 * bad form input, etc. The message is shown to the user as it is.
 */
public class BookingException extends Exception {

    public BookingException(String message) {
        super(message);
    }
}
