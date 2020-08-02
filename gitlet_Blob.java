package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    //For serialization
    static final long serialVersionUID = 0;
    static boolean hasMergeConflict = false;

    //Instance variables
    String SHA;
    String shortSHA;
    String fileName;
    File directory;
    byte[] blobContents;

    //Constructor
    public Blob(String nameOfFile, String directoryLocation) {
        /* initializes 3 main components of a blob:
        SHA Id, version number, and the reference to the physical file*/
        //Fields
        /** SHA id **/
        fileName = nameOfFile;
        directory = Utils.join(directoryLocation, nameOfFile);
        if (!directory.exists()) {
            blobContents = null;
        } else {
            blobContents = Utils.readContents(directory);
        }
    }

    //this constructor is only for copying use
    public Blob(Blob given) {
        this.fileName = given.fileName;
        this.directory = given.directory;
    }

    //Functions
    /* returns a boolean True if b1 and b2 are the same blob, else returns false */
    boolean compare2Blobs(Blob b1, Blob b2) {
        //same blob means same file contents
        return b1.shortSHA.equals(b2.shortSHA);
    }

    boolean compareContents(Blob b1) {
        //return whether the this current blob is the same as input blobs
        return this.shortSHA.equals(b1.shortSHA);
    }

    @Override
    public boolean equals(Object o) {
        Blob obj = (Blob) o;
        return (shortSHA.equals(obj.shortSHA));
    }


    @Override
    public String toString() {
        return "Blob{" + "SHA='" + SHA + '\''
                + ", shortSHA='" + shortSHA + '\''
                + ", fileName='" + fileName + '\''
                + ", directory=" + directory + '}';
    }

    public void setSHAShortSha() {

        //the full-length SHA for in logs
        //in case no other object exists with a
        // SHA-1 identifier that starts with the same six digits.
        /*actual filename, input for future checkouts*/
//        String s = Utils.readContents(directory).toString();
//        System.out.println(s);
        String bytes = new String(Utils.readContents(directory));
        SHA = Utils.sha1(bytes + fileName);
        shortSHA = SHA.substring(0, 6);
    }
}
