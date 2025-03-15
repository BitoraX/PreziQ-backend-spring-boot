package com.bitorax.priziq.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    // General errors
    UNCATEGORIZED_EXCEPTION(9999, HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi không xác định do phía hệ thống chưa xử lý"),
    INVALID_KEY(1001, HttpStatus.BAD_REQUEST, "Tên khóa xử lý lỗi không hợp lệ"),
    UNAUTHENTICATED(1002, HttpStatus.UNAUTHORIZED, "Thông tin đăng nhập không đúng"),
    UNAUTHORIZED(1003, HttpStatus.FORBIDDEN, "Truy cập bị từ chối, bạn không có quyền để truy cập tính năng này"),
    SEND_EMAIL_ERROR(1004, HttpStatus.INTERNAL_SERVER_ERROR, "Gửi email không thành công"),
    TOKEN_EXPIRED(1005, HttpStatus.UNAUTHORIZED, "Token đã hết hạn"),
    INVALID_TOKEN(1006, HttpStatus.UNAUTHORIZED, "Token không hợp lệ"),
    NOT_FOUND_ROUTE(1007, HttpStatus.NOT_FOUND, "Không thể tìm thấy tài nguyên trên hệ thống"),
    DATE_FORMAT_INSTANT(1008, HttpStatus.BAD_REQUEST, "Định dạng ngày phải theo chuẩn ISO-8601"),
    DECODE_INVALID_TOKEN(1009, HttpStatus.UNAUTHORIZED, "Xác thực không thành công: Token không hợp lệ, bị thiếu hoặc đã hết hạn"),
    SYSTEM_EMAIL_CANNOT_BE_DELETED(1010, HttpStatus.BAD_REQUEST, "Không thể xóa hoặc thay đổi email mặc định của hệ thống"),
    SYSTEM_ROLE_CANNOT_BE_DELETED(1011, HttpStatus.BAD_REQUEST, "Không thể xóa hoặc thay đổi vai trò mặc định của hệ thống"),
    INVALID_PHONE_NUMBER(1012, HttpStatus.BAD_REQUEST, "Số điện thoại không hợp lệ"),
    PHONE_NUMBER_FORMAT_ERROR(1013, HttpStatus.BAD_REQUEST, "Định dạng số điện thoại không đúng"),
    PHONE_NUMBER_NOT_SUPPORTED(1014, HttpStatus.BAD_REQUEST, "Số điện thoại không được hỗ trợ"),
    INVALID_HTTP_METHOD(1015, HttpStatus.BAD_REQUEST, "Phương thức HTTP không hợp lệ, chỉ hỗ trợ các phương thức: GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS"),
    INVALID_REQUEST_DATA(1016, HttpStatus.BAD_REQUEST, "Dữ liệu trong yêu cầu không hợp lệ. Hãy kiểm tra định dạng, giá trị các trường và gửi lại"),

    // Module auth errors
    EMAIL_NOT_BLANK(1101, HttpStatus.BAD_REQUEST, "Email không được để trống"),
    EMAIL_INVALID(1102, HttpStatus.BAD_REQUEST, "Email không đúng định dạng"),
    EMAIL_PROVIDER_INVALID(1103, HttpStatus.BAD_REQUEST, "Hiện tại hệ thống chỉ hỗ trợ gmail và yopmail"),
    PASSWORD_NOT_BLANK(1104, HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống"),
    PASSWORD_INVALID_FORMAT(1105, HttpStatus.BAD_REQUEST, "Mật khẩu phải có ít nhất 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt"),
    FIRSTNAME_NOT_BLANK(1106, HttpStatus.BAD_REQUEST, "Họ không được để trống"),
    FIRSTNAME_INVALID_LENGTH(1107, HttpStatus.BAD_REQUEST, "Họ phải có độ dài từ 2 đến 50 ký tự"),
    LASTNAME_NOT_BLANK(1108, HttpStatus.BAD_REQUEST, "Tên không được để trống"),
    LASTNAME_INVALID_LENGTH(1109, HttpStatus.BAD_REQUEST, "Tên phải có độ dài từ 2 đến 50 ký tự"),
    PHONE_NUMBER_NOT_BLANK(1110, HttpStatus.BAD_REQUEST, "Số điện thoại không được để trống"),
    PHONE_NUMBER_VN_INVALID(1111, HttpStatus.BAD_REQUEST, "Số điện thoại không hợp lệ tại Việt Nam"),
    EMAIL_EXISTED(1112, HttpStatus.CONFLICT, "Người dùng với email đã tồn tại trên hệ thống"),
    TOKEN_NOT_BLANK(1113, HttpStatus.BAD_REQUEST, "Token không được để trống"),
    PASSWORD_MISMATCH(1114, HttpStatus.UNAUTHORIZED, "Mật khẩu hiện tại không chính xác"),
    NOT_VERIFIED_ACCOUNT_TWICE(1115, HttpStatus.BAD_REQUEST, "Bạn đã xác thực tài khoản trước đó"),
    NOT_VERIFIED_ACCOUNT(1116, HttpStatus.BAD_REQUEST, "Bạn chưa xác thực tài khoản của mình, hãy kiểm tra hoặc gửi lại email xác thực"),
    PHONE_NUMBER_EXISTED(1117, HttpStatus.CONFLICT, "Người dùng với số điện thoại đã được đăng ký trên hệ thống, vui lòng sử dụng số điện thoại khác"),
    NEW_PASSWORD_NOT_BLANK(1118, HttpStatus.BAD_REQUEST, "Mật khẩu mới không được để trống"),
    CONFIRM_PASSWORD_NOT_BLANK(1119, HttpStatus.BAD_REQUEST, "Xác nhận mật khẩu không được để trống"),
    PASSWORD_AND_CONFIRM_MISMATCH(1120, HttpStatus.BAD_REQUEST, "Mật khẩu và xác nhận mật khẩu không khớp"),
    EMAIL_OR_PHONE_REQUIRED(1121, HttpStatus.BAD_REQUEST, "Vui lòng nhập email hoặc số điện thoại để đăng nhập"),
    ONLY_EMAIL_OR_PHONE(1122, HttpStatus.BAD_REQUEST, "Chỉ được phép nhập một trong hai: email hoặc số điện thoại"),
    CONFIRM_PASSWORD_INVALID_FORMAT(1123, HttpStatus.BAD_REQUEST, "Xác nhận mật khẩu phải có ít nhất 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt"),

    // Module user errors
    USER_NOT_FOUND(1201, HttpStatus.NOT_FOUND, "Không tìm thấy người dùng trên hệ thống"),
    NICKNAME_INVALID_LENGTH(1202, HttpStatus.BAD_REQUEST, "Biệt danh phải có độ dài từ 2 đến 50 ký tự"),
    CURRENT_PASSWORD_NOT_BLANK(1203, HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không được để trống"),
    PASSWORD_SAME_AS_CURRENT(1204, HttpStatus.BAD_REQUEST, "Mật khẩu mới phải khác mật khẩu hiện tại"),
    NEW_PHONE_NUMBER_NOT_BLANK(1205, HttpStatus.BAD_REQUEST, "Số điện thoại mới không được để trống"),
    NEW_EMAIL_NOT_BLANK(1206, HttpStatus.BAD_REQUEST, "Địa chỉ email mới không được để trống"),
    NEW_EMAIL_SAME_BEFORE(1207, HttpStatus.BAD_REQUEST, "Địa chỉ email mới trùng với địa chỉ email hiện tại, vui lòng sử dụng địa chỉ email khác"),
    NEW_PHONE_NUMBER_SAME_BEFORE(1208, HttpStatus.BAD_REQUEST, "Số điện thoại mới trùng với số điện thoại hiện tại, vui lòng sử dụng số điện thoại khác"),
    USER_SAME_IS_VERIFY(1209, HttpStatus.BAD_REQUEST, "Người dùng hiện tại đã ở trạng thái xác thực này"),

    // Module role errors
    ROLE_NAME_NOT_FOUND(1301, HttpStatus.NOT_FOUND, "Không tìm thấy tên vai trò trên hệ thống"),
    ROLE_NAME_NOT_BLANK(1302, HttpStatus.BAD_REQUEST, "Tên vai trò không được để trống"),
    ROLE_NAME_EXISTED(1303, HttpStatus.CONFLICT, "Tên vai trò đã tồn tại trên hệ thống, vui lòng sử dụng tên khác"),
    ROLE_NOT_FOUND(1304, HttpStatus.NOT_FOUND, "Không tìm thấy vai trò trên hệ thống"),
    DUPLICATE_ROLE_IDS(1305, HttpStatus.BAD_REQUEST, "Danh sách vai trò chứa các ID trùng lặp, vui lòng kiểm tra lại"),
    PERMISSION_ALREADY_EXISTS_IN_ROLE(1306, HttpStatus.BAD_REQUEST, "Vai trò hiện tại đã có các quyền hạn này"),
    ROLE_ALREADY_ASSIGNED(1307, HttpStatus.BAD_REQUEST, "Người dùng đã có vai trò này nên không thể thêm mới"),
    ROLE_SAME_IS_ACTIVE(1308, HttpStatus.BAD_REQUEST, "Vai trò hiện tại đã ở trạng thái hoạt động này"),

    // Module permission errors
    PERMISSION_NAME_NOT_BLANK(1401, HttpStatus.BAD_REQUEST, "Tên quyền hạn không được để trống"),
    PERMISSION_API_PATH_NOT_BLANK(1402, HttpStatus.BAD_REQUEST, "API path không được để trống"),
    PERMISSION_MODULE_NOT_BLANK(1403, HttpStatus.BAD_REQUEST, "Tên module không được để trống"),
    PERMISSION_HTTP_METHOD_NOT_BLANK(1404, HttpStatus.BAD_REQUEST, "HTTP method không được để trống"),
    PERMISSION_NAME_EXISTED(1405, HttpStatus.CONFLICT, "Tên quyền hạn đã tồn tại trên hệ thống, vui lòng sử dụng tên khác"),
    PERMISSION_PATH_AND_METHOD_EXISTED(1406, HttpStatus.CONFLICT, "API path và method đã tồn tại trên hệ thống"),
    PERMISSION_NOT_FOUND(1407, HttpStatus.NOT_FOUND, "Không tìm thấy quyền hạn trên hệ thống"),
    DUPLICATE_PERMISSION_IDS(1408, HttpStatus.BAD_REQUEST, "Danh sách quyền hạn chứa các ID trùng lặp, vui lòng kiểm tra lại"),
    PERMISSION_IDS_NOT_BLANK(1409, HttpStatus.BAD_REQUEST, "Danh sách ID các quyền hạn không được để trống, vui lòng bổ sung thêm"),
    PERMISSION_NOT_IN_ROLE(1410, HttpStatus.BAD_REQUEST, "Quyền hạn không tồn tại trong vai trò hiện tại"),
    PERMISSION_MODULE_NOT_FOUND(1411, HttpStatus.NOT_FOUND, "Module không tồn tại trên hệ thống"),
    PERMISSION_ALREADY_IN_ANOTHER_MODULE(1412, HttpStatus.BAD_REQUEST, "Quyền hạn đã thuộc về một module khác"),
    PERMISSION_MODULE_NAME_EXISTED(1413, HttpStatus.CONFLICT, "Tên module đã tồn tại trên hệ thống, vui lòng sử dụng tên khác"),
    PERMISSION_MODULE_NAME_INVALID(1414, HttpStatus.BAD_REQUEST, "Tên module không hợp lệ, vui lòng sử dụng tên khác"),

    // Module upload files
    FILE_NOT_FOUND(1501, HttpStatus.NOT_FOUND, "Không tìm thấy file trên hệ thống"),
    FILE_UPLOAD_FAILED(1502, HttpStatus.INTERNAL_SERVER_ERROR, "Tải lên file thất bại, vui lòng thử lại"),
    FILE_TYPE_NOT_ALLOWED(1503, HttpStatus.BAD_REQUEST, "Loại file không được phép, vui lòng kiểm tra lại"),
    FILE_TOO_LARGE(1504, HttpStatus.BAD_REQUEST, "Kích thước file vượt quá giới hạn cho phép"),
    FILE_TOO_SMALL(1505, HttpStatus.BAD_REQUEST, "Kích thước file quá nhỏ, không đạt yêu cầu"),
    INVALID_FILE_NAME(1506, HttpStatus.BAD_REQUEST, "Tên file không hợp lệ, vui lòng kiểm tra lại"),
    FILE_DELETE_FAILED(1507, HttpStatus.INTERNAL_SERVER_ERROR, "Xóa file thất bại, vui lòng thử lại"),
    FILE_MOVE_FAILED(1508, HttpStatus.INTERNAL_SERVER_ERROR, "Di chuyển file thất bại, vui lòng thử lại"),
    MULTIPLE_FILE_UPLOAD_PARTIALLY_FAILED(1509, HttpStatus.PARTIAL_CONTENT, "Một hoặc nhiều file tải lên thất bại, vui lòng kiểm tra lại"),
    FILE_ALREADY_EXISTS(1510, HttpStatus.CONFLICT, "File đã tồn tại trên hệ thống, vui lòng sử dụng tên khác hoặc đổi tên"),
    FOLDER_NOT_FOUND(1511, HttpStatus.NOT_FOUND, "Không tìm thấy thư mục trên hệ thống"),
    FOLDER_CREATION_FAILED(1512, HttpStatus.INTERNAL_SERVER_ERROR, "Tạo thư mục thất bại, vui lòng thử lại"),
    FOLDER_DELETE_FAILED(1513, HttpStatus.INTERNAL_SERVER_ERROR, "Xóa thư mục thất bại, vui lòng thử lại"),
    FILE_NOT_BLANK(1514, HttpStatus.NOT_FOUND, "File không được để trống"),
    INVALID_FILE_PATH(1515, HttpStatus.BAD_REQUEST, "Đường dẫn file không hợp lệ, cú pháp đúng: folderName/fileName.ext"),
    EMPTY_SOURCE_KEY(1516, HttpStatus.BAD_REQUEST, "Đường dẫn file nguồn (sourceKey) không được để trống"),
    EMPTY_SOURCE_LIST(1517, HttpStatus.BAD_REQUEST, "Danh sách file nguồn (sourceKeys) không được để trống"),
    DESTINATION_FOLDER_EMPTY(1518, HttpStatus.BAD_REQUEST, "Tên thư mục đích (destinationFolder) không được để trống"),

    ;

    int code;
    HttpStatusCode statusCode;
    String message;
}
