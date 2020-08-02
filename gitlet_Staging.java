package gitlet;

import java.io.Serializable;
import java.util.HashMap;

public class Staging implements Serializable {

    //Constructor

    //For serialization
    static final long serialVersionUID = 0;
    //Fields
    HashMap<String, Blob> deleted;
    HashMap<String, Blob> added;
    /**
     * creates empty ArrayList <Blob>, sets previous to default commit
     **/
    public Staging() {
        added = new HashMap<>();
        deleted = new HashMap<>();
    }
    /**
     * creates empty ArrayList <Blob>, sets previous to default commit
     **/

    //Functions

    /**
     * resets staging area,
     * makes the new commit created to the previous commit instance variable
     **/
    public void wipe() {
        deleted = new HashMap<>();
        added = new HashMap<>();
    }

}
