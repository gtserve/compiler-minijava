all: compile execute

compile:
	jflex scanner.flex
	java -jar ../java-cup-11b.jar -interface -parser Parser parser.cup
	javac -cp ../java-cup-11b-runtime.jar *.java

execute:
	@ java -cp ../java-cup-11b-runtime.jar:. Driver

clean:
	rm -f *.class Main.java Scanner.java Parser.java sym.java
