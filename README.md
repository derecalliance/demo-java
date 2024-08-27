## Prerequisites
1. Clone and build the [DeRec cryptography library repository](https://github.com/derecalliance/cryptography)

## UI setup in IntelliJ
1. Clone the repo
2. Open the project in IntelliJ
3. Add the native cryptography library (ex: the `.dylib` file) to this project's file structure. Make sure to modify the cryptography library under Modules > Dependencies. Instructions to do this can be found [here](https://www.jetbrains.com/help/idea/library.html#add_classes_to_libraries).
4. In the root directory `derecui`, run `mvn clean compile package install`
5. In the top right corner of the screen, there should be a button that says "Current File" with a dropdown menu. On the right of that button, there is a play button (looks like a triangle). Click on "Current File" > "Edit Configurations" > "Add New Configuration" > "Application"
6. In the window that pops up, do the following:
   1. There should be a text field that says "Main class". In that text field, start typing "MainApp". It should
      select `MainApp` from `org.derecalliance.derec.demo`.
   2. In the text field titled "Environment variables", enter `username=<name>`.
   3. Title the run configuration as the same name that you selected as the username.
   4. Repeat the above 3 steps for usernames Alice, Bob, Carol, Dave. In the end, you should have 4 run
      configurations created.
7. To start the UI application, select each name from the dropdown of run configurations and click the Play button
   (looks like a triangle).

## Formatting Files
1. Run `mvn validate` in the root directory to format all files if changes are made.
