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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlogPoster {

    // 모델명: gemini-2.5-flash (최신 모델 권장)
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    // -------------------------------------------------------------------
    // [설정 영역] Gemini가 요리할 '광범위한 재료(Seeds)'
    // (오타 수정 및 가독성 개선 완료)
    // -------------------------------------------------------------------
    private static final Map<String, String> CATEGORY_TOPICS = new HashMap<>();

    static {
        // 1. Tech
        CATEGORY_TOPICS.put("tech",
                "Game Design Patterns, System of Interactive Movie Games, Unity Engine Optimization Tricks, Unreal Engine Blueprints Best Practices, " +
                        "Retro Console Architecture (NES/SNES/PS1), Development Techniques of Retro Nintendo Games, Procedural Generation Algorithms, " +
                        "Server-Side Network Sync (Rollback/Dead Reckoning), TRPG Rule Systems implemented in Code, Narrative Design Techniques used in Telltale Games, " +
                        "Indie Game Marketing & Post-mortem, Shader Math & GLSL, AI Behavior Trees, Data-Oriented Technology Stack (DOTS)");

        // 2. Art
        CATEGORY_TOPICS.put("art",
                "Color Theory & Psychology in Games, Pixel Art Techniques, Photorealism vs Stylized, " +
                        "Shape Language (Circle/Square/Triangle), Environmental Storytelling, " +
                        "UI/UX Design Philosophy (Diegetic/Meta), The Uncanny Valley effect, " +
                        "Fantasy Cartography, Architectural History (Gothic/Baroque/Cyberpunk), " +
                        "Lighting Composition & Mood, Character Silhouette Design, Visual Hierarchy");

        // 3. Lore
        CATEGORY_TOPICS.put("lore",
                "High Fantasy, Cyberpunk, Steampunk, Post-Apocalypse, Deep Sea Horror, League of Legends Universe, " +
                        "Space Opera, Cosmic Horror, Time Paradoxes, Magic the Gathering Universe, Among Us(Telltale Games) Universe, DC Comics Universe, " +
                        "Eldritch Gods, Artificial Intelligence Society, Magical Realism, Path Of Exile(Online Game) Universe, " +
                        "Dystopian Government, Gangsters and Mafia");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("실행 인자(카테고리명)가 필요합니다.");
        String categoryKey = args[0].toLowerCase();

        if (!CATEGORY_TOPICS.containsKey(categoryKey)) {
            throw new IllegalArgumentException("지원하지 않는 카테고리입니다: " + categoryKey);
        }

        String apiKey = System.getenv("GAK");
        if (apiKey == null || apiKey.isEmpty()) throw new RuntimeException("API Key (GAK)가 없습니다.");

        // Java에서 랜덤 키워드를 강제로 뽑아서 프롬프트 생성
        String prompt = generatePrompt(categoryKey);
        System.out.println("Category: " + categoryKey + " / Creating New Post...");

        String responseText = callGemini(apiKey, prompt);
        savePost(categoryKey, responseText);
    }

    private static String generatePrompt(String categoryKey) {
        // 1. 전체 키워드 문자열 가져오기
        String allTopicsString = CATEGORY_TOPICS.get(categoryKey);
        
        // 2. 쉼표로 쪼개고 리스트로 변환
        String[] topicArray = allTopicsString.split(",");
        List<String> topicList = new ArrayList<>();
        for (String t : topicArray) {
            if (!t.trim().isEmpty()) {
                topicList.add(t.trim());
            }
        }

        // 3. [핵심] Java에서 직접 셔플 (진정한 무작위 보장)
        Collections.shuffle(topicList);

        // 4. 키워드 2개 선정
        String selectedTopic1 = topicList.get(0);
        String selectedTopic2 = (topicList.size() > 1) ? topicList.get(1) : selectedTopic1;
        
        // 프롬프트에 들어갈 최종 재료
        String finalSelectedTopics = String.format("'%s' AND '%s'", selectedTopic1, selectedTopic2);

        System.out.println("Selected Random Topics: " + finalSelectedTopics); 

        long randomSeed = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();

        // -------------------------------------------------------------------
        // [프롬프트 개선 버전]
        // -------------------------------------------------------------------
        switch (categoryKey) {
            case "tech":
                sb.append("당신은 '실력 있는 시니어 게임 개발자'이자 '테크 블로거'입니다.\n");
                sb.append("이번 포스팅의 핵심 주제는 다음 두 가지 키워드의 결합입니다: **[").append(finalSelectedTopics).append("]**\n");
                sb.append("이 두 키워드를 연결하여, 현업 개발자들에게 영감을 줄 수 있는 **깊이 있고(Deep Dive) 실무적인(Practical) 기술 포스팅**을 작성하세요.\n");
                sb.append("만약 두 키워드의 연결이 너무 억지스럽다면, 첫 번째 키워드를 메인으로 잡고 두 번째 키워드는 비유나 예시로 활용하세요.\n\n");
                
                sb.append("--- [Tech 작성 가이드라인] ---\n");
                sb.append("1. **Problem & Solution**: 단순 설명보다는 '어떤 문제가 있었고, 이를 이 기술로 어떻게 해결했는가'의 구조를 가지세요.\n");
                sb.append("2. **Code & Logic**: 개발자들을 위해 의사코드(Pseudo-code)나 핵심 알고리즘 로직, 혹은 아키텍처 다이어그램 설명을 반드시 포함하세요.\n");
                sb.append("3. **Opinionated**: 단순히 정보를 나열하지 말고, 당신의 주관적인 견해나 경험담(Storytelling)을 섞어 글에 맛을 더하세요.\n");
                sb.append("4. **Tone**: 너무 딱딱한 논문체보다는, 동료 개발자에게 커피 한 잔 마시며 설명하듯 위트 있고 지적인 어조를 사용하세요.\n");
                break;

            case "art":
                sb.append("당신은 '게임 미학(Game Aesthetics) 큐레이터'입니다.\n");
                sb.append("이번 포스팅의 주제는 다음 두 가지 키워드의 미학적 분석입니다: **[").append(finalSelectedTopics).append("]**\n");
                sb.append("이 요소들이 게임 내에서 어떻게 플레이어의 감정을 조작하고 몰입감을 주는지, **'Why'와 'How'**에 집중하여 분석하세요.\n\n");
                
                sb.append("--- [Art 작성 가이드라인] ---\n");
                sb.append("1. **Ekphrasis(공감각적 묘사)**: 독자가 이미지를 보지 않고도 머릿속에 장면이 그려지도록 광원, 질감, 색감, 분위기를 섬세하게 묘사하세요.\n");
                sb.append("2. **Case Study**: 실제 존재하는 유명 게임이나 예술 사조를 구체적인 예시로 들어 비교 분석하세요.\n");
                sb.append("3. **Insight**: 단순히 '예쁘다'를 넘어, 그것이 게임의 메커니즘이나 내러티브와 어떻게 연결되는지 통찰력을 보여주세요.\n");
                sb.append("4. **Tone**: 감성적이지만 분석적인, 마치 미술관 가이드가 설명해주는 듯한 우아한 어조를 유지하세요.\n");
                break;

            case "lore":
                sb.append("당신은 차원을 넘나드는 '세계관 설계자(World Builder)'입니다.\n");
                sb.append("다음 두 가지 키워드를 믹스 앤 매치(Mix & Match)하여 **매혹적이고 독창적인 새로운 세계**를 창조하세요: **[").append(finalSelectedTopics).append("]**\n");
                sb.append("흔한 클리셰를 비틀어, 독자에게 신선한 충격을 주어야 합니다. 억지로 두 개를 섞기 어렵다면 하나를 메인 테마로 잡으세요.\n\n");
                
                sb.append("--- [Lore 작성 가이드라인] ---\n");
                sb.append("1. **Narrative Hook**: 당신은 훌륭한 스토리 텔러입니다. 생생하면서도 디테일한 설명으로 독자가 몰입하도록 하세요.\n");
                sb.append("2. **Origins (기원)**: 이 세계가 왜 이런 형태가 되었는지에 대한 역사적/신화적 배경.\n");
                sb.append("3. **Ecosystem & Culture**: 그 환경에 적응한 고유하고 특이한 생태계와 독특한 사회 구조/법칙.\n");
                sb.append("3. **Species & Tribe**: 그 세상에 살고 있는 특색 있는 종족이나 부족 등. 유니크한 종족을 창조하여 설명하세요.\n");
                sb.append("4. **The Twist (비밀)**: 겉으로 보이는 평화나 질서 뒤에 숨겨진 어두운 비밀이나 반전 요소를 포함하세요.\n");
                break;

            default:
                throw new IllegalArgumentException("정의되지 않은 카테고리입니다.");
        }

        sb.append("\n\n--- [시스템 필수 제약사항] ---\n");
        sb.append("1. **NO HTML**: <div>, <span> 등 태그 사용 금지 (Markdown만 사용).\n");
        sb.append("2. **Language**: 한국어(Korean)로 자연스럽게 작성.\n");
        sb.append("3. **IMAGE_PROMPT**: 주제를 가장 잘 나타내는 고퀄리티 예술적 영문 프롬프트 (Abstract, Cinematic lighting 등 키워드 포함).\n");
        sb.append("4. **Random Seed**: ").append(randomSeed).append("\n");
        sb.append("5. **출력 형식(Strict format)**:\n\n");
        sb.append("TITLE: [클릭을 유도하는 매력적인 제목]\n");
        sb.append("IMAGE_PROMPT: [영어 이미지 프롬프트]\n");
        sb.append("BODY:\n");
        sb.append("본문 시작 시, 오늘의 주제 키워드(").append(finalSelectedTopics).append(")를 자연스럽게 언급하며 시작하세요.\n");
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

                // HTML 태그 및 유니코드 찌꺼기 제거
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
