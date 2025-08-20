package com.example.chat.util;

import java.util.Scanner;

public class ConsoleUtils {
    private static final Scanner scanner = new Scanner(System.in);

    public static String readLine(String prompt) {
        System.out.print(prompt); // NÃO coloca ":" aqui
        return scanner.nextLine();
    }

    public static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Valor inválido. " + prompt);
            scanner.next();
        }
        int v = scanner.nextInt();
        scanner.nextLine(); // consome \n
        return v;
    }
}
