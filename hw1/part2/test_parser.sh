#!/bin/bash

# test_parser.sh
# -----------------------------------------------------------------------------
# Compilers - Homework 1, Part 2
# name:    George Tservenis
# email:   sdi1500164@di.uoa.gr
# date:    17-04-2021
# -----------------------------------------------------------------------------

ERR_NUM_ARGS=1
ERR_FILE_IO=2

# ------------------------------ Functions ------------------------------------
print_usage() {
    # Print the proper usage of the script.
    echo "Script usage:"
    echo "  bash ./test_parser.sh [parserFile] [inputDir] [outputDir]"
}
# -----------------------------------------------------------------------------

# Check the number of arguments.
if [[ "$#" -ne 3 ]]; then
    echo "Error: Wrong number of arguments."
    print_usage
    exit "$ERR_NUM_ARGS"
fi

# Check that parser file and directories exist and are readable.
if [[ ! -e "$1" ]] || [[ ! -f "$1" ]]; then
    echo "Error: Parser file '$1' does not exits."
    exit "$ERR_FILE_IO"
fi
if [[ ! -d "$2" ]]; then
    echo "Error: Input directory '$2' does not exist."
    exit "$ERR_FILE_IO"
fi

parser_file="$1"
input_dir="$2"

if [[ ! -d "$3" ]];
    then
        mkdir output
        output_dir="./output"
    else
        rm -rf "$3"
        mkdir "$3"
        output_dir="$3"
fi


# make clean
# make compile

# Print a useful message.
echo "------------------ Parser Test -------------------"

for file_path in "$input_dir"/*; do

    filename=$(basename -- "$file_path")
    out_file="$output_dir/${filename%.*}"_out.txt

    echo "[ERROR]" >> "$out_file"
    make execute < "$file_path" > Main.java 2>> "$out_file"

    if [[ "$?" -eq 0 ]]; then
        > "$out_file"
        echo "[OUTPUT]" >> "$out_file"
        cat Main.java >> "$out_file"

        echo "" >> "$out_file"
        echo "[RESULT]" >> "$out_file"
        java Main.java &>> "$out_file"
    fi

    rm -f Main.java

    echo "Parsed file: $(basename "$file_path")"

done

echo ""
echo "Output saved in '$output_dir'"

# make clean
