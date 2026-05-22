import java.io.*;
import java.util.*;

/**
 * Handles all file I/O: user registration, login, and score saving/loading.
 * Stores data in plain text files in the game's directory.
 */
public class FileManager {
    private static final String USERS_FILE  = "users.txt";
    private static final String SCORES_FILE = "scores.txt";

    // Register a new user. Returns false if username already taken.
    public static boolean registerUser(String username, String password) {
        if (username.isBlank() || password.isBlank()){
            return false;
        }
        if (userExists(username)){
            return false;
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, true));
            writer.println(username + ":" + password);
            writer.close();
            return true;
        } catch (IOException e) {
            System.out.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    // Check if login credentials match a saved user.
    public static boolean loginUser(String username, String password) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    reader.close();
                    return true;
                }
            }
            reader.close();
        } catch (IOException e) {
            // File doesn't exist yet - no users registered
        }
        return false;
    }

    // Check if a username already exists in the file.
    public static boolean userExists(String username) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 1 && parts[0].equals(username)) {
                    reader.close();
                    return true;
                }
            }
            reader.close();
        } catch (IOException e) {
            // No users file yet
        }
        return false;
    }

    // Save a score entry for a user.
    public static void saveScore(String username, int score, int level) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(SCORES_FILE, true));
            writer.println(username + ":" + score + ":" + level);
            writer.close();
        } catch (IOException e) {
            System.out.println("Error saving score: " + e.getMessage());
        }
    }

    // Get the best score for a specific user.
    public static int getBestScore(String username) {
        int best = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(SCORES_FILE));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 2 && parts[0].equals(username)) {
                    int score = Integer.parseInt(parts[1]);
                    if (score > best) {
                        best = score;
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            // No scores file yet
        }
        return best;
    }

    // Get top N scores across all players as a list of formatted strings.
    public static List<String> getTopScores(int n) {
        // Build a map of best score per user
        HashMap<String, Integer> bestPerUser = new HashMap<String, Integer>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(SCORES_FILE));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 2) {
                    String user = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    if (!bestPerUser.containsKey(user) || score > bestPerUser.get(user)) {
                        bestPerUser.put(user, score);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            // No scores yet
        }

        // Convert map to a list and sort descending by score
        List<String> users = new ArrayList<String>(bestPerUser.keySet());
        for (int i = 0; i < users.size() - 1; i++) {
            for (int j = i + 1; j < users.size(); j++) {
                if (bestPerUser.get(users.get(j)) > bestPerUser.get(users.get(i))) {
                    String temp = users.get(i);
                    users.set(i, users.get(j));
                    users.set(j, temp);
                }
            }
        }

        // Build result list
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < users.size() && i < n; i++) {
            String user = users.get(i);
            result.add((i + 1) + ". " + user + "  -  " + bestPerUser.get(user) + " pts");
        }
        return result;
    }
}