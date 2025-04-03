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
    UNCATEGORIZED_EXCEPTION(9999, HttpStatus.INTERNAL_SERVER_ERROR, "Uncategorized exception due to an unhandled system error"),
    INVALID_KEY(1001, HttpStatus.BAD_REQUEST, "Invalid error handling key"),
    UNAUTHENTICATED(1002, HttpStatus.UNAUTHORIZED, "Invalid login credentials"),
    UNAUTHORIZED(1003, HttpStatus.FORBIDDEN, "Access denied, you do not have permission to access this feature"),
    SEND_EMAIL_ERROR(1004, HttpStatus.INTERNAL_SERVER_ERROR, "Email sending failed"),
    TOKEN_EXPIRED(1005, HttpStatus.UNAUTHORIZED, "Token has expired"),
    INVALID_TOKEN(1006, HttpStatus.UNAUTHORIZED, "Invalid token"),
    NOT_FOUND_ROUTE(1007, HttpStatus.NOT_FOUND, "Resource not found"),
    DATE_FORMAT_INSTANT(1008, HttpStatus.BAD_REQUEST, "Date format must follow ISO-8601"),
    MISSING_TOKEN(1009, HttpStatus.UNAUTHORIZED, "Token is missing"),
    SYSTEM_EMAIL_CANNOT_BE_DELETED(1010, HttpStatus.BAD_REQUEST, "Cannot delete or modify the system default email"),
    SYSTEM_ROLE_CANNOT_BE_DELETED(1011, HttpStatus.BAD_REQUEST, "Cannot delete or modify the system default role"),
    INVALID_PHONE_NUMBER(1012, HttpStatus.BAD_REQUEST, "Invalid phone number"),
    PHONE_NUMBER_FORMAT_ERROR(1013, HttpStatus.BAD_REQUEST, "Phone number format is incorrect"),
    PHONE_NUMBER_NOT_SUPPORTED(1014, HttpStatus.BAD_REQUEST, "Phone number is not supported"),
    INVALID_HTTP_METHOD(1015, HttpStatus.BAD_REQUEST, "Invalid HTTP method. Only the following methods are supported: GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS"),
    INVALID_REQUEST_DATA(1016, HttpStatus.BAD_REQUEST, "Invalid request data. Please check field formats and values and try again"),

    // Module auth errors
    EMAIL_NOT_BLANK(1101, HttpStatus.BAD_REQUEST, "Email must not be blank"),
    EMAIL_INVALID(1102, HttpStatus.BAD_REQUEST, "Invalid email format"),
    EMAIL_PROVIDER_INVALID(1103, HttpStatus.BAD_REQUEST, "Only Gmail and Yopmail email providers are supported"),
    PASSWORD_NOT_BLANK(1104, HttpStatus.BAD_REQUEST, "Password must not be blank"),
    PASSWORD_INVALID_FORMAT(1105, HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long, including at least 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character"),
    FIRSTNAME_NOT_BLANK(1106, HttpStatus.BAD_REQUEST, "First name must not be blank"),
    FIRSTNAME_INVALID_LENGTH(1107, HttpStatus.BAD_REQUEST, "First name must be between 2 and 50 characters long"),
    LASTNAME_NOT_BLANK(1108, HttpStatus.BAD_REQUEST, "Last name must not be blank"),
    LASTNAME_INVALID_LENGTH(1109, HttpStatus.BAD_REQUEST, "Last name must be between 2 and 50 characters long"),
    PHONE_NUMBER_NOT_BLANK(1110, HttpStatus.BAD_REQUEST, "Phone number must not be blank"),
    PHONE_NUMBER_VN_INVALID(1111, HttpStatus.BAD_REQUEST, "Invalid Vietnamese phone number"),
    EMAIL_EXISTED(1112, HttpStatus.CONFLICT, "A user with this email already exists in the system"),
    TOKEN_NOT_BLANK(1113, HttpStatus.BAD_REQUEST, "Token must not be blank"),
    PASSWORD_MISMATCH(1114, HttpStatus.UNAUTHORIZED, "Current password is incorrect"),
    NOT_VERIFIED_ACCOUNT_TWICE(1115, HttpStatus.BAD_REQUEST, "You have already verified your account"),
    NOT_VERIFIED_ACCOUNT(1116, HttpStatus.BAD_REQUEST, "Your account is not verified. Please check your email or request a new verification email"),
    PHONE_NUMBER_EXISTED(1117, HttpStatus.CONFLICT, "A user with this phone number already exists. Please use a different phone number"),
    NEW_PASSWORD_NOT_BLANK(1118, HttpStatus.BAD_REQUEST, "New password must not be blank"),
    CONFIRM_PASSWORD_NOT_BLANK(1119, HttpStatus.BAD_REQUEST, "Confirm password must not be blank"),
    PASSWORD_AND_CONFIRM_MISMATCH(1120, HttpStatus.BAD_REQUEST, "Password and confirm password do not match"),
    EMAIL_OR_PHONE_REQUIRED(1121, HttpStatus.BAD_REQUEST, "Please enter an email or phone number to log in"),
    ONLY_EMAIL_OR_PHONE(1122, HttpStatus.BAD_REQUEST, "Only one of email or phone number is allowed"),
    CONFIRM_PASSWORD_INVALID_FORMAT(1123, HttpStatus.BAD_REQUEST, "Confirm password must be at least 8 characters long, including at least 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character"),

    // Module user errors
    USER_NOT_FOUND(1201, HttpStatus.NOT_FOUND, "User not found"),
    NICKNAME_INVALID_LENGTH(1202, HttpStatus.BAD_REQUEST, "Nickname must be between 2 and 50 characters long"),
    CURRENT_PASSWORD_NOT_BLANK(1203, HttpStatus.BAD_REQUEST, "Current password must not be blank"),
    PASSWORD_SAME_AS_CURRENT(1204, HttpStatus.BAD_REQUEST, "New password must be different from the current password"),
    NEW_PHONE_NUMBER_NOT_BLANK(1205, HttpStatus.BAD_REQUEST, "New phone number must not be blank"),
    NEW_EMAIL_NOT_BLANK(1206, HttpStatus.BAD_REQUEST, "New email address must not be blank"),
    NEW_EMAIL_SAME_BEFORE(1207, HttpStatus.BAD_REQUEST, "The new email address is the same as the current one, please use a different email address"),
    NEW_PHONE_NUMBER_SAME_BEFORE(1208, HttpStatus.BAD_REQUEST, "The new phone number is the same as the current one, please use a different phone number"),
    USER_SAME_IS_VERIFY(1209, HttpStatus.BAD_REQUEST, "The user is already verified"),

    // Module role errors
    ROLE_NAME_NOT_FOUND(1301, HttpStatus.NOT_FOUND, "Role name not found in the system"),
    ROLE_NAME_NOT_BLANK(1302, HttpStatus.BAD_REQUEST, "Role name must not be blank"),
    ROLE_NAME_EXISTED(1303, HttpStatus.CONFLICT, "Role name already exists in the system, please use a different name"),
    ROLE_NOT_FOUND(1304, HttpStatus.NOT_FOUND, "Role not found"),
    DUPLICATE_ROLE_IDS(1305, HttpStatus.BAD_REQUEST, "The list of roles contains duplicate IDs, please check again"),
    PERMISSION_ALREADY_EXISTS_IN_ROLE(1306, HttpStatus.BAD_REQUEST, "The current role already has these permissions"),
    ROLE_ALREADY_ASSIGNED(1307, HttpStatus.BAD_REQUEST, "The user already has this role and cannot be assigned it again"),
    ROLE_SAME_IS_ACTIVE(1308, HttpStatus.BAD_REQUEST, "The current role is already active"),

    // Module permission errors
    PERMISSION_NAME_NOT_BLANK(1401, HttpStatus.BAD_REQUEST, "Permission name must not be blank"),
    PERMISSION_API_PATH_NOT_BLANK(1402, HttpStatus.BAD_REQUEST, "API path must not be blank"),
    PERMISSION_MODULE_NOT_BLANK(1403, HttpStatus.BAD_REQUEST, "Module name must not be blank"),
    PERMISSION_HTTP_METHOD_NOT_BLANK(1404, HttpStatus.BAD_REQUEST, "HTTP method must not be blank"),
    PERMISSION_NAME_EXISTED(1405, HttpStatus.CONFLICT, "Permission name already exists in the system, please use a different name"),
    PERMISSION_PATH_AND_METHOD_EXISTED(1406, HttpStatus.CONFLICT, "The API path and method already exist in the system"),
    PERMISSION_NOT_FOUND(1407, HttpStatus.NOT_FOUND, "Permission not found"),
    DUPLICATE_PERMISSION_IDS(1408, HttpStatus.BAD_REQUEST, "The list of permissions contains duplicate IDs, please check again"),
    PERMISSION_IDS_NOT_BLANK(1409, HttpStatus.BAD_REQUEST, "The list of permission IDs must not be blank, please add some"),
    PERMISSION_NOT_IN_ROLE(1410, HttpStatus.BAD_REQUEST, "Permission does not exist in the current role"),
    PERMISSION_MODULE_NOT_FOUND(1411, HttpStatus.NOT_FOUND, "Module not found"),
    PERMISSION_ALREADY_IN_ANOTHER_MODULE(1412, HttpStatus.BAD_REQUEST, "Permission already belongs to another module"),
    PERMISSION_MODULE_NAME_EXISTED(1413, HttpStatus.CONFLICT, "Module name already exists in the system, please use a different name"),
    PERMISSION_MODULE_NAME_INVALID(1414, HttpStatus.BAD_REQUEST, "Invalid module name, please use a different name"),

    // Module upload files errors
    FILE_NOT_FOUND(1501, HttpStatus.NOT_FOUND, "File not found"),
    FILE_UPLOAD_FAILED(1502, HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed, please try again"),
    FILE_TYPE_NOT_ALLOWED(1503, HttpStatus.BAD_REQUEST, "File type is not allowed"),
    FILE_TOO_LARGE(1504, HttpStatus.BAD_REQUEST, "File size exceeds the allowed limit"),
    FILE_TOO_SMALL(1505, HttpStatus.BAD_REQUEST, "File size is too small and does not meet requirements"),
    INVALID_FILE_NAME(1506, HttpStatus.BAD_REQUEST, "Invalid file name, please check again"),
    FILE_DELETE_FAILED(1507, HttpStatus.INTERNAL_SERVER_ERROR, "File deletion failed, please try again"),
    FILE_MOVE_FAILED(1508, HttpStatus.INTERNAL_SERVER_ERROR, "File move failed, please try again"),
    MULTIPLE_FILE_UPLOAD_PARTIALLY_FAILED(1509, HttpStatus.PARTIAL_CONTENT, "One or more file uploads failed, please check again"),
    FILE_ALREADY_EXISTS(1510, HttpStatus.CONFLICT, "File already exists in the system, please use a different name or rename it"),
    FOLDER_NOT_FOUND(1511, HttpStatus.NOT_FOUND, "Folder not found"),
    FOLDER_CREATION_FAILED(1512, HttpStatus.INTERNAL_SERVER_ERROR, "Folder creation failed, please try again"),
    FOLDER_DELETE_FAILED(1513, HttpStatus.INTERNAL_SERVER_ERROR, "Folder deletion failed, please try again"),
    FILE_NOT_BLANK(1514, HttpStatus.NOT_FOUND, "File must not be blank"),
    INVALID_FILE_PATH(1515, HttpStatus.BAD_REQUEST, "Invalid file path, correct syntax: folderName/fileName.ext"),
    EMPTY_SOURCE_KEY(1516, HttpStatus.BAD_REQUEST, "Source file path (sourceKey) must not be blank"),
    EMPTY_SOURCE_LIST(1517, HttpStatus.BAD_REQUEST, "Source file list (sourceKeys) must not be blank"),
    DESTINATION_FOLDER_EMPTY(1518, HttpStatus.BAD_REQUEST, "Destination folder name (destinationFolder) must not be blank"),

    // Module collection errors
    COLLECTION_TITLE_NOT_BLANK(1601, HttpStatus.BAD_REQUEST, "Collection title must not be blank"),
    COLLECTION_NOT_FOUND(1602, HttpStatus.NOT_FOUND, "Collection not found"),
    ORDER_ACTIVITY_IDS_NOT_EMPTY(1603, HttpStatus.NOT_FOUND, "Activity order list cannot be empty, please provide at least one activity ID"),
    DUPLICATE_ACTIVITY_ID(1604, HttpStatus.BAD_REQUEST, "Duplicate activity IDs found in request"),
    MISSING_ACTIVITY_ID(1605, HttpStatus.BAD_REQUEST, "Some activity IDs from the collection are missing in the request"),
    ACTIVITY_NOT_IN_COLLECTION(1606, HttpStatus.BAD_REQUEST, "Activity does not belong to the specified collection"),

    // Module activity errors
    COLLECTION_ID_REQUIRED(1701, HttpStatus.BAD_REQUEST, "Collection ID is required"),
    ACTIVITY_TYPE_ID_REQUIRED(1702, HttpStatus.BAD_REQUEST, "Activity type ID is required"),
    ACTIVITY_ID_REQUIRED(1703, HttpStatus.BAD_REQUEST, "Activity ID is required"),
    INVALID_ACTIVITY_TYPE(1704, HttpStatus.BAD_REQUEST, "Invalid activity type. Only the following types are supported: QUIZ_BUTTONS, QUIZ_CHECKBOXES, QUIZ_TRUE_OR_FALSE, QUIZ_TYPE_ANSWER, QUIZ_REORDER, INFO_SLIDE"),
    INVALID_POINT_TYPE(1705, HttpStatus.BAD_REQUEST, "Invalid point type. Only the following types are supported: NO_POINTS, STANDARD, DOUBLE_POINTS"),
    ACTIVITY_TYPE_NOT_BLANK(1706, HttpStatus.BAD_REQUEST, "Activity type must not be blank"),
    ACTIVITY_NOT_FOUND(1707, HttpStatus.NOT_FOUND, "Activity not found"),
    INVALID_SLIDE_ELEMENT_TYPE(1708, HttpStatus.BAD_REQUEST, "Invalid slide element type. Only the following types are supported: TEXT, IMAGE"),

    ;

    int code;
    HttpStatusCode statusCode;
    String message;
}
