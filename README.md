## UI setup in IntelliJ
1. Clone the repo
2. Open the project in IntelliJ
3. In the root directory `derec-ui` , run `mvn clean compile package install`
4. On the top right corner of the screen, there should be a button that says "Current File" with a dropdown menu. On the right of that button, there is a play button (looks like a triangle). Click on "Current File" > "Edit Configurations" > "Add New Configuration" > "Application"
5. In the window that pops up, there should be a text field that says "Main class". In that text field, start typing "MainApp". It should select `MainApp` from `org.derecalliance.derec.demo`.
6. In the same window, there is a blue text button that says "Modify options". Click that button, and click the option that says "Allow multiple instances".
7. Name the configuration (I usually just name it MainApp)
8. To start the UI application, click the play button to open a new UI window.
