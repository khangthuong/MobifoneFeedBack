package com.example.khangnt.mobifonefeedback.helper;

public class AppConfig {
    public static String URL_REGISTER = "http://192.168.1.48/mobifoneAPI/register.php";
    public static String URL_LOGIN = "http://192.168.1.48/mobifoneAPI/login.php";
    public static String URL_READ_FEEDBACK_BY_USER = "http://192.168.1.48/mobifoneAPI/getfeedbackbyuser.php";
    public static String URL_SUBMIT_NEW_FEEDBACK = "http://192.168.1.48/mobifoneAPI/storenewfb.php";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;
    public static final String SHARED_PREF = "MBFFeedback";
}
