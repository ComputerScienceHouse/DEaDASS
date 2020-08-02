package api.model;

import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides structure to map a Validation Exception to an object so that it may be returned as JSON
 *
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class ValidationMapping {

    /** The detailed error messages */
    public final List<String> errors = new ArrayList<>();

    /** The toplevel exception message */
    public final String errorMessage;


    public ValidationMapping(String errorMessage) {
        this.errorMessage = errorMessage;
    }


    /**
     * Store the error details.
     *
     * @param error a specific error that caused a ValidationException
     */
    public void addValidationError(ObjectError error) {
        errors.add(error.getDefaultMessage());
    }
}
