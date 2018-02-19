# Edge computing solver :

### Summary

Given a property file with the descriptions of :
  - The edge network on which to deploy
  - The services to deploy

Find the right edge site on which to place each component of each service.


### Install

- Create a repository in which to clone the project and move in it:  
        ```mkdir edge_computing && cd edge_computing```
- Clone this repository:  
        ```git clone https://github.com/fmilhe14/projet_option.git```
- Move to java directory:  
        ```cd java```
- Build (install dependencies):  
        ```mvn clean install```

### Run

#### Properties file

This repository comes with an example properties files, at ```java/ressources/edge.properties```.  
Follow the same conventions while writing your own ```.properties``` file.  

### Parameters

If you wrote your own ```.properties``` file, please change the argument in line 16 of ```java/src/Main.java``` accordingly
(you can also change the name of the Solver if you like).

### Run the project

You can only the project from inside an IDE, by running the main method in ```java/src/Main.java```

