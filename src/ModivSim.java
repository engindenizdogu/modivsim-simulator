import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ModivSim extends Thread {
    static int table[][]=new int[5][5];
    public static void main(String args[]) throws IOException {
        System.out.println("ModivSim started...");
        readFile();
        initialize();
        print();

    }

    // TODO: Read files and initialize nodes here
    public static void readFile() throws IOException {
        BufferedReader br0 = new BufferedReader(new FileReader("nodes/node0.txt"));
        BufferedReader br1 = new BufferedReader(new FileReader("nodes/node1.txt"));
        BufferedReader br2 = new BufferedReader(new FileReader("nodes/node2.txt"));
        BufferedReader br3 = new BufferedReader(new FileReader("nodes/node3.txt"));
        BufferedReader br4 = new BufferedReader(new FileReader("nodes/node4.txt"));
        String st;
        while ((st = br0.readLine()) != null) {
            System.out.println(st);
        }
    }



    public static void initialize() {

        for (int x = 0; x < 5; x++) {
            for (int z = 1; z < 5; z++) {
                table[x][z] = 999;
            }
        }
    }
    public static void print() {
        System.out.println("Distance Table:");
        System.out.println("dst   |   0        1        2        3");
        System.out.printf("---------------------------------------\n");
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                System.out.print(table[x][z] + "      ");
            }
            System.out.println();
        }

    }

}

