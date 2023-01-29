package avl_tree_tests;

import org.junit.jupiter.api.*;
import org.shulgin.tree.IMemTable;
import org.shulgin.tree.MemTable;

import java.io.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemTableTest {
    String putPath = "src/test/resources/put_test_files";
    String removePath = "src/test/resources/remove_test_files";
    static IMemTable<Integer, String> memTable;

    @BeforeAll
    public static void setUp() {
        memTable = new MemTable<>();
    }

    @Order(1)
    @Test
    public void putTest() throws Exception{
        File inputFile = new File(putPath + "/input.txt");
        File outputFile = new File(putPath + "/output.txt");
        File resultFile = new File(putPath + "/result.txt");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        int n = Integer.parseInt(reader.readLine());

        while(n-- > 0) {
            String[] line = reader.readLine().split(" ");
            int key = Integer.parseInt(line[0]);
            String value = line[1];
            memTable.put(key, value);
        }

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(resultFile)));
        memTable.printTree(writer);
        reader.close();
        writer.close();
        Assertions.assertTrue(compareFiles(outputFile, resultFile), "Деревья не равны!");
    }

    @Order(2)
    @Test
    public void removeTest() throws Exception{
        File inputFile = new File(removePath + "/input.txt");
        File outputFile = new File(removePath + "/output.txt");
        File resultFile = new File(removePath + "/result.txt");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        int n = Integer.parseInt(reader.readLine());

        while(n-- > 0) {
            int key = Integer.parseInt(reader.readLine());
            memTable.remove(key);
        }

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(resultFile)));
        memTable.printTree(writer);
        reader.close();
        writer.close();
        Assertions.assertTrue(compareFiles(outputFile, resultFile), "Деревья не равны!");
        System.out.println(memTable.size());
        System.out.println(memTable.hiddenSize());
    }

    private boolean compareFiles(File file1, File file2) throws Exception{
        FileInputStream fis1 = new FileInputStream(file1);
        FileInputStream fis2 = new FileInputStream(file2);
        int len1 = fis1.available();
        int len2 = fis2.available();

        if(len1 != len2) {
            return false;
        }

        while(len1-- > 0) {
            if(fis1.read() != fis2.read()) {
                return false;
            }
        }

        fis1.close();
        fis2.close();
        return true;
    }
}
