public class ModivSim {
    public static void main(String args[]) throws IOException
    {
        static int graph[][];
        static int via[][];
        static int rt[][];
        static int v;
        static int e;

    }


    public static void initialize(){
        for(int j = 0; j < v; j++)
        {
            if(i == j)
            {
                rt[i][j] = 0;
                via[i][j] = i;
            }
            else
            {
                rt[i][j] = 9999;
                via[i][j] = 100;
            }
        }
    }


    static void print_tables()
    {
        for(int i = 0; i < v; i++)
        {
            for(int j = 0; j < v; j++)
            {
                System.out.print("Dist: " + rt[i][j] + "    ");
            }
            System.out.println();
        }
    }






}





