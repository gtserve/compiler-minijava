# MiniJava Compiler

An educational compiler project written in Java for the University of Athens course **K31 – Compilers**. The repository follows the course's three assignments, progressing from parsing small languages to semantic analysis and LLVM IR generation for MiniJava.

## Project stages

### 1. Parsers and translators

- A hand-written LL(1) recursive-descent calculator supporting addition, subtraction, exponentiation, and parentheses
- A JFlex and Java CUP parser that translates a small string-processing language into Java

### 2. MiniJava semantic analysis

The second assignment parses MiniJava programs and performs static checks in two visitor passes. It builds a symbol table, validates declarations and types, handles inheritance and method overriding, and prints field and method offsets.

### 3. LLVM IR generation

The final assignment extends the semantic analyzer with a third visitor that emits LLVM IR (`.ll`). The checked-in generator handles class and method metadata, virtual-method tables, local allocation, and several arithmetic and logical expressions. Some visitors—including parts of statement, array, allocation, and method-call generation—remain marked as incomplete in the source.

This is coursework rather than a production compiler. The repository includes accepted and rejected MiniJava programs for exercising the parser and semantic checks; generated IR is not guaranteed to compile for every accepted program.

## Requirements

- JDK 11 or newer
- GNU Make
- JFlex, only for rebuilding Homework 1 Part 2

The required Java CUP, JTB, and JavaCC JAR files are included in the repository.

## Build and run

To build the final MiniJava-to-LLVM stage:

```bash
git clone https://github.com/gtserve/compiler-minijava.git
cd compiler-minijava
make -C hw3/src compile
```

Run the compiler with one or more MiniJava source files:

```bash
(cd hw3/src && java Main ../test/course/correct/Factorial.java)
```

For each successfully parsed and checked input, the program prints declarations and offsets and writes an LLVM IR file with the same base name in the current directory—for example, `Factorial.ll`.

The semantic-analysis-only stage is used in the same way:

```bash
make -C hw2/src compile
(cd hw2/src && java Main ../test/course/correct/Factorial.java)
```

The calculator and string-language translator have their own Makefiles:

```bash
# Recursive-descent calculator; reads expressions from standard input
(cd hw1/part1/src && \
  javac CalculatorParser.java ParseError.java && \
  echo '2**3+1' | java CalculatorParser)

# String-language-to-Java translator; reads a program from standard input
(cd hw1/part2 && \
  make compile && \
  make execute < test/course/example1.txt)
```

## Repository layout

```text
hw1/
├── part1/  Recursive-descent calculator
└── part2/  JFlex/Java CUP translator
hw2/       MiniJava parser and semantic analyzer
└── test/  Correct and erroneous MiniJava programs
hw3/       Semantic analyzer and LLVM IR generator
└── test/  Correct, erroneous, and sample LLVM files
```

## License

Licensed under the [MIT License](LICENSE).
