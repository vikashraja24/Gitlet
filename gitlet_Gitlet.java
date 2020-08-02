package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Gitlet implements Serializable {
    //Constructor
    /**
     * sets directory to directory
     **/

    //Fields
    HashMap<String, Commit> commits;
    HashMap<String, Branches> branches;
    String directory;
    Staging stagingArea;
    File workingdirectory;
    String headShaCommit;
    String headBranch;

    boolean hasMergeConflict = false;
    public Gitlet() {
        directory = System.getProperty("user.dir");
        workingdirectory = Utils.join(directory, ".gitlet");
        String dir = workingdirectory.getPath();
        try {
            commits = (HashMap<String, Commit>)
                    SerializableHelper.readObject(Utils.join(dir, "commits"));
        } catch (IllegalArgumentException e) {
            commits = new HashMap<>();
        }
        try {
            branches = (HashMap<String, Branches>)
                    SerializableHelper.readObject(Utils.join(dir, "branches"));
        } catch (IllegalArgumentException e) {
            branches = new HashMap<>();
        }
        try {
            stagingArea = (Staging) SerializableHelper.readObject(Utils.join(dir, "staging"));
        } catch (IllegalArgumentException e) {
            stagingArea = new Staging();
        }
        try {
            headShaCommit = (String) SerializableHelper.readObject(Utils.join(dir, "headcommit"));
        } catch (IllegalArgumentException e) {
            headShaCommit = "";
        }
        try {
            headBranch = (String) SerializableHelper.readObject(Utils.join(dir, "headbranch"));
        } catch (IllegalArgumentException e) {
            headBranch = "";
        }
    }

//functions

    /**
     * Initializes git
     */
    public void init() {
        if (workingdirectory.exists()) {
            System.out.println("A gitlet version-control system "
                    + "already exists in the current directory");
            return;
        }
        workingdirectory.mkdirs();
        Commit temp = new Commit("initial commit", new HashMap<>(), new HashMap<>(), null);
        temp.setSHAShortSha();
        commits.put(temp.shortSha, temp);
        headShaCommit = temp.shortSha;
        Branches head = new Branches("master", null);
        branches.put("master", head);
        headBranch = "master";
        serialize();
    }

    /**
     * creates a new commit object and adds it to commit hashmap
     */
    public void commit(String message) {
        Commit temp;
        Commit parent = commits.get(headShaCommit);
        if (parent.parentTracked.isEmpty()) {
            HashMap<String, Blob> t = new HashMap<>();
            t.putAll(stagingArea.added);
            temp = new Commit(message, t, stagingArea.added, headShaCommit);
        } else {
            HashMap<String, Blob> combined = new HashMap<>();
            combined.putAll(parent.parentTracked);
            combined.putAll(stagingArea.added);
            HashMap<String, Blob> t = new HashMap<>();
            t.putAll(combined);
            temp = new Commit(message, t, combined, headShaCommit);
        }
        temp.setSHAShortSha();

        headShaCommit = temp.shortSha;
        commits.put(headShaCommit, temp);
        branches.get(headBranch).shaLast = headShaCommit;
        stagingArea.wipe();
        serialize();
    }

    /**
     * Removes a blob to added
     **/

    void rm(String blobName) {
        Blob b = commits.get(headShaCommit).parentTracked.get(blobName);
        if (commits.get(headShaCommit).parentTracked.containsKey(blobName)) {
            stagingArea.deleted.put(blobName, b);
            stagingArea.added.remove(blobName);
            b.directory.delete();
            commits.get(headShaCommit).parentTracked.remove(blobName);
        } else if (stagingArea.added.containsKey(blobName)) {
            stagingArea.added.remove(blobName);
        } else {
            System.out.println("No reason to remove the file.");
        }
        serialize();
    }

    /**
     * adds a Blob to added
     **/
    void add(String filename) {
        Blob b = new Blob(filename, directory);
        HashMap<String, Blob> temp = stagingArea.added;
        if (!b.directory.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        b.setSHAShortSha();
        Commit c = commits.get(headShaCommit);
//        if (c == null) {
//            temp.put(filename, b);
//            serialize();
//            return;
//        }
        HashMap<String, Blob> headBlobby = c.blobby;
        if (headBlobby.containsKey(filename)
                && headBlobby.get(filename).SHA.equals(b.SHA)) {
            stagingArea.deleted.remove(filename);
            serialize();
            return;
        }
        if (stagingArea.deleted.containsKey(filename)) {
            stagingArea.deleted.remove(filename);
        }
        temp.put(filename, b);

        serialize();
    }


    /**
     * Prints out history of commit from head to master
     */
    public void log() {
        Commit pointer = commits.get(headShaCommit);
        while (pointer != null) {
            printlog(pointer);
            pointer = commits.get(pointer.parentSHA);
        }
    }

    public void globalLog() {
        //for loop printing commit id, date, and message for all commits
        for (Commit c : commits.values()) {
            printlog(c);
        }
    }

    public void printlog(Commit c) {
        System.out.println("===");
        System.out.println("Commit " + c.SHA);
        System.out.println(c.dateFormatted);
        System.out.println(c.logMessage);
        System.out.println();
    }

    public void find(String commitMessage) {
        //for loop to locate commit messages in commit array and print multiple commits when found
        boolean found = false;
        for (Commit c : commits.values()) {
            if (c.logMessage.compareTo(commitMessage) == 0) {
                System.out.println(c.SHA);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        /** marks current branch * */
        /**displays branches */
        //System.out.println(branches names);
        //repeat for staged files, removed files, modifications, and untracked files
        ArrayList<String> sortbranches = new ArrayList<>(branches.keySet());
        Collections.sort(sortbranches);
        System.out.println("=== Branches ===");
        for (String b : sortbranches) {
            if (headBranch.equals(b)) {
                System.out.println("*" + b);
            } else {
                System.out.println(b);
            }
        }
        System.out.println("\n=== Staged Files ===");
        for (String b : stagingArea.added.keySet()) {
            System.out.println(b);
        }
        System.out.println("\n=== Removed Files ===");

        for (String b : stagingArea.deleted.keySet()) {
            System.out.println(b);

        }
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===");
        System.out.println();
    }

    //Checkout Methods - 3 overloaded checkout methods:

    //testing
    public void checkout(String before, String filename) {

        /* 1 - input: filename, puts in working directory, overwrites original if exist,
        new version not staged */
        File existing = Utils.join(directory, filename);
        Commit headCommit = commits.get(headShaCommit);
        Blob blobVersion = headCommit.blobby.get(filename);

        //If file does not exist in previous commit, aborts, prints error message
        if (!existing.exists()) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        Utils.writeContents(existing, blobVersion.blobContents);

        serialize();
    }



    public void branch(String branchname) {
        if (branches.containsKey(branchname)) {
            System.out.println("A branch with that name already exists.");
        } else {
            branches.put(branchname, new Branches(branchname, commits.get(headShaCommit).shortSha));
        }
        serialize();
    }

    /**
     * removes a branch from the branch arraylist
     **/
    public void rmBranch(String branchname) {
        if (!branches.containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
        } else if (branchname.equals(branches.get(headBranch).name)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branches.remove(branchname);
        }
        serialize();
    }

    /**
     * checks out all the file to the given commit
     **/
    public void reset(String commitId) {
        String sSha = commitId.substring(0, 6);
        Commit thisOne = commits.get(sSha);
        if (thisOne == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        List<String> filesInDir = Utils.plainFilenamesIn(directory);
        Commit brCommit = thisOne;
        Commit brLast = commits.get(branches.get(headBranch).shaLast);
        if (!brCommit.parentTracked.isEmpty()) {
            for (String b : filesInDir) {
                Blob temp = new Blob(b, directory);
                temp.setSHAShortSha();
                Blob similar = brLast.blobby.get(b);
                if (similar == null) {
                    if (temp.directory.isFile()
                            && brCommit.blobby.get(b) != null
                            && !brCommit.blobby.get(b)
                            .shortSHA.equals(temp.shortSHA)) {
                        System.out.println("There is an untracked file in the way;"
                                + "delete it or add it first.");
                        return;
                    }
                }
                if (temp.directory.isFile()
                        && brCommit.blobby.get(b) != null
                        && !brCommit.blobby.get(b).shortSHA.equals(temp.shortSHA)
                        && !brLast.blobby.get(b).shortSHA.equals(temp.shortSHA)) {
                    System.out.println("There is an untracked file in the way;"
                            + "delete it or add it first.");
                    return;
                }
                if (!thisOne.parentTracked.containsKey(b)) {
                    temp.directory.delete();
                }
            }
        }
        for (String b : thisOne.parentTracked.keySet()) {
            checkout(commitId, "--", b);
        }
        headShaCommit = sSha;
        Branches head = branches.get(headBranch);
        head.shaLast = sSha;

        stagingArea.wipe();
        serialize();
    }



    /**
     * merge the current branch with the give branch
     **/

    public void merge(String branchname) {

        if (branches.get(branchname) != null) {
            if (branchname.equals("master")) {
                //current branch's last commit is the split point in the given branch
                //set given branch last to current branch last
                branches.get(headBranch).shaLast = branches.get(branchname).shaLast;
                System.out.println("Current branch fast-forwarded.");
            } else if (branches.get(branchname).shaSplit.equals(branches.get(headBranch).shaLast)) {
                branches.get(headBranch).shaLast = branches.get(branchname).shaLast;
                System.out.println("Current branch fast-forwarded.");
            } else if (branches.get(branchname).shaSplit
                    .equals(branches.get(branchname).shaLast)) {
                System.out.println("Given branch is an ancestor of the current branch.");
            } else {
                //actually merging

                Branches inputBranch = branches.get(branchname);
                Commit brLast = commits.get(branches.get(headBranch).shaLast);
                //head of branch you are checking out
                Commit brCommit = commits.get(inputBranch.shaLast);
                List<String> filesInDir = Utils.plainFilenamesIn(directory);
                if (!brCommit.parentTracked.isEmpty()) {
                    for (String b : filesInDir) {
                        Blob temp = new Blob(b, directory);
                        temp.setSHAShortSha();
                        Blob similar = brLast.blobby.get(b);
                        if (similar == null) {
                            if (temp.directory.isFile()
                                    && brCommit.blobby.get(b) != null
                                    && !brCommit.blobby.get(b).shortSHA.equals(temp.shortSHA)) {
                                System.out.println("There is an untracked file in the way;"
                                        + "delete it or add it first.");
                                return;
                            }
                        }
                        if (temp.directory.isFile()
                                && brCommit.blobby.get(b) != null
                                && !brCommit.blobby.get(b).shortSHA.equals(temp.shortSHA)
                                && !brLast.blobby.get(b).shortSHA.equals(temp.shortSHA)) {
                            System.out.println("There is an untracked file in the way;"
                                    + "delete it or add it first.");
                            return;
                        }
                    }
                }

                mergeHelper(commits.get(branches.get(headBranch).shaLast),
                        commits.get(branches.get(branchname).shaLast),
                        commits.get(branches.get(branchname).shaSplit),
                        branchname);
            }
        } else {
            System.out.println("A branch with that name does not exist.");
        }
        serialize();

    }

    public void writeMergeConflict(Blob currentBlob, Blob givenBlob) {
        byte[] head = "<<<<<<< HEAD\n".getBytes();
        byte[] divider = "=======\n".getBytes();
        byte[] bot = ">>>>>>>\n".getBytes();
        byte[] currentBytes = currentBlob.blobContents;
        byte[] givenBytes = givenBlob.blobContents;
        byte[] result1 = concatenateByteArray(head, currentBytes);
        byte[] result2 = concatenateByteArray(result1, divider);
        byte[] result3 = concatenateByteArray(result2, givenBytes);
        byte[] result4 = concatenateByteArray(result3, bot);
        Utils.writeContents(currentBlob.directory, result4);
        serialize();
    }

    public void writeMergeConflictWhenDeleted(Blob currentBlob) {
        byte[] head = "<<<<<<< HEAD\n".getBytes();
        byte[] divider = "=======\n".getBytes();
        byte[] bottom = ">>>>>>>\n".getBytes();
        byte[] currentBytes = currentBlob.blobContents;
        byte[] result1 = concatenateByteArray(head, currentBytes);
        byte[] result2 = concatenateByteArray(result1, divider);
        byte[] result3 = concatenateByteArray(result2, bottom);
        Utils.writeContents(currentBlob.directory, result3);
        serialize();
    }

    public byte[] concatenateByteArray(byte[] arr1, byte[] arr2) {
        byte[] result = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    private void serialize() {
        String dir = Utils.join(directory, ".gitlet").getPath();
        SerializableHelper.writeObject(branches, Utils.join(dir, "branches"));
        SerializableHelper.writeObject(commits, Utils.join(dir, "commits"));
        SerializableHelper.writeObject(stagingArea, Utils.join(dir, "staging"));
        SerializableHelper.writeObject(headShaCommit, Utils.join(dir, "headcommit"));
        SerializableHelper.writeObject(headBranch, Utils.join(dir, "headbranch"));
    }

}
