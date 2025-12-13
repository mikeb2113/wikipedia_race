package wiki;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class testgson {
    public static void main(String[] args) {
        String json = "{\"name\":\"test\"}";
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        System.out.println("✅ Gson works! Name: " + obj.get("name").getAsString());
    }
}