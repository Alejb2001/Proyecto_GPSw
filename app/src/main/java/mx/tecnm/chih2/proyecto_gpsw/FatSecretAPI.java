package mx.tecnm.chih2.proyecto_gpsw;

import android.util.Log;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FatSecretAPI {
    private static final String TAG = "FatSecretAPI";
    private static final String API_URL = "http://platform.fatsecret.com/rest/server.api";
    private static final String API_KEY = BuildConfig.getFatSecretApiKey();
    private static final String API_SECRET = BuildConfig.getFatSecretApiSecret();
    private static final OAuth10aService service = new ServiceBuilder(API_KEY)
            .apiSecret(API_SECRET)
            .build(FatSecretApiImpl.instance());

    public static List<FatSecretFood> searchFood(String query) {
        List<FatSecretFood> results = new ArrayList<>();
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = API_URL + "?method=foods.search&format=json&search_expression=" + encodedQuery;
            
            OAuthRequest request = new OAuthRequest(Verb.GET, url);
            service.signRequest(new OAuth1AccessToken("", ""), request);
            
            try (Response response = service.execute(request)) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error en la respuesta: " + response.getCode());
                }

                String responseBody = response.getBody();
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject foods = jsonResponse.getJSONObject("foods");
                JSONArray foodArray = foods.getJSONArray("food");

                for (int i = 0; i < foodArray.length(); i++) {
                    JSONObject food = foodArray.getJSONObject(i);
                    String foodId = food.getString("food_id");
                    String foodName = food.getString("food_name");
                    String brandName = food.optString("brand_name", null);
                    
                    FatSecretFood foodInfo = getFoodInfo(foodId);
                    if (foodInfo != null) {
                        results.add(foodInfo);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching food: " + e.getMessage());
        }
        return results;
    }

    public static FatSecretFood getFoodInfo(String foodId) {
        try {
            String url = API_URL + "?method=food.get&format=json&food_id=" + foodId;
            
            OAuthRequest request = new OAuthRequest(Verb.GET, url);
            service.signRequest(new OAuth1AccessToken("", ""), request);
            
            try (Response response = service.execute(request)) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error en la respuesta: " + response.getCode());
                }

                String responseBody = response.getBody();
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject food = jsonResponse.getJSONObject("food");
                
                String foodName = food.getString("food_name");
                String brandName = food.optString("brand_name", null);
                
                JSONObject servings = food.getJSONObject("servings");
                JSONObject serving = servings.getJSONObject("serving");
                
                int calories = serving.getInt("calories");
                double servingSize = serving.getDouble("metric_serving_amount");
                String servingUnit = serving.getString("metric_serving_unit");
                double protein = serving.getDouble("protein");
                double carbs = serving.getDouble("carbohydrate");
                double fat = serving.getDouble("fat");

                return new FatSecretFood(foodId, foodName, brandName, calories,
                                       servingSize, servingUnit, protein, carbs, fat);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting food info: " + e.getMessage());
            return null;
        }
    }

    public static FatSecretFood getFoodByBarcode(String barcode) {
        try {
            String url = API_URL + "?method=food.find_id_for_barcode&format=json&barcode=" + barcode;
            
            OAuthRequest request = new OAuthRequest(Verb.GET, url);
            service.signRequest(new OAuth1AccessToken("", ""), request);
            
            try (Response response = service.execute(request)) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error en la respuesta: " + response.getCode());
                }

                String responseBody = response.getBody();
                JSONObject jsonResponse = new JSONObject(responseBody);
                if (jsonResponse.has("food_id")) {
                    String foodId = jsonResponse.getString("food_id");
                    return getFoodInfo(foodId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting food by barcode: " + e.getMessage());
        }
        return null;
    }
}
