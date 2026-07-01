package com.lockbox;

<<<<<<< HEAD
import com.lockbox.model.User;
import com.lockbox.repository.UserRepository;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.lockbox.services.*;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

@SpringBootApplication
public class LockBoxApp extends Application {
    static CryptoService crypto = new CryptoService();
    
=======
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LockBoxApp extends Application {
>>>>>>> 535533973bd0930c1d5ead3e9838a6dadb1dd5c9

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
<<<<<<< HEAD
                getClass().getResource("/com/lockbox/fxml/login-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 420, 520);
=======
                getClass().getResource("/com/lockbox/login-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 420, 520);
        scene.getStylesheets().add(getClass().getResource("/com/lockbox/styles.css").toExternalForm());
>>>>>>> 535533973bd0930c1d5ead3e9838a6dadb1dd5c9
        stage.setTitle("LockBox");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

<<<<<<< HEAD
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        
        
        User user = new User();
        user.setEmail("mama@gmail.com");
        SecretKey key = crypto.generateKey("Password123",user.getSalt() );
        System.out.println(key);
       
        String hash = crypto.encrypt("MamaMia", key);
        System.out.println(hash);
        String unhash = crypto.decrypt(hash, key);
        System.out.println(unhash);
        
        
        launch();
        
        
=======
    public static void main(String[] args) {
        launch();
>>>>>>> 535533973bd0930c1d5ead3e9838a6dadb1dd5c9
    }
}