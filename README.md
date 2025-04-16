# 🖥️ Java Command Line Interpreter
This project is a simple Java-based command-line shell that simulates Unix-like behavior. It allows users to interact with the file system and execute basic shell commands through a text-based interface. The interpreter supports built-in commands, redirection operators (>, >>), and piping (|).

🚀 Features
Basic shell-like environment with a prompt ($>)

Supports core Unix commands:

pwd: Show current working directory

mkdir <dir>: Create a new directory

rmdir <dir>: Remove an empty directory

touch <file>: Create a new file

rm <file>: Delete a file

mv <src> <dest>: Move or rename a file

cat <file>: Display file contents

help: Show supported commands

exit: Exit the shell

Output redirection:

>: Overwrite output to a file

>>: Append output to a file

Command piping (|): Pass output of one command as input to the next

Error handling with informative messages

Modular command processing structure
