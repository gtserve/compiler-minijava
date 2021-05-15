import syntaxtree.*;
import types.SemanticException;
import visitors.*;

import java.io.*;


public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Driver <inputFiles>");
            System.exit(1);
        }

        FileInputStream fis = null;
        for (int i = 0; i < args.length; i++) {
            try {
//                System.out.println("-------------------- File " + i
//                        + " --------------------");
                String fileName = args[i].substring(args[i].lastIndexOf("/") + 1);

                System.out.println("---------------- File #" + (i + 1) + ": " + fileName +
                        " ----------------");

                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);
                Goal root = parser.Goal();
                System.out.println("Program parsed successfully.");

                FirstPassVisitor collector = new FirstPassVisitor();
                root.accept(collector, null);
                System.out.println("SEM_CHECK: First pass OK!");

                SecondPassVisitor analyzer = new SecondPassVisitor(collector.getClasses());
                root.accept(analyzer, null);
                System.out.println("SEM_CHECK: Second pass OK!");

                /* Print all declarations. */
                //collector.printDeclarations();

                /* Print offsets for every class. */
                collector.printOffsets();

            } catch (ParseException | SemanticException ex) {
                System.out.println(ex.getMessage());
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            } finally {
                System.out.print("--------------------------"
                        + "----------------------\n\n");
                try {
                    if (fis != null) fis.close();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
            System.out.println();
        }
    }
}