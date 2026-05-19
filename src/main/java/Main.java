import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;

public class Main {
     public static void main(String[] args) throws Exception {
          main1(args);
         //main2(args);
     }

    public static void main1(String[] args) throws Exception {
        String clientId = "evc338djrpxme75hr34q";
        String secret = "30a960fb82e94ffe933925e788ad53a4";
        String t = String.valueOf(System.currentTimeMillis());

        String method = "GET";
        String url = "/v1.0/token?grant_type=1";

        // 1. Хэш пустого тела (Content-SHA256)
        String contentHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        // 2. Сборка StringToSign (V2 формат)
        String stringToSign = method + "\n" + contentHash + "\n" + "" + "\n" + url;

        // 3. Финальная склейка: ID + t + StringToSign
        String signSource = clientId + t + stringToSign;

        // 4. HMAC-SHA256
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        String sign = getSign(sha256_HMAC, signSource);

        System.out.println("CURL Headers:");
        System.out.println("t: " + t);
        System.out.println("sign: " + sign);
    }

   public static void main2(String[] args) throws Exception {
        String clientId = "evc338djrpxme75hr34q";
        String secret = "30a960fb82e94ffe933925e788ad53a4";
        String accessToken = "cb7277224ce9d415b30eb6890ecdfe1b"; // Твой свежий токен
        String deviceId = "bf4884553f9061c2adyuaf";

        String t = String.valueOf(System.currentTimeMillis());
        String method = "GET";
        String url = "/v1.0/devices/" + deviceId + "/status";

        // Хэш пустого тела
        String contentHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        String stringToSign = method + "\n" + contentHash + "\n" + "" + "\n" + url;

        // ВАЖНО: accessToken теперь участвует в склейке!
        String signSource = clientId + accessToken + t + stringToSign;

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        String sign = getSign(sha256_HMAC, signSource);

        System.out.println("CURL Headers for Device Status:");
        System.out.println("client_id: " + clientId);
        System.out.println("access_token: " + accessToken);
        System.out.println("t: " + t);
        System.out.println("sign: " + sign);
    }

    private static String getSign(Mac sha256_HMAC, String signSource) {
        byte[] hash = sha256_HMAC.doFinal(signSource.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hash) { result.append(String.format("%02x", b)); }
        return result.toString().toUpperCase();
    }
}

