package xyz.rugman27.drycleanerspos.utilites;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A simplified, universal API response wrapper for consistent messages.
 * It can encapsulate either successful data or a single error message.
 *
 * @param <T> The type of data to be returned on success.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields from JSON output for cleaner responses
public class ApiResponse<T> {

    // Common fields for all responses
    private String timestamp;
    private int code; // HTTP status code
    private String message; // Human-readable message
    private String path; // Request path

    // Data payload for success responses
    private T data;

    // --- Private Constructor for Internal Use ---
    // This constructor sets all common fields.
    private ApiResponse(HttpStatus httpStatus, String message, String path, T data) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.code = httpStatus.value();
        this.message = message;
        this.path = path;
        this.data = data; // Will be null for error responses
    }


    // Full success method to be used internally by others for consistency
    public static <T> ResponseEntity<ApiResponse<T>> success(HttpStatus httpStatus, String message, String path, T data) {
        ApiResponse<T> response = new ApiResponse<>(httpStatus, message, path, data);
        return new ResponseEntity<>(response, httpStatus);
    }


    // --- Static Factory Methods for Error Responses ---

    /**
     * Creates an error API response with a basic message and HTTP status.
     * The `data` field will be null.
     *
     * @param httpStatus The HTTP status for the error (e.g., HttpStatus.BAD_REQUEST).
     * @param message    The main error message.
     * @param path       The request path where the error occurred.
     * @return A ResponseEntity containing the error ApiResponse.
     */
    public static ResponseEntity<ApiResponse<Void>> error(HttpStatus httpStatus, String message, String path) {
        return new ResponseEntity<>(new ApiResponse<>(httpStatus, message, path, null), httpStatus);
    }
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus httpStatus, String message, String path, T data) {
        return new ResponseEntity<>(new ApiResponse<>(httpStatus, message, path, data), httpStatus);
    }

}