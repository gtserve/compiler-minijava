compile:
	@ echo "-------------------------- JFlex - Scanner Generation --------------------------"
	@ jflex scanner.flex
	@ echo "--------------------------------------------------------------------------------"
	@ echo ""
	@ java -jar ../java-cup-11b.jar -interface -parser Parser parser.cup
	@ echo ""
	@ javac -cp ../java-cup-11b-runtime.jar *.java
	@ echo ""

execute:
	@ java -cp ../java-cup-11b-runtime.jar:. Driver

clean:
	@ rm -f *.class Main.java Scanner.java Parser.java sym.java
