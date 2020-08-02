package api;


import api.model.ValidationMapping;
import api.model.exception.BadRequestException;
import api.model.exception.DeadassException;
import api.model.exception.NotFoundException;
import api.model.exception.SQLException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@ControllerAdvice
@RequestMapping(produces = "application/json")
public class ExceptionControllerAdvice {

    @ExceptionHandler({BadRequestException.class, org.springframework.http.converter.HttpMessageNotReadableException.class})
    public ResponseEntity<String> badRequest(Exception e) {
        return error(e, HttpStatus.BAD_REQUEST);
    }


    /**
     * Handles validation failures (e.g. missing body fields)
     *
     * @param error
     * @return
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ValidationMapping> invalidArgs(MethodArgumentNotValidException error) {
        BindingResult errors = error.getBindingResult();
        ValidationMapping error_map = new ValidationMapping("Validation failed. " + errors.getErrorCount() + " error(s)");
        for (ObjectError objectError : errors.getAllErrors()) {
            error_map.addValidationError(objectError);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error_map);
    }


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> notFound(NotFoundException e) {
        return error(e, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler({SQLException.class, java.sql.SQLException.class, DeadassException.class, Exception.class})
    public ResponseEntity internalServerError(Exception e) {
        return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private ResponseEntity<String> error(final Exception exception, final HttpStatus httpStatus) {
        // TODO Proper JSON API error format
        // TODO logging
        final String message = httpStatus.getReasonPhrase() + Optional.of(": " + exception.getMessage()).orElse("");
        return new ResponseEntity<>(message, httpStatus);
    }

}
