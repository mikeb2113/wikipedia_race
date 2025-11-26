import java.io.IOException;

public class entry_point {

    public static void main(String[] args){
        docker_build();
        docker_run();
    }

    private static void docker_build(){
    try {
            String[] cmd = {"docker", "build", "-t", "docker-java-server", "."};

            Process proc = new ProcessBuilder(cmd)
                    .inheritIO() // optional: pipe output to console
                    .start();

            proc.waitFor();  // wait for docker to finish

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void docker_run(){
    try {
            String[] cmd = {"docker", "run", "--rm", "docker-java-server"};

            Process proc = new ProcessBuilder(cmd)
                    .inheritIO() // optional: pipe output to console
                    .start();

            proc.waitFor();  // wait for docker to finish

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
