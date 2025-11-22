package com.tusistema.sistemaventas.util; 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ChartColors {

    private static final List<String> PREDEFINED_COLORS = Arrays.asList(
        "rgba(255, 99, 132, 0.7)",  // Rojo
        "rgba(54, 162, 235, 0.7)", // Azul
        "rgba(255, 206, 86, 0.7)", // Amarillo
        "rgba(75, 192, 192, 0.7)", // Verde azulado
        "rgba(153, 102, 255, 0.7)",// Púrpura
        "rgba(255, 159, 64, 0.7)", // Naranja
        "rgba(101, 186, 101, 0.7)",// Verde claro
        "rgba(231, 131, 174, 0.7)",// Rosa
        "rgba(131, 158, 231, 0.7)",// Azul claro
        "rgba(174, 231, 131, 0.7)" // Lima
    );

    private static final Random random = new Random();

    public static List<String> getColors(int count) {
        List<String> colors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (i < PREDEFINED_COLORS.size()) {
                colors.add(PREDEFINED_COLORS.get(i));
            } else {
                int r = random.nextInt(256);
                int g = random.nextInt(256);
                int b = random.nextInt(256);
                colors.add(String.format("rgba(%d, %d, %d, 0.7)", r, g, b));
            }
        }
        return colors;
    }

    public static String getColor(int index) {
        if (PREDEFINED_COLORS.isEmpty()) {
            return "rgba(0, 0, 0, 0.7)"; 
        }
        return PREDEFINED_COLORS.get(index % PREDEFINED_COLORS.size());
    }
}