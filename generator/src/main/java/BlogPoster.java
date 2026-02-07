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
    // [설정 영역] 카테고리별 주제와 톤을 여기서 관리하세요.
    // -------------------------------------------------------------------
    private static final Map<String, CategoryConfig> CATEGORY_MAP = new HashMap<>();

    static {
        // 1. Tech: 구체적인 기술적 깊이와 덕력을 요구
        CATEGORY_MAP.put("tech", new CategoryConfig(
                // 주제(Seeds)
                "Game Development Patterns, Unity/Unreal Engine Deep Dive, " +
                        "Retro Game Architecture (NES/SNES), Procedural Content Generation, " +
                        "Game Server Network Sync (Dead Reckoning), TRPG Rule Logic in Code, " +
                        "Indie Game Post-mortem, Shader Programming, Memory Management in Games",

                // 어조(Tone)
                "마치 10년 차 시니어 게임 클라이언트 개발자가 후배에게 열정적으로 설명하는 톤. " +
                        "기술적 용어를 적절히 섞고, 깊이 있는 분석과 인사이트를 반드시 포함할 것."
        ));

        // 2. Art: 세계관과 미학, 철학을 요구
        CATEGORY_MAP.put("art", new CategoryConfig(
                // 주제(Seeds)
                "Magic: The Gathering Color Philosophy, Elden Ring Environmental Storytelling, " +
                        "Lovecraftian Cosmic Horror Art, Pixel Art Aesthetics & Limitations, " +
                        "Dark Fantasy Concept Art, Classical TRPG Bestiary Art Styles, " +
                        "Visual Storytelling in UI Design, Glitch Art & Cyberpunk Aesthetics",

                // 어조(Tone)
                "예술 대학의 괴짜 교수님이나 판타지 세계관의 현자가 이야기하는 듯한 톤. " +
                        "단순한 묘사를 넘어 그 안에 숨겨진 철학이나 의도를 해석하려 노력할 것."
        ));
    }
    // -------------------------------------------------------------------

    // 설정값을 담을 내부 클래스 (구조체 역할)
    static class CategoryConfig {
        String topics; // 주제 목록
        String tone;   // 글의 어조

        public CategoryConfig(String topics, String tone) {
            this.topics = topics;
            this.tone = tone;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("실행 인자(카테고리명)가 필요합니다. (예: tech, art)");
        String categoryKey = args[0].toLowerCase();

        // 카테고리 존재 여부 확인
        if (!CATEGORY_MAP.containsKey(categoryKey)) {
            throw new IllegalArgumentException("지원하지 않는 카테고리입니다: " + categoryKey + "\n지원 목록: " + CATEGORY_MAP.keySet());
        }

        String apiKey = System.getenv("GAK");
        if (apiKey == null || apiKey.isEmpty()) throw new RuntimeException("API Key (GAK)가 없습니다.");

        // 1. 프롬프트 생성 (Map에서 꺼내옴)
        String prompt = generatePrompt(categoryKey);
        System.out.println("Category: " + categoryKey + " / Asking Gemini...");

        // 2. Gemini API 호출
        String responseText = callGemini(apiKey, prompt);

        // 3. 결과 저장 (이미지 체크 로직 포함)
        savePost(categoryKey, responseText);
    }

    private static String generatePrompt(String categoryKey) {
        CategoryConfig config = CATEGORY_MAP.get(categoryKey);

        String baseRequest =
                "당신은 해당 분야의 깊은 지식을 가진 전문가(Nerd/Geek) 블로거입니다.\n" +
                "아래 제공된 [키워드 목록]을 바탕으로, 오늘 독자들에게 들려줄 가장 흥미롭고 구체적인 주제 하나를 스스로 선정하세요.\n" +
                "뻔하거나 일반적인 내용은 피하고, 당신만의 독창적인 시각이나 깊이 있는 분석이 담긴 주제여야 합니다.\n\n" +

                "[키워드 목록]: " + config.topics + "\n" +
                "[글의 톤앤매너]: " + config.tone + "\n\n" +

                "작성 규칙을 엄격히 준수하세요:\n" +
                "1. TITLE: 선정된 주제를 바탕으로 클릭을 유도하는 매력적인 제목을 지으세요.\n" +
                "2. IMAGE_PROMPT: 글의 분위기를 완벽하게 표현하는 예술적인 영어 프롬프트를 작성하세요. (구체적인 조명, 스타일 묘사 필수)\n" +
                "3. BODY: 마크다운 형식을 사용하여 본문을 작성하세요. 소제목, 불렛 포인트 등을 활용하여 가독성을 높이세요.\n\n" +

                "출력 형식:\n" +
                "TITLE: [제목]\n" +
                "IMAGE_PROMPT: [영어 이미지 프롬프트]\n" +
                "BODY:\n" +
                "[본문 내용]";

        return baseRequest;
    }

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

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API 호출 실패: " + response.body());
        }
        return response.body();
    }

    private static void savePost(String category, String jsonResponse) throws IOException {
        String title = "제목 없음";
        String imagePrompt = "abstract digital art";
        String body = "";

        // --- 1. 본문 파싱 ---
        try {
            int textStart = jsonResponse.indexOf("\"text\": \"");
            if (textStart > -1) {
                String temp = jsonResponse.substring(textStart + 9);
                String rawText = temp.split("\"\\s*\\n*\\s*}")[0];
                String unescaped = rawText.replace("\\n", "\n").replace("\\\"", "\"");

                String[] lines = unescaped.split("\n");
                boolean bodyStarted = false;
                StringBuilder bodyBuilder = new StringBuilder();

                for (String line : lines) {
                    if (line.startsWith("TITLE:")) {
                        title = line.replace("TITLE:", "").trim();
                    } else if (line.startsWith("IMAGE_PROMPT:")) {
                        imagePrompt = line.replace("IMAGE_PROMPT:", "").trim();
                    } else if (line.startsWith("BODY:")) {
                        bodyStarted = true;
                        continue;
                    } else if (bodyStarted) {
                        bodyBuilder.append(line).append("\n");
                    }
                }
                body = bodyBuilder.toString();
                if (body.isEmpty()) body = unescaped;
            }
        } catch (Exception e) {
            System.err.println("파싱 중 에러 발생: " + e.getMessage());
        }

        // --- 2. 이미지 URL 생성 및 검증 ---
        String encodedPrompt = URLEncoder.encode(imagePrompt, StandardCharsets.UTF_8);
        int randomSeed = (int)(Math.random() * 10000);
        String imageUrl = "https://image.pollinations.ai/prompt/" + encodedPrompt + "?width=800&height=450&nologo=true&seed=" + randomSeed;

        System.out.println("🎨 이미지 생성 시도: " + imagePrompt);
        boolean isImageAvailable = checkImageAvailability(imageUrl);

        // --- 3. 파일 저장 ---
        ZoneId kstZone = ZoneId.of("Asia/Seoul");
        LocalDate now = LocalDate.now(kstZone);
        String date = now.toString();

        String safeTitle = title.replaceAll("[^a-zA-Z0-9가-힣\\s]", "").replace(" ", "-");
        if(safeTitle.length() > 50) safeTitle = safeTitle.substring(0, 50);

        String fileName = "_posts/" + date + "-" + safeTitle + ".md";

        StringBuilder content = new StringBuilder();
        content.append("---\n");
        content.append("layout: post\n");
        content.append("title: \"" + title.replace("\"", "\\\"") + "\"\n");
        content.append("categories: " + category + "\n");
        content.append("---\n\n");

        if (isImageAvailable) {
            content.append("![" + title + "](" + imageUrl + ")\n\n");
            content.append("> **AI Image Prompt:** " + imagePrompt + "\n\n");
            System.out.println("✅ 이미지 생성 성공! 포스팅에 포함합니다.");
        } else {
            content.append("### \u26A0\uFE0F Image Generation Failed\n");
            content.append("```text\n");
            content.append("Prompt: " + imagePrompt + "\n");
            content.append("```\n");
            content.append("> 서버 문제로 이미지가 로드되지 않았습니다. 위 프롬프트를 참고하세요.\n\n");
            System.out.println("⚠️ 이미지 서버 에러(502). 프롬프트 텍스트로 대체합니다.");
        }

        content.append(body);

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content.toString());
        }
        System.out.println("✅ 저장 완료: " + fileName);
    }

    private static boolean checkImageAvailability(String imageUrl) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}