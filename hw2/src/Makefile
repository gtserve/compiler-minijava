#      To compile type: "make all" or "make compile"
#  To remove object and executable files type: "make clean"
#         George Tservenis (sdi1500164@di.uoa.gr)

JFL  = -g
JC   = javac
JVM  = java
MAIN = Main
OBJ  = *.class ./visitors/*.class ./types/*.class
JTB  = minijava-jtb.jj JavaCharStream.java MiniJavaParser.java MiniJavaParserConstans.java \
	   MiniJavaParserTokenManager.java ParseException.java Token.java TokenMgrError.java MiniJavaParserConstants.java

all: compile

compile:
	java -jar ../jtb132di.jar minijava.jj -te
	java -jar ../javacc5.jar  minijava-jtb.jj
	javac $(MAIN).java

clean:
	@ rm -f $(MAIN).class ./types/*.class ./visitors/*.class

reset:
	@ rm -f $(OBJ) $(JTB)
	@ rm -rf ./syntaxtree/ ./visitor/
