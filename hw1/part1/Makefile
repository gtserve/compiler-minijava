#
#      To compile type: "make all" or "make compile"
#     You can also execute it by typing "make execute"
# To remove object and executable files type: "make clean"
#         George Tservenis (sdi1500164@di.uoa.gr)
#

JFL  = -g
JC   = javac
JVM  = java
MAIN = CalculatorParser

all: compile

compile:
	$(JC) $(JFL) $(MAIN).java

execute:
	@ $(JVM) $(MAIN)

clean:
	rm -f *.class
