import java.io.IOException;
import java.util.Scanner;
import javax.swing.*;
public class git_control_automated{
    private static final Scanner sc = new Scanner(System.in);
    public static void main(String[] args){
        String name = get_name();

        try{
            checkoutBranch(name);
            add();
            commit();
            push(name);
        } catch(Exception e){
            e.printStackTrace();
        }
        sc.close();
    }

    private static void checkoutBranch(String name) throws IOException, InterruptedException {
    // Try switch first (branch exists)
    Process switchProc = new ProcessBuilder("git", "switch", name)
            .inheritIO()
            .start();
    int exit = switchProc.waitFor();

    if (exit != 0) {
        // If switching failed, create the branch
        Process createProc = new ProcessBuilder("git", "switch", "-c", name)
                .inheritIO()
                .start();
        createProc.waitFor();
    }
    }
    private static void add(){
        try {
            String[] cmd = {"git", "add", "."};

            Process proc = new ProcessBuilder(cmd)
                    .inheritIO() // optional: pipe output to console
                    .start();

            proc.waitFor();  // wait for docker to finish

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void commit(){
        String message = sc.nextLine();
        while(message.isBlank()){
            if(message.isEmpty()){
                System.out.println("Please input any commit message: ");
            }
                message=sc.nextLine().trim();
        }
        try {
            String[] cmd = {"git", "commit", "-m", message};
            Process proc = new ProcessBuilder(cmd)
                    .inheritIO() // optional: pipe output to console
                    .start();

            proc.waitFor();  // wait for docker to finish

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void push(String name){
        try {
            String[] cmd = {"git", "push", "-u", "origin", name};

            Process proc = new ProcessBuilder(cmd)
                    .inheritIO() // optional: pipe output to console
                    .start();

            proc.waitFor();  // wait for docker to finish

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static String get_name(){
        System.out.println("Please select your name:\n1| Adeebah\n2| Darcie\n3| Marvin\n4| Michael\n5| Saksham");
        Scanner sc = new Scanner(System.in);
        int role = sc.nextInt();
        switch(role){
            case (int)1:
                return "adeebah";
            case (int)2:
                return "darcie";
            case (int)3:
                return "marvin";
            case (int)4:
                return "michael";
            case (int)5:
                return "saksham";
            default:
                return null;
        }
    }
}
