package com.example.userservice.generatorCVU;
import java.util.Random;
public class GeneratorCVU {
        public static String generateCVU() {
            Random random = new Random();
            StringBuilder cvu = new StringBuilder();
            for (int i = 0; i < 22; i++) {
                cvu.append(random.nextInt(10)); // Genera un dígito aleatorio entre 0 y 9
            }
            return cvu.toString();
        }


}
