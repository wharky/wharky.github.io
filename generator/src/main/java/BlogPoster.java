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

    // [수정 1] 모델명 오타 수정 (2.5 -> 1.5)
    // 혹은 gemini-3-pro-preview 를 쓰셔도 됩니다. 여기선 최신 Flash 모델로 설정했습니다.
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    // -------------------------------------------------------------------
    // [설정 영역] Gemini가 요리할 '광범위한 재료(Seeds)'
    // -------------------------------------------------------------------
    private static final Map<String, String> CATEGORY_TOPICS = new HashMap<>();

    static {
        // 1. Tech
        CATEGORY_TOPICS.put("tech",
                "Game Design Patterns, System of Interactive Movie games ,Unity Engine Tricks, Unreal Engine Blueprints, " +
                        "Retro Console Architecture (NES/SNES/PS1), Develop Technique of Retro Nintendo Games, Procedural Generation Algorithms, " +
                        "Server-Side Network Sync (Rollback/Dead Reckoning), TRPG Rule Systems in Code, Great Adventure Game Techniques Use in TellTale Games" +
                        "Indie Game Marketing & Post-mortem, Shader Math & GLSL, AI Behavior Trees");

        // 2. Art
        CATEGORY_TOPICS.put("art",
                "Color Theory & Psychology, Pixel Art Techniques, Photorealism vs Stylized, " +
                        "Shape Language (Circle/Square/Triangle), Environmental Storytelling, " +
                        "UI/UX Design Philosophy (Diegetic/Meta), The Uncanny Valley, " +
                        "Fantasy Cartography, Architectural History (Gothic/Baroque/Cyberpunk), " +
                        "Lighting Composition & Mood, Character Silhouette Design");

        // 3. Lore
        CATEGORY_TOPICS.put("lore",
                "High Fantasy, Cyberpunk, Steampunk, Post-Apocalypse, Deep Sea Horror, Lord of the Rings, Dota2, League of Legends, " +
                        "Space Opera, Great HiveMind, Time Paradoxes, Magic the gathering, Telltale games Among us, " +
                        "Eldritch Gods, Artificial Intelligence Society, Magical Realism, World of Warcraft, " +
                        "Dystopian Government, Gangsters and Mafia, Ancient Mythology Reinterpretation");
    }

    // [수정 2] 불필요해진 CategoryConfig 클래스 삭제함

    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("실행 인자(카테고리명)가 필요합니다.");
        String categoryKey = args[0].toLowerCase();

        // [수정 3] 변수명 통일 (CATEGORY_MAP -> CATEGORY_TOPICS)
        if (!CATEGORY_TOPICS.containsKey(categoryKey)) {
            throw new IllegalArgumentException("지원하지 않는 카테고리입니다: " + categoryKey);
        }

        String apiKey = System.getenv("GAK");
        if (apiKey == null || apiKey.isEmpty()) throw new RuntimeException("API Key (GAK)가 없습니다.");

        String prompt = generatePrompt(categoryKey);
        System.out.println("Category: " + categoryKey + " / Creating New Post...");

        String responseText = callGemini(apiKey, prompt);
        savePost(categoryKey, responseText);
    }

    private static String generatePrompt(String categoryKey) {
        String topics = CATEGORY_TOPICS.get(categoryKey);
        long randomSeed = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();

        switch (categoryKey) {
            case "tech":
                sb.append("당신은 '괴짜 천재 게임 개발자'입니다.\n");
                sb.append("아래 [재료 키워드]들을 살펴보고, 그 중 2가지를 선택해 독창적으로 결합하거나 1개만 선택해 대해 심층적이며 **아주 구체적이고(Niche) 실무적인 주제** 하나를 선정하세요.\n");
                sb.append("예를 들어, 단순히 '유니티'가 아니라 '유니티에서 젤다의 전설 식의 툰 쉐이딩을 구현하는 3가지 트릭' 처럼 구체적이어야 합니다. 예시일뿐 참고만 하세요\n\n");
                sb.append("[재료 키워드]: ").append(topics).append("\n\n");
                sb.append("--- [Tech 작성 미션] (어떠한 재료 키워드가 사용되었는지 기재) ---\n");
                sb.append("1. **Deep Dive**: 겉핥기 식 정보는 금지합니다. 원리를 파고드세요.\n");
                sb.append("2. **Code & Logic**: 개발자들을 위해 의사코드(Pseudo-code)나 알고리즘 로직을 반드시 포함하세요.\n");
                sb.append("3. **Wit**: 딱딱하지 않게, 개발자 유머를 섞어 작성하세요.\n");
                sb.append("3. **Sense**: 꼭 개발적인 얘기로만 포스팅을 채울 필요는 없습니다. 블로그니까요. 주제에서 크게 선을 넘지 않는 선에서 관련된 흥미있는 얘기를 섞어도 좋습니다.\n");
                break;

            case "art":
                sb.append("당신은 '게임 미학(Game Aesthetics) 연구가'입니다.\n");
                sb.append("아래 [재료 키워드] 중 무작위로 2개를 골라서, 우리가 무심코 지나쳤던 시각적 요소의 **숨겨진 의도나 심리학적 원리**를 분석하는 주제를 선정하세요.\n");
                sb.append("단순히 '예쁘다'가 아니라 예시를 들면, '왜 이 게임은 이 컬러 팔레트를 사용했는가?' 혹은 '왜 이러한 게임 디자인, 혹은 레벨링 구조를 설계했는가?' 처럼 **'Why'**에 집중해야 합니다.\n\n");
                sb.append("[재료 키워드]: ").append(topics).append("\n\n");
                sb.append("--- [Art 작성 미션] (어떠한 재료 키워드가 사용되었는지 기재)---\n");
                sb.append("1. **이미지 없이 보는 법**: 텍스트만 읽어도 장면이 상상되도록, 이론(색채학, 구도 등)을 들어 설명하세요.\n");
                sb.append("2. **비교 분석**: 유명한 게임이나 예술 작품을 예시로 들어 설명하세요.\n");
                sb.append("3. **통찰력**: 독자가 게임을 보는 눈을 높여주세요.\n");
                sb.append("3. **접근성**: 누구나 맘 편히 읽을 수 있도록 이해하기 쉽게, 재미있게 설명해주세요.\n");
                break;

            case "lore":
                sb.append("당신은 '괴짜 천재 작가'입니다.\n");
                sb.append("아래 [재료 키워드] 중 2개를 무작위로 골라 믹스 앤 매치(Mix & Match)하여, **지금껏 어디서도 본 적 없는 새로운 세계관** 을 즉석에서 창조하세요. 너무 터무니 없는 조합이라면 **조합하지 않고 하나만 사용해도 좋습니다**.\n");
                sb.append("클리셰를 비틀거나 누구나 쉽게 생각하지 못 했을 법한 참신함이 필요합니다.\n\n");
                sb.append("[재료 키워드]: ").append(topics).append("\n\n");
                sb.append("--- [Lore 작성 미션 (목차 준수 및 어떠한 재료 키워드가 사용 되었는지 기재)] ---\n");
                sb.append("1. **기원(Origins)**: 이 세계가 뒤틀리거나 탄생한 결정적 사건.\n");
                sb.append("2. **환경(Environment)**: 기괴하거나 아름다운 지형과 날씨. 그로 인해 세계에 어떠한 영향이 끼쳐서 어떤 문명이 이루어졌는가 등.\n");
                sb.append("3. **생태계(Ecosystem)**: 그곳에 적응해 사는 독특한 생물이나 종족. 상응하는 두 종족이나 라이벌과 같은 관계처럼 유동적인 상관 관계를 포함하세요.\n");
                sb.append("4. **비밀(The Secret)**: 이 세계를 관통하는 충격적인 반전.\n");
                break;

            default:
                throw new IllegalArgumentException("정의되지 않은 카테고리입니다.");
        }

        sb.append("\n\n--- [시스템 제약사항] ---\n");
        sb.append("1. **NO HTML**: <div>, <span> 등 태그 사용 금지.\n");
        sb.append("2. **IMAGE_PROMPT**: 주제를 관통하는 예술적인 영어 프롬프트 작성.\n");
        sb.append("3. **Random Seed**: ").append(randomSeed).append(" (이 숫자는 무시하되, 매번 새로운 창의성을 발휘하는 트리거로 삼으세요.)\n");
        sb.append("4. **출력 형식 준수**:\n\n");
        sb.append("TITLE: [제목]\n");
        sb.append("IMAGE_PROMPT: [영어 이미지 프롬프트]\n");
        sb.append("BODY:\n");
        sb.append("[본문 내용]");

        return sb.toString();
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
        if (response.statusCode() != 200) throw new RuntimeException("API Error: " + response.body());
        return response.body();
    }

    private static void savePost(String category, String jsonResponse) throws IOException {
        String title = "제목 없음";
        String imagePrompt = "abstract digital art";
        String body = "";

        // 1. 파싱
        try {
            int textStart = jsonResponse.indexOf("\"text\": \"");
            if (textStart > -1) {
                String temp = jsonResponse.substring(textStart + 9);
                String rawText = temp.split("\"\\s*\\n*\\s*}")[0];
                String unescaped = rawText.replace("\\n", "\n").replace("\\\"", "\"");

                // [중요] HTML 태그 및 유니코드 찌꺼기 제거
                unescaped = unescaped.replaceAll("<[^>]*>", "").replace("\\u003c", "<").replace("\\u003e", ">");

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
            System.err.println("파싱 에러: " + e.getMessage());
        }

        // 2. 이미지 생성
        String encodedPrompt = URLEncoder.encode(imagePrompt, StandardCharsets.UTF_8);
        int randomSeed = (int)(Math.random() * 10000);
        String imageUrl = "https://image.pollinations.ai/prompt/" + encodedPrompt + "?width=800&height=450&nologo=true&seed=" + randomSeed;
        boolean isImageAvailable = checkImageAvailability(imageUrl);

        // 3. 파일 저장
        ZoneId kstZone = ZoneId.of("Asia/Seoul");
        LocalDate now = LocalDate.now(kstZone);
        // 파일명에 특수문자 제거
        String safeTitle = title.replaceAll("[^a-zA-Z0-9가-힣\\s]", "").replace(" ", "-");
        if(safeTitle.length() > 50) safeTitle = safeTitle.substring(0, 50);

        String fileName = "_posts/" + now.toString() + "-" + safeTitle + ".md";

        StringBuilder content = new StringBuilder();
        content.append("---\n");
        content.append("layout: post\n");
        content.append("title: \"" + title.replace("\"", "\\\"") + "\"\n");
        content.append("categories: " + category + "\n");
        content.append("---\n\n");

        if (isImageAvailable) {
            content.append("![" + title + "](" + imageUrl + ")\n\n");
            content.append("> **AI Image Prompt:** " + imagePrompt + "\n\n");
        } else {
            content.append("### \u26A0\uFE0F Image Generation Failed\n");
            content.append("```text\n");
            content.append("Prompt: " + imagePrompt + "\n");
            content.append("```\n\n");
        }
        content.append(body);

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content.toString());
        }
        System.out.println("Saved: " + fileName);
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
