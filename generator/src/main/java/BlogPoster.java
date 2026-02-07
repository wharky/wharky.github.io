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
    // [설정 영역] Gemini가 요리할 '광범위한 재료(Seeds)'만 던져줍니다.
    // -------------------------------------------------------------------
    private static final Map<String, String> CATEGORY_TOPICS = new HashMap<>();

    static {
        // 1. Tech: 최대한 다양한 분야를 나열
        CATEGORY_TOPICS.put("tech",
                "Game Design Patterns, Unity Engine Tricks, Unreal Engine Blueprints, " +
                        "Retro Console Architecture (NES/SNES/PS1), Procedural Generation Algorithms, " +
                        "Server-Side Network Sync (Rollback/Dead Reckoning), TRPG Rule Systems in Code, " +
                        "Indie Game Marketing & Post-mortem, Shader Math & GLSL, AI Behavior Trees");

        // 2. Art: 시각 예술의 이론적 재료들
        CATEGORY_TOPICS.put("art",
                "Color Theory & Psychology, Pixel Art Techniques, Photorealism vs Stylized, " +
                        "Shape Language (Circle/Square/Triangle), Environmental Storytelling, " +
                        "UI/UX Design Philosophy (Diegetic/Meta), The Uncanny Valley, " +
                        "Fantasy Cartography, Architectural History (Gothic/Baroque/Cyberpunk), " +
                        "Lighting Composition & Mood, Character Silhouette Design");

        // 3. Lore: 세계관 생성을 위한 재료들
        CATEGORY_TOPICS.put("lore",
                "High Fantasy, Cyberpunk, Steampunk, Post-Apocalypse, Deep Sea Horror, " +
                        "Space Opera, Subterranean Civilizations, Time Paradoxes, " +
                        "Eldritch Gods, Artificial Intelligence Society, Magical Realism, " +
                        "Dystopian Government, Ancient Mythology Reinterpretation");
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

    private static String generatePrompt(String categoryKey) {
        String topics = CATEGORY_TOPICS.get(categoryKey);

        // [핵심] LLM의 답변이 매번 달라지도록 '난수(Entropy)'를 프롬프트에 주입합니다.
        long randomSeed = System.currentTimeMillis();

        StringBuilder sb = new StringBuilder();

        // 1. 카테고리별 페르소나 및 창의적 주제 선정 지시
        switch (categoryKey) {
            case "tech":
                sb.append("당신은 '괴짜 천재 게임 개발자'입니다.\n");
                sb.append("아래 [재료 키워드]들을 살펴보고, 그 중 2~3가지를 독창적으로 결합하여 **아주 구체적이고(Niche) 실무적인 주제** 하나를 선정하세요.\n");
                sb.append("예를 들어, 단순히 '유니티'가 아니라 '유니티에서 젤다의 전설 식의 툰 쉐이딩을 구현하는 3가지 트릭' 처럼 구체적이어야 합니다.\n\n");
                sb.append("[재료 키워드]: ").append(topics).append("\n\n");
                sb.append("--- [Tech 작성 미션] ---\n");
                sb.append("1. **Deep Dive**: 겉핥기 식 정보는 금지합니다. 원리를 파고드세요.\n");
                sb.append("2. **Code & Logic**: 개발자들을 위해 의사코드(Pseudo-code)나 알고리즘 로직을 반드시 포함하세요.\n");
                sb.append("3. **Wit**: 딱딱하지 않게, 개발자 유머를 섞어 작성하세요.\n");
                break;

            case "art":
                sb.append("당신은 '게임 미학(Game Aesthetics) 연구가'입니다.\n");
                sb.append("아래 [재료 키워드]를 바탕으로, 우리가 무심코 지나쳤던 시각적 요소의 **숨겨진 의도나 심리학적 원리**를 분석하는 주제를 선정하세요.\n");
                sb.append("단순히 '예쁘다'가 아니라, '왜 이 공포 게임은 녹색 조명을 썼는가?' 처럼 **'Why'**에 집중해야 합니다.\n\n");
                sb.append("[재료 키워드]: ").append(topics).append("\n\n");
                sb.append("--- [Art 작성 미션] ---\n");
                sb.append("1. **이미지 없이 보는 법**: 텍스트만 읽어도 장면이 상상되도록, 이론(색채학, 구도 등)을 들어 설명하세요.\n");
                sb.append("2. **비교 분석**: 유명한 게임이나 예술 작품을 예시로 들어 설명하세요.\n");
                sb.append("3. **통찰력**: 독자가 게임을 보는 눈을 높여주세요.\n");
                break;

            case "lore":
                sb.append("당신은 '무한한 차원의 기록관(Archivist)'입니다.\n");
                sb.append("아래 [재료 키워드]를 믹스 앤 매치(Mix & Match)하여, **지금껏 어디서도 본 적 없는 새로운 세계관**을 즉석에서 창조하세요.\n");
                sb.append("클리셰를 비틀어야 합니다. (예: '마법이 있는 사이버펑크', '물이 없는 심해 문명' 등)\n\n");
                sb.append("[재료 키워드]: ").append(topics).append("\n\n");
                sb.append("--- [Lore 작성 미션 (목차 준수)] ---\n");
                sb.append("1. **기원(Origins)**: 이 세계가 뒤틀리거나 탄생한 결정적 사건.\n");
                sb.append("2. **환경(Environment)**: 기괴하거나 아름다운 지형과 날씨.\n");
                sb.append("3. **생태계(Ecosystem)**: 그곳에 적응해 사는 독특한 생물이나 종족.\n");
                sb.append("4. **비밀(The Secret)**: 이 세계를 관통하는 충격적인 반전.\n");
                break;

            default:
                throw new IllegalArgumentException("정의되지 않은 카테고리입니다.");
        }

        // 2. 공통 시스템 제약사항 (엄수)
        sb.append("\n\n--- [시스템 제약사항] ---\n");
        sb.append("1. **NO HTML**: <div>, <span> 등 태그 사용 금지.\n");
        sb.append("2. **IMAGE_PROMPT**: 주제를 관통하는 예술적인 영어 프롬프트 작성.\n");
        sb.append("3. **Random Seed**: ").append(randomSeed).append(" (이 숫자는 무시하되, 매번 새로운 창의성을 발휘하는 트리거로 삼으세요.)\n"); // 난수 주입
        sb.append("4. **출력 형식 준수**:\n\n");
        sb.append("TITLE: [제목]\n");
        sb.append("IMAGE_PROMPT: [영어 이미지 프롬프트]\n");
        sb.append("BODY:\n");
        sb.append("[본문 내용]");

        return sb.toString();
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
