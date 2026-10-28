package com.yourname.digishop.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;

public class Bt26CardPicker {
    private static final String API = "https://digimoncard.io/api-public";
    private static final String IMG = "https://images.digimoncard.io/images/cards/";
    private static final Path IMG_DIR = Path.of("D:/Image/digimon");
    private static final String PREFIX = "BT26-";

    // Độ hiếm muốn lấy và số thẻ tối đa mỗi loại (tự chỉnh)
    private static final Map<String, Integer> LIMIT = new LinkedHashMap<>();
    static {
        LIMIT.put("C", 12);
        LIMIT.put("U", 10);
        LIMIT.put("R", 8);
        LIMIT.put("SR", 6);
        LIMIT.put("UR", 3);     // nếu game không có UR thì mục này tự rỗng
        LIMIT.put("SEC", 4);
    }

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Files.createDirectories(IMG_DIR);
        Files.createDirectories(Path.of("src/main/resources/data"));

        // 1. Lấy toàn bộ mã thẻ BT26
        JsonNode all = getJson(API + "/getAllCards?series=Digimon%20Card%20Game&sort=card_number");
        List<String> ids = new ArrayList<>();
        for (JsonNode n : all) {
            String no = n.path("cardnumber").asText();
            if (no.startsWith(PREFIX) && !ids.contains(no)) ids.add(no);
        }
        System.out.println("BT26 co " + ids.size() + " ma the.");

        // 2. Lấy chi tiết theo lô, gom nhóm theo độ hiếm
        Map<String, List<JsonNode>> byRarity = new LinkedHashMap<>();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < ids.size(); i += 20) {
            sleep(1000);                                   // giới hạn 15 request / 10 giây
            String url = API + "/search?series=Digimon%20Card%20Game&card="
                    + String.join(",", ids.subList(i, Math.min(i + 20, ids.size())));
            for (JsonNode n : getJson(url)) {
                String id = n.path("id").asText();
                if (!seen.add(id)) continue;               // bỏ trùng
                byRarity.computeIfAbsent(n.path("rarity").asText().toUpperCase(),
                        k -> new ArrayList<>()).add(n);
            }
        }

        // 3 + 4. Chọn theo độ hiếm, chỉ giữ thẻ có ảnh
        List<Map<String, Object>> result = new ArrayList<>();
        for (var e : LIMIT.entrySet()) {
            List<JsonNode> list = byRarity.getOrDefault(e.getKey(), List.of());
            int taken = 0;
            for (JsonNode n : list) {
                if (taken >= e.getValue()) break;
                String id = n.path("id").asText();
                if (!ensureImage(id)) {
                    System.out.println("   bo qua (khong tai duoc anh): " + id);
                    continue;
                }
                result.add(toMap(n));
                taken++;
            }
            System.out.println(e.getKey() + ": API co " + list.size() + " the, lay " + taken);
        }

        // Cho biết các độ hiếm khác có trong set mà bạn không chọn
        byRarity.keySet().stream().filter(r -> !LIMIT.containsKey(r))
                .forEach(r -> System.out.println("(khong lay) " + r + ": "
                        + byRarity.get(r).size() + " the"));

        // 5. Ghi cards.json
        MAPPER.writerWithDefaultPrettyPrinter()
                .writeValue(new File("src/main/resources/data/cards.json"), result);
        System.out.println("Da ghi cards.json voi " + result.size() + " the.");
    }

    private static boolean ensureImage(String id) throws Exception {
        Path file = IMG_DIR.resolve(id + ".webp");
        if (Files.exists(file)) return true;               // đã có thì khỏi tải
        sleep(700);
        HttpResponse<InputStream> res = CLIENT.send(
                HttpRequest.newBuilder(URI.create(IMG + id + ".webp")).GET().build(),
                HttpResponse.BodyHandlers.ofInputStream());
        if (res.statusCode() != 200) return false;
        try (InputStream in = res.body()) {
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
        }
        return true;
    }

    private static Map<String, Object> toMap(JsonNode n) {
        String id = n.path("id").asText();
        String setCode = id.substring(0, id.indexOf('-'));
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("cardCode", id);
        c.put("name", n.path("name").asText());
        c.put("cardType", n.path("type").asText().toUpperCase().replace("-", "_"));
        c.put("color", n.path("color").asText().toUpperCase());
        c.put("rarity", n.path("rarity").asText().toUpperCase());
        c.put("level", intOrNull(n, "level"));
        c.put("playCost", intOrNull(n, "play_cost"));
        c.put("dp", intOrNull(n, "dp"));
        c.put("digimonAttribute", textOrNull(n, "attribute"));
        c.put("digimonType", textOrNull(n, "type"));
        c.put("digimonForm", textOrNull(n, "stage"));
        c.put("effect", n.path("main_effect").asText());
        c.put("setCode", setCode);
        c.put("setName", n.path("set_name").isArray() && n.path("set_name").size() > 0
                ? n.path("set_name").get(0).asText() : setCode);
        return c;
    }

    private static String textOrNull(JsonNode n, String field) {
        JsonNode v = n.path(field);
        if (v.isMissingNode() || v.isNull() || v.asText().isBlank()) return null;
        return v.asText();
    }

    private static Integer intOrNull(JsonNode n, String field) {
        JsonNode v = n.path(field);
        return v.isMissingNode() || v.isNull() ? null : v.asInt();
    }

    private static JsonNode getJson(String url) throws Exception {
        HttpResponse<String> res = CLIENT.send(
                HttpRequest.newBuilder(URI.create(url)).header("Accept", "application/json")
                        .GET().build(), HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 400) return MAPPER.createArrayNode();   // không tìm thấy thẻ nào
        if (res.statusCode() != 200)
            throw new RuntimeException("HTTP " + res.statusCode() + " tu " + url + "\n" + res.body());
        return MAPPER.readTree(res.body());
    }

    private static void sleep(long ms) throws InterruptedException { Thread.sleep(ms); }
}