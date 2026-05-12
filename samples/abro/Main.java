package samples.abro;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ABRO abro = new ABRO();
        
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("Enter command (A, B, R, O, tick, reset, exit):");
                String[] command = scanner.nextLine().trim().split(" ");
                var verb = command[0].toUpperCase();
                var arg = command.length > 1 ? command[1] : null;

                switch (verb) {
                    case "EXIT":
                        return;
                    case "TICK":
                    case "":
                        abro.tick();
                        System.out.format("Current state: A=%s, B=%s, R=%s, O=%s%n", abro.getVariable("A"), abro.getVariable("B"), abro.getVariable("R"), abro.getVariable("O"));
                        break;
                    case "RESET":
                        abro.reset();
                        break;
                    case "A":
                    case "B":
                    case "R":
                        boolean boolValue = Boolean.parseBoolean(arg);
                        abro.setVariable(verb, boolValue);
                        break;
                    case "O":
                        System.out.println("Output O: " + abro.getVariable("O"));
                        break;
                    default:
                        System.out.println("Unknown command.");
                }
            }
        }
    }
}
