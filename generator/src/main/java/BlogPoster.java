import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class BlogPoster {

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    // -------------------------------------------------------------------
    // [설정 영역]
    // -------------------------------------------------------------------
    private static final Map<String, CategoryConfig> CATEGORY_MAP = new HashMap<>();

    static {
        // 1. Tech 설정
        CATEGORY_MAP.put("tech", new CategoryConfig(
                "Game Development Patterns, Unity/Unreal Optimization, " +
                        "Retro Game Architecture (NES/SNES), Procedural Content Generation, " +
                        "Game Server Sync (Dead Reckoning), TRPG Rule Logic in Code, " +
                        "Indie Game Post-mortem, Shader Programming", // 주제

                "10년 차 시니어 개발자가 후배에게 설명하는 깊이 있고 분석적인 톤.", // 톤

                "소제목을 활용하여 기술적인 원리를 단계별로 설명하세요. 코드 예시가 있다면 포함하세요." // 추가 지침
        ));

        // 2. Art 설정
        CATEGORY_MAP.put("art", new CategoryConfig(
                "Magic: The Gathering Color Philosophy, Elden Ring Environmental Storytelling, " +
                        "Lovecraftian Cosmic Horror Art, Pixel Art Aesthetics, " +
                        "Dark Fantasy Concept Art, Classical TRPG Bestiary Art Styles",

                "미학적이고 철학적인 톤. 갤러리 큐레이터나 판타지 세계의 현자가 설명하듯이.",

                "작품의 시각적 요소(색감, 구도)와 그 안에 담긴 의도를 해석하는 데 집중하세요."
        ));

        // 3. [New] Lore (세계관) 설정
        CATEGORY_MAP.put("lore", new CategoryConfig(
                "High Fantasy, Cyberpunk Dystopia, Steampunk Floating Islands, " +
                        "Post-Apocalyptic Overgrown Cities, Deep Sea Civilization, " +
                        "Cosmic Horror Space Opera, Subterranean Kingdom, Time-Loop World," +
                        "Classic Space Opera TRPG Base",

                "전지전능한 창조주(Creator) 혹은 고대 도서관의 기록관이 비사를 읊어주는 듯한 장엄하고 신비로운 톤.",

                // 여기가 핵심입니다! (필수 포함 요소 지정)
                "반드시 다음 4가지 목차를 포함하여 작성하세요:\n" +
                        "1. **세계의 기원과 설정**: 이 세계가 어떻게 탄생했는지, 마법이나 기술의 수준은 어떤지.\n" +
                        "2. **환경과 기후**: 독특한 날씨, 지형, 생태계 묘사.\n" +
                        "3. **주요 종족과 문화**: 인간 외의 독창적인 종족들과 그들의 식생, 신앙, 갈등.\n" +
                        "4. **충격적인 반전 요소**: 이 세계관의 사람들이 모르고 있는 비밀이나, 역사의 진실 (Plot Twist)."
        ));
    }

    // 설정값을 담을 내부 클래스 (필드 추가됨)
    static class CategoryConfig {
        String topics;       // 주제 키워드
        String tone;         // 어조
        String instructions; // 구체적인 작성 지침 (목차 등)

        public CategoryConfig(String topics, String tone, String instructions) {
            this.topics = topics;
            this.tone = tone;
            this.instructions = instructions;
        }
    }

    // ... (main 메서드 등은 기존과 동일) ...
    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("실행 인자(카테고리명)가 필요합니다.");
        String categoryKey = args[0].toLowerCase();

        if (!CATEGORY_MAP.containsKey(categoryKey)) {
            throw new IllegalArgumentException("지원하지 않는 카테고리입니다: " + categoryKey);
        }

        String apiKey = System.getenv("GAK");
        if (apiKey == null || apiKey.isEmpty()) throw new RuntimeException("API Key (GAK)가 없습니다.");

        String prompt = generatePrompt(categoryKey);
        System.out.println("Category: " + categoryKey + " / Creating New World...");

        String responseText = callGemini(apiKey, prompt);
        savePost(categoryKey, responseText);
    }

    // ... (generatePrompt 메서드 수정됨) ...
    private static String generatePrompt(String categoryKey) {
        CategoryConfig config = CATEGORY_MAP.get(categoryKey);

        return "당신은 해당 분야의 최고의 전문가이자 창작자입니다.\n" +
                "아래 제공된 [키워드 목록] 중 하나를 랜덤으로 선택하거나 조합하여, " +
                "오늘 독자들에게 선보일 가장 독창적인 콘텐츠를 생성하세요.\n\n" +

                "[키워드 목록]: " + config.topics + "\n" +
                "[글의 톤앤매너]: " + config.tone + "\n\n" +

                "[필수 작성 지침]:\n" + config.instructions + "\n\n" + // 여기가 추가됨

                "작성 규칙을 엄격히 준수하세요:\n" +
                "1. TITLE: 내용을 관통하는 매력적인 제목을 지으세요.\n" +
                "2. IMAGE_PROMPT: 글의 분위기를 완벽하게 표현하는 예술적인 영어 프롬프트를 작성하세요. (구체적인 조명, 스타일 묘사 필수)\n" +
                "3. BODY: 오직 '표준 마크다운(Markdown)' 문법만 사용하세요.\n" +
                "   - ❌ 금지: HTML 태그, 색상 코드 절대 금지.\n" +
                "   - ✅ 권장: 소제목(#), 굵게(**), 리스트(-) 활용.\n\n" +

                "출력 형식:\n" +
                "TITLE: [제목]\n" +
                "IMAGE_PROMPT: [영어 이미지 프롬프트]\n" +
                "BODY:\n" +
                "[본문 내용]";
    }

    // ... (callGemini, savePost 등 나머지 메서드는 기존 유지) ...
    // ... (checkImageAvailability 등 포함) ...
    // (이전 답변의 savePost 메서드를 그대로 쓰시면 됩니다.
    // HTML 태그 제거 로직이 포함된 버전을 권장합니다.)

    // 편의를 위해 callGemini와 savePost 부분만 아래에 간략히 붙입니다.
    // 실제로는 직전 답변의 코드를 그대로 쓰되 위 설정 부분만 바꾸면 됩니다.

    private static String callGemini(String apiKey, String prompt) throws IOException, InterruptedException {
        String safePrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n");
        String jsonBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + safePrompt + "\"}]}]}";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new RuntimeException("API Error: " + response.body());
        return response.body();
    }

    private static void savePost(String category, String jsonResponse) throws IOException {
        // ... (직전 코드와 동일: 파싱 -> HTML 태그 제거 -> 이미지 생성 -> 저장) ...
        // ... (내용이 길어 생략하지만, 직전 답변의 savePost를 그대로 사용하세요) ...
        // 여기서는 생략된 부분만 채워넣으시면 됩니다.

        // 1. 파싱 (제목, 프롬프트, 본문 분리)
        String title = "제목 없음";
        String imagePrompt = "fantasy world landscape";
        String body = "";

        try {
            int textStart = jsonResponse.indexOf("\"text\": \"");
            if (textStart > -1) {
                String temp = jsonResponse.substring(textStart + 9);
                String rawText = temp.split("\"\\s*\\n*\\s*}")[0];
                String unescaped = rawText.replace("\\n", "\n").replace("\\\"", "\"");

                // HTML 태그 제거 (중요!)
                unescaped = unescaped.replaceAll("<[^>]*>", "").replace("\\u003c", "<").replace("\\u003e", ">");

                String[] lines = unescaped.split("\n");
                boolean bodyStarted = false;
                StringBuilder bodyBuilder = new StringBuilder();

                for (String line : lines) {
                    if (line.startsWith("TITLE:")) title = line.replace("TITLE:", "").trim();
                    else if (line.startsWith("IMAGE_PROMPT:")) imagePrompt = line.replace("IMAGE_PROMPT:", "").trim();
                    else if (line.startsWith("BODY:")) { bodyStarted = true; continue; }
                    else if (bodyStarted) bodyBuilder.append(line).append("\n");
                }
                body = bodyBuilder.toString();
                if (body.isEmpty()) body = unescaped;
            }
        } catch (Exception e) { System.err.println("파싱 에러: " + e.getMessage()); }

        // 2. 이미지 생성
        String encodedPrompt = URLEncoder.encode(imagePrompt, StandardCharsets.UTF_8);
        int randomSeed = (int)(Math.random() * 10000);
        String imageUrl = "https://image.pollinations.ai/prompt/" + encodedPrompt + "?width=800&height=450&nologo=true&seed=" + randomSeed;
        boolean isImageAvailable = checkImageAvailability(imageUrl);

        // 3. 저장
        ZoneId kstZone = ZoneId.of("Asia/Seoul");
        LocalDate now = LocalDate.now(kstZone);
        String fileName = "_posts/" + now.toString() + "-" + title.replaceAll("[^a-zA-Z0-9가-힣\\s]", "").replace(" ", "-") + ".md";

        StringBuilder content = new StringBuilder();
        content.append("---\nlayout: post\ntitle: \"" + title.replace("\"", "\\\"") + "\"\ncategories: " + category + "\n---\n\n");

        if (isImageAvailable) {
            content.append("![" + title + "](" + imageUrl + ")\n\n> **AI Image Prompt:** " + imagePrompt + "\n\n");
        } else {
            content.append("### \u26A0\uFE0F Image Generation Failed\n```text\nPrompt: " + imagePrompt + "\n```\n\n");
        }
        content.append(body);

        try (FileWriter writer = new FileWriter(fileName)) { writer.write(content.toString()); }
        System.out.println("Saved: " + fileName);
    }

    private static boolean checkImageAvailability(String imageUrl) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(imageUrl)).GET().timeout(Duration.ofSeconds(30)).build();
            return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
        } catch (Exception e) { return false; }
    }
}
