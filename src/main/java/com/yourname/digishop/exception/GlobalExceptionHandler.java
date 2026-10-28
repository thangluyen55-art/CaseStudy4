
package com.yourname.digishop.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessException(
            BusinessException ex, Model model) {

        model.addAttribute("error", ex.getMessage());
        return "error/general";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(
            Exception ex,
            HttpServletRequest request,
            Model model) {

        log.error("Unexpected error at {}", request.getRequestURI(), ex);

        model.addAttribute(
                "error",
                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."
        );

        return "error/general";
    }
}
