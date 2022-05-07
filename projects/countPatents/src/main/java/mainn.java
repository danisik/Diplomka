import java.io.File;

public class mainn {

    public static void main(String[] args) {

        File folder = new File("D:\\PATENTY\\2.Data");

        File[] patentFolders = folder.listFiles();

        for (File patentFolder : patentFolders) {

            long count = 0;

            File[] subdirs = patentFolder.listFiles();

            for (File subdir : subdirs) {

                count += countPatents(subdir);
            }

            System.out.println(patentFolder.getName() + ": " + count);
        }
    }

    public static long countPatents(File folder) {

        long count = 0;

        if (folder.isFile()){

            String extension = folder.getName().substring(folder.getName().lastIndexOf('.') + 1);
            if (extension.equalsIgnoreCase("xml") ||
                    extension.equalsIgnoreCase("csv") ||
                    extension.equalsIgnoreCase("sql") ||
                    extension.equalsIgnoreCase("xlsx"))
                return 1;

            else return count;
        }
        else {

            File[] files = folder.listFiles();

            for (File file : files) {

                count += countPatents(file);
            }
        }

        return count;
    }
}
