package com.idp.idpapi.auth.constants;

public final class AuthMessages {

    public static final String INVALID_CREDENTIALS = "Email hoặc mật khẩu không chính xác.";
    public static final String ACCOUNT_LOCKED = "Tài khoản đang tạm khóa do đăng nhập sai nhiều lần.";
    public static final String ACCOUNT_INACTIVE = "Tài khoản đang bị vô hiệu hóa.";
    public static final String EMAIL_NOT_VERIFIED = "Tài khoản chưa xác thực email.";
    public static final String LOGIN_SUCCESS = "Đăng nhập thành công.";
    public static final String LOGOUT_SUCCESS = "Đăng xuất thành công.";
    public static final String LOGOUT_ALL_SUCCESS = "Đã đăng xuất khỏi tất cả thiết bị.";
    public static final String REFRESH_SUCCESS = "Làm mới access token thành công.";
    public static final String PASSWORD_CHANGED = "Đổi mật khẩu thành công.";
    public static final String PASSWORD_RESET_REQUESTED = "Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi.";
    public static final String PASSWORD_RESET_SUCCESS = "Đặt lại mật khẩu thành công.";
    public static final String EMAIL_VERIFIED = "Xác thực email thành công.";
    public static final String EMAIL_VERIFICATION_SENT = "Nếu email chưa xác thực, liên kết xác thực mới đã được gửi.";
    public static final String PROFILE_LOADED = "Lấy thông tin người dùng thành công.";
    public static final String SESSIONS_LOADED = "Lấy danh sách phiên đăng nhập thành công.";
    public static final String SESSION_REVOKED = "Đăng xuất phiên thành công.";
    public static final String CURRENT_PASSWORD_INCORRECT = "Mật khẩu hiện tại không đúng.";
    public static final String PASSWORD_CONFIRM_MISMATCH = "Mật khẩu xác nhận không khớp.";
    public static final String INVALID_REFRESH_TOKEN = "Refresh token không hợp lệ hoặc đã hết hạn.";
    public static final String INVALID_RESET_TOKEN = "Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.";
    public static final String INVALID_VERIFY_TOKEN = "Token xác thực email không hợp lệ hoặc đã hết hạn.";

    private AuthMessages() {
    }
}
