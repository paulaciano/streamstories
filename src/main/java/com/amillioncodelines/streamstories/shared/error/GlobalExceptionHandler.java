package com.amillioncodelines.streamstories.shared.error;

import java.net.URI;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERRORS_PROPERTY = "errors";
    private static final String SQL_STATE_DATA_EXCEPTION = "22";
    private static final URI ABOUT_BLANK = URI.create("about:blank");

    @ExceptionHandler(ProblemException.class)
    ProblemDetail handleProblem(ProblemException ex) {
        ProblemDetail body = problem(ex.type(), ex.getMessage());
        ex.properties().forEach(body::setProperty);
        return body;
    }

    /**
     * Safety net for races that bypass the service checks; a database constraint has the last word. Invalid values
     * (SQLState class 22) are the client's fault; integrity violations (class 23) are conflicts. The database message
     * is never logged or returned, because it can contain the offending values, such as an email.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String sqlState = sqlState(ex);
        log.info("Request rejected by the database (SQLState {}, constraint {})", sqlState, constraintName(ex));
        if (sqlState != null && sqlState.startsWith(SQL_STATE_DATA_EXCEPTION)) {
            return problem(ProblemType.VALIDATION_ERROR, "A value is out of the allowed range.");
        }
        return problem(ProblemType.CONFLICT, "The operation conflicts with existing data.");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail handleOptimisticLocking(OptimisticLockingFailureException ex) {
        return problem(
                ProblemType.CONFLICT, "The resource was modified by another operation at the same time; try again.");
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .body(problem(ProblemType.UNAUTHORIZED, "A valid access token is required."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(ProblemType.FORBIDDEN, "You do not have permission to perform this operation.");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error while processing request", ex);
        return problem(ProblemType.INTERNAL_ERROR, "An unexpected error occurred.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldError::field))
                .toList();
        return validationProblem(errors, ex, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldError> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new FieldError(
                                result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();
        return validationProblem(errors, ex, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail body = problem(
                ProblemType.MALFORMED_REQUEST, "The request body is not valid JSON or contains unknown fields.");
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail body = problem(ProblemType.NOT_FOUND, "The requested resource does not exist.");
        return handleExceptionInternal(ex, body, headers, HttpStatus.NOT_FOUND, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String parameter = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName()
                : String.valueOf(ex.getPropertyName());
        ProblemDetail body =
                problem(ProblemType.VALIDATION_ERROR, "Parameter '%s' has an invalid value.".formatted(parameter));
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Every other framework error (for example 405 or 415) keeps Spring's detail but gets a StreamStories problem
     * type and title, so clients can rely on {@code type} for every error.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        if (body == null && ex instanceof ErrorResponse error) {
            body = error.getBody();
        }
        if (body instanceof ProblemDetail problem
                && (problem.getType() == null || ABOUT_BLANK.equals(problem.getType()))) {
            ProblemType type = forStatus(statusCode.value());
            if (type != null) {
                problem.setType(type.uri());
                problem.setTitle(type.title());
            }
        }
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private static ProblemType forStatus(int status) {
        return switch (status) {
            case 400 -> ProblemType.MALFORMED_REQUEST;
            case 404 -> ProblemType.NOT_FOUND;
            case 405 -> ProblemType.METHOD_NOT_ALLOWED;
            case 406 -> ProblemType.NOT_ACCEPTABLE;
            case 415 -> ProblemType.UNSUPPORTED_MEDIA_TYPE;
            case 500 -> ProblemType.INTERNAL_ERROR;
            default -> null;
        };
    }

    private ResponseEntity<Object> validationProblem(
            List<FieldError> errors, Exception ex, HttpHeaders headers, WebRequest request) {
        ProblemDetail body = problem(
                ProblemType.VALIDATION_ERROR, "The request has %d validation error(s).".formatted(errors.size()));
        body.setProperty(ERRORS_PROPERTY, errors);
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    private static String sqlState(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getSQLState() != null) {
                return sql.getSQLState();
            }
        }
        return null;
    }

    private static String constraintName(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                return violation.getConstraintName();
            }
        }
        return null;
    }

    private static ProblemDetail problem(ProblemType type, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(type.status(), detail);
        problem.setType(type.uri());
        problem.setTitle(type.title());
        return problem;
    }
}
