<<<<<<<< HEAD:src/main/java/com/lockbox/controller/HelloController.java
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.controller;
========
package com.lockbox;
>>>>>>>> 535533973bd0930c1d5ead3e9838a6dadb1dd5c9:src/main/java/com/lockbox/HelloController.java

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}