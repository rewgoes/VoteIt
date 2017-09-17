package com.wolfbytelab.voteit.util;

public class FirebaseUtils {

    public static final String SURVEYS_PER_USER_KEY = "surveys_per_user";
    public static final String SURVEYS_KEY = "surveys";
    public static final String INVITES_PER_USER_KEY = "invites_per_user";
    public static final String INVITES_KEY = "invites";
    public static final String MEMBERS_KEY = "members";
    public static final String ENCODED_EMAIL_KEY = "encoded_email";
    public static final String EMAIL_KEY = "email";
    public static final String NAME_KEY = "name";
    public static final String USERS_KEY = "users";

    //Firebase Database paths must not contain '.', '#', '$', '[', or ']'
    public static String encodeAsFirebaseKey(String string) {
        return string.replaceAll("%", "%25")
                .replaceAll("\\.", "%2E")
                .replaceAll("#", "%23")
                .replaceAll("\\$", "%24")
                .replaceAll("/", "%2F")
                .replaceAll("\\[", "%5B")
                .replaceAll("\\]", "%5D");
    }

}
