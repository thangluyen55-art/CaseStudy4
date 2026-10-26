package com.yourname.digishop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CheckoutForm {
    @NotBlank(message = "Vui lòng nhập tên người nhận")
    private String receiverName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^(0|\\+84)\\d{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng")
    @Size(min = 10, message = "Địa chỉ quá ngắn, hãy ghi đủ số nhà, đường, quận, thành phố")
    private String address;

    @Pattern(regexp = "COD|BANK_TRANSFER", message = "Phương thức thanh toán không hợp lệ")
    private String paymentMethod = "COD";
}