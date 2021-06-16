import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import my_types.FileHandler;
import my_types.SymbolTable;
import syntaxtree.*;
import my_types.SemanticException;
import my_visitors.*;


public class Main {

    private static void printUsage() {
        System.err.println("Usage:\n  java Main [inputFile1] [inputFile2] ... [inputFileN]");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            FileHandler fh = null;
            FileInputStream fis = null;
            FileWriter fw = null;
            File llvmFile;

            try {
                fh = new FileHandler(i, args[i]);
                fh.printTitleSep();

                fis = new FileInputStream(args[i]);

                MiniJavaParser parser = new MiniJavaParser(fis);
                Goal root = parser.Goal();
                System.out.println("File parsed successfully.");

                FirstPassVisitor collector = new FirstPassVisitor();
                root.accept(collector, null);
                System.out.println("First pass successful.");

                SymbolTable classes = collector.getClasses();

                SecondPassVisitor analyzer = new SecondPassVisitor(classes);
                root.accept(analyzer, null);
                System.out.println("Second pass successful.");

                /* Print all declarations. */
                collector.printDeclarations();

                /* Print offsets for every class. */
                collector.printOffsets();

                /* Create LLVM IR file. */
                llvmFile = new File(fh.getFileName() + ".ll");
                if (llvmFile.exists())
                    llvmFile.delete();
                llvmFile.createNewFile();
                fw = new FileWriter(llvmFile);

                ThirdPassVisitor generator = new ThirdPassVisitor(classes, fw);
                root.accept(generator, null);
                System.out.println("IR generated successfully.");

            } catch (ParseException | SemanticException ex) {
                System.out.println(ex.getMessage());
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            } finally {
                try {
                    if (fis != null) fis.close();
                    if (fw != null) fw.close();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }

                if (fh != null) fh.printSep();
                System.out.println();
            }
        }
    }
}
