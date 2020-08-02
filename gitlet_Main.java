package gitlet;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {

    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    //        if (args == null) {
//            System.out.println("Please enter a command.");
//            return;
//        }
    public static void main(String... args) {
        String firstParam;
        try {
            firstParam = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Please enter a command.");
            return;
        }
        String secondParam = null; String thirdParam = null; String fourthParam = null;
        if (args.length > 1) {
            secondParam = args[1];
        }
        if (args.length > 2) {
            thirdParam = args[2];
        }
        if (args.length > 3) {
            fourthParam = args[3];
        }
        Gitlet self = new Gitlet();
        if (firstParam.equals("init")) {
            self.init();
        } else if (!self.workingdirectory.exists()) {
            System.out.println("Not in an initialized gitlet directory.");
        } else if (firstParam.equals("status")) {
            self.status();
        } else if (firstParam.equals("log")) {
            self.log();
        } else if (firstParam.equals("global-log")) {
            self.globalLog();
        } else if (secondParam == null) {
            System.out.println("Incorrect operands.");
        } else if (firstParam.equals("add")) {
            self.add(secondParam);
        } else if (firstParam.equals("commit")) {
            if (secondParam.equals("") || secondParam.isEmpty()) {
                System.out.println("Please enter a commit message.");
            } else if (self.stagingArea.added.isEmpty() && self.stagingArea.deleted.isEmpty()) {
                System.out.println("No changes added to the commit.");
            } else {
                self.commit(secondParam);
            }
        } else if (firstParam.equals("merge")) {
            if (secondParam.equals("") || secondParam.isEmpty()) {
                System.out.println("Please enter a branch.");
            } else if (!self.stagingArea.added.isEmpty()) {
                System.out.println("You have uncommitted changes.");
            } else {
                self.merge(secondParam);
            }
        } else if (firstParam.equals("rm")) {
            self.rm(secondParam);
        } else if (firstParam.equals("find")) {
            self.find(secondParam);
        } else if (firstParam.equals("branch")) {
            self.branch(secondParam);

        } else if (firstParam.equals("rm-branch")) {
            self.rmBranch(secondParam);
        } else if (firstParam.equals("reset")) {
            self.reset(secondParam);
        } else if (firstParam.equals("checkout") && (secondParam.equals("--"))) {
            if (thirdParam == null) {
                System.out.println("Incorrect operands.");
            }
            self.checkout(secondParam, thirdParam);
        } else if (firstParam.equals("checkout") && !(secondParam == null)
                && !(thirdParam == null)) {
            if (fourthParam.equals(null) || !thirdParam.equals("--")) {
                System.out.println("Incorrect operands.");
            }
            self.checkout(secondParam, thirdParam, fourthParam);
        } else if (!(firstParam == null) && !(secondParam == null)) {
            self.checkout(secondParam);
        } else {
            System.out.println("No command with that name exists.");
        }
    }

}
