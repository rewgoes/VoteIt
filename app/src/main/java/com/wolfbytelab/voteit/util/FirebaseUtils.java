package com.wolfbytelab.voteit.util;

public class FirebaseUtils {

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
