package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Commit implements Serializable {

    // Fields
    //For serialization
    static final long serialVersionUID = 0;
    /**
     * The SHA-1 identifier of my parent, or null if I am the initial commit.
     */
    String SHA;
    String parentSHA;
    String shortSha;
    /**
     * My log message.
     */
    String logMessage;
    /**
     * My timestamp. (java.util.Date)
     */
    String dateFormatted;
    /**
     * Array of Blobs
     */
    //String is the filename
    HashMap<String, Blob> parentTracked;
    HashMap<String, Blob> blobby;
//    File directory;

    public Commit(String message, HashMap<String, Blob> arr,
                  HashMap<String, Blob> tr, String pSha) {
        logMessage = message;
        blobby = arr;
        parentTracked = tr;
        parentSHA = pSha;

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd kk:mm:ss");
        dateFormatted = sdf.format(c.getTime());
    }

    // Methods

    /**
     * return Date
     **/
    public String getCommitDate() {
        return dateFormatted;
    }

    public void setSHAShortSha() {
        if (parentTracked == null) {
            String s = "";
            for (Blob b : blobby.values()) {
                s += b.toString();
            }
            SHA = Utils.sha1(logMessage + dateFormatted + s);
        } else {
            String s = "";
            for (Blob b : blobby.values()) {
                s += b.toString();
            }
            for (Blob b : parentTracked.values()) {
                s += b.toString();
            }
            SHA = Utils.sha1(logMessage + dateFormatted + s);
        }
        shortSha = SHA.substring(0, 6);
    }
}
