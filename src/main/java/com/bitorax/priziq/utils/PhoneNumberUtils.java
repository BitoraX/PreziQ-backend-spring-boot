package com.bitorax.priziq.utils;

import com.bitorax.priziq.exception.AppException;
import com.bitorax.priziq.exception.ErrorCode;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhoneNumberUtils {

    public String formatPhoneNumberToE164(String phoneNumber, String region) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(phoneNumber, region);

            if (!phoneUtil.isValidNumber(parsedNumber)) {
                throw new AppException(ErrorCode.INVALID_PHONE_NUMBER);
            }

            return phoneUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new AppException(ErrorCode.PHONE_NUMBER_FORMAT_ERROR);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.PHONE_NUMBER_NOT_SUPPORTED);
        }
    }

}
