package samples.abro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import mjson.Json;

public class Main {

    public static ABRO model = new ABRO();

    private static long _tickstart;
private static long _ticktime;

    
    public static BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));
            
    private static void receiveVariables() {
        try {
            String line = stdInReader.readLine();
            Json json = Json.read(line);
            
            // Receive A
            if (json.has("A")) {
                model.A = json.at("A").asBoolean();
            }
            // Receive B
            if (json.has("B")) {
                model.B = json.at("B").asBoolean();
            }
            // Receive R
            if (json.has("R")) {
                model.R = json.at("R").asBoolean();
            }
            // Receive O
            if (json.has("O")) {
                model.O = json.at("O").asBoolean();
            }
            // Receive #ticktime
            if (json.has("#ticktime")) {
                _ticktime = json.at("#ticktime").asLong();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Json.MalformedJsonException e) {
           // Ignore other input
        }
    }
    
    private static void sendVariables() {
        Json json = Json.object();
        
        // Send A
        json.set("A", model.A);
        // Send B
        json.set("B", model.B);
        // Send R
        json.set("R", model.R);
        // Send O
        json.set("O", model.O);
        // Send #ticktime
        json.set("#ticktime", _ticktime);
        
        System.out.println(json.toString());
    }
    
    public static void main(String[] args) {
        
        
        // Initialize 
        model.reset();
        
        sendVariables();

        
        while (true) {
            
        
           // Read inputs
           receiveVariables();

           
           _tickstart = System.nanoTime();

        
           // Reaction of model
           model.tick();
           
           _ticktime = System.nanoTime() - _tickstart;

           
           // Send outputs
           sendVariables();

           
           
        }
    }
}
