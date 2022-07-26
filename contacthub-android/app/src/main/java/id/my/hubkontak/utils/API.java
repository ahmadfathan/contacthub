package id.my.hubkontak.utils;

public class API {
    public static final String BASE_URL = "https://sandbox.kontakhub.my.id";
    public static final String BASE_URL_UPLOADS = BASE_URL +  "/uploads/";
    public static final String BASE_URL_ARTICLE =  BASE_URL + "/article/";
    public static final String AMQ_HOST = "amq.kontakhub.my.id";

    //    authentication
    public static final String API_LOGIN = BASE_URL + "/login";
    public static final String API_REGISTER = BASE_URL + "/register";
    public static final String API_REQ_FORGOT_PASSWORD = BASE_URL + "/forgot-password/request";
    public static final String API_VERIFY_FORGOT_PASSWORD = BASE_URL + "/forgot-password/verify";
    public static final String API_LOGOUT = BASE_URL + "/customer/logout";
    public static final String API_STARTUP = BASE_URL + "/startup";

    public static final String API_UPDATE_FIREBASE_TOKEN = BASE_URL + "/firebase";

    //    list greeting
    public static final String API_LIST_GREETING = BASE_URL + "/master/greeting";
    //    list interest
    public static final String API_LIST_INTEREST = BASE_URL + "/master/interest";
    //    list profession
    public static final String API_LIST_PROFESSION = BASE_URL + "/master/profession";
    //    list profession
    public static final String API_LIST_BUSINESS_TYPE = BASE_URL + "/master/business-type";

    //    list article
    public static final String API_LIST_ARTICLE = BASE_URL + "/article/customer";

    //    profile
    public static final String API_GET_PROFILE = BASE_URL + "/profile";
    public static final String API_UPDATE_PROFILE = BASE_URL + "/profile";
    public static final String API_UPDATE_PASSWORD = BASE_URL + "/profile/password";

    //    credit
    public static final String API_TOPUP_REQUEST = BASE_URL + "/credit/topup";
    public static final String API_BALANCE = BASE_URL + "/credit/balance";
    public static final String API_HISTORY_CREDIT = BASE_URL + "/credit";

    //    affiliate
    public static final String API_HISTORY_AFFILIATE = BASE_URL + "/affiliate";
    public static final String API_SUMMARY_AFFILIATE = BASE_URL + "/affiliate/summary";

    //    summary
    public static final String API_CONTACT_SUMMARY = BASE_URL + "/contact/summary";
    public static final String API_LIST_CONTACT_SHARE = BASE_URL + "/contact/share";
    public static final String API_LIST_CONTACT_SAVE = BASE_URL + "/contact/save";

    public static final String API_HISTORY_WITHDRAW = BASE_URL + "/withdraw";
    public static final String API_WITHDRAW_REQUEST = BASE_URL + "/withdraw/request";
    public static final String API_SUMMARY_WITHDRAW = BASE_URL + "/withdraw/summary";
    public static final String API_AMOUNT_WITHDRAW = BASE_URL + "/withdraw/amount";

    public static final String API_SINGKRON_LOCAL_CONTACT = BASE_URL + "/sync/contact";
    public static final String API_CONTACT_DETAIL = BASE_URL + "/contact/detail";

    public static final String API_UPLOAD_FOTO = BASE_URL + "/profile/upload/foto";
    public static final String API_UPLOAD_COVER = BASE_URL + "/profile/upload/cover";

    public static final String API_REMOVE_FOTO = BASE_URL + "/profile/remove/foto";
    public static final String API_REMOVE_COVER = BASE_URL + "/profile/remove/cover";

    public static final String API_LIST_PROVINCE = BASE_URL + "/master/province";
    public static final String API_LIST_CITY = BASE_URL + "/master/city";
    public static final String API_SAVE_CONTACT_MANUAL = BASE_URL + "/contact/save-manual";
    public static final String API_LIST_FEED = BASE_URL + "/feed-customer";
    public static final String API_LIST_FEED_PUBLIC = BASE_URL + "/feed/public/customer";
    public static final String API_CREATE_FEED = BASE_URL + "/feed-customer";
    public static final String API_UPDATE_FEED = BASE_URL + "/feed-customer";
    public static final String API_DELETE_FEED = BASE_URL + "/feed-customer";
    public static final String API_BANNER = BASE_URL + "/slider/customer";
    public static final String API_CLICK_FEED = BASE_URL + "/feed/click";


}
