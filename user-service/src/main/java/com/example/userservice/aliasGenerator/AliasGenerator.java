package com.example.userservice.aliasGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AliasGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AliasGenerator.class);
    private static List<String> words;

    static {
        try {
            // Cargar las palabras desde el archivo TXT en el classpath
            InputStream inputStream = AliasGenerator.class.getClassLoader().getResourceAsStream("words.txt");
            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el archivo words.txt en el classpath");
            }

            LOGGER.info("Archivo words.txt encontrado, comenzando a leer...");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                words = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    words.add(line.trim()); // Agrega la palabra a la lista eliminando espacios en blanco
                }
                LOGGER.info("Palabras cargadas correctamente: " + words);
            }
        } catch (IOException e) {
            LOGGER.error("Error leyendo el archivo de palabras", e);
            throw new RuntimeException("Error leyendo el archivo de palabras", e);
        }
    }

    public static String generateAlias() {
        Random random = new Random();
        StringBuilder alias = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                alias.append("."); // Agregar un punto entre palabras
            }
            String randomWord = words.get(random.nextInt(words.size())); // Selección aleatoria de una palabra
            alias.append(randomWord);
        }

        LOGGER.info("Alias generado: " + alias.toString());
        return alias.toString(); // Devolver el alias como una cadena de 3 palabras separadas por puntos
    }
}
