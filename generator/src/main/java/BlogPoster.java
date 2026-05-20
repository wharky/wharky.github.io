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

    // 모델명: gemini-2.5-flash (최신 모델)
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    // -------------------------------------------------------------------
    // [설정 영역] 트렌디하게 업데이트된 키워드 (Seeds)
    // -------------------------------------------------------------------
    private static final Map<String, String> CATEGORY_TOPICS = new HashMap<>();

    static {
        // 1. Tech (최신 엔진 트렌드, AI 연동, 최적화 중심)
        CATEGORY_TOPICS.put("tech",
                "Godot 4 Architecture vs Unity, LLM powered NPC Dialogue Systems, Unreal Engine 5 Nanite & Lumen Optimization, " +
                        "ECS (Entity Component System) Deep Dive, Rollback Netcode for Indie Games, Procedural Animation & Inverse Kinematics (IK), " +
                        "Shader Graph Magic for Stylized Water, WebGL rendering tricks, Serverless Multiplayer Backend, Data-Oriented Technology Stack (DOTS)");

        // 2. Art (최신 미학 트렌드, 레트로의 귀환, 내러티브 중심 시각화)
        CATEGORY_TOPICS.put("art",
                "PS1 Retro Low-poly Aesthetic, Spider-Verse Style NPR (Non-Photorealistic Rendering), Liminal Space & Environmental Storytelling, " +
                        "Brutalist UI/UX in Modern Games, Solarpunk Color Palettes, Volumetric Lighting and Mood, " +
                        "Diegetic UI in Narrative Games, The Uncanny Valley in Metahumans, Tech-Art: Procedural Weather Systems");

        // 3. Lore (매니아들이 열광하는 장르 융합)
        CATEGORY_TOPICS.put("lore",
                "Analog Horror, Cozy Fantasy but Dark Magic, Neo-Noir Cyberpunk, Solarpunk Dystopia, Mythological Space Opera, " +
                        "SCP Foundation-style Anomalies, Liminal Afterlife, Steampunk Deep Sea Exploration, Alternate History AI Society, " +
                        "Victorian Cosmic Horror");

        // 4. Game Design (신규: 텔테일 스타일 인터랙티브 무비 기획)
        CATEGORY_TOPICS.put("game_design",
                "The Illusion of Choice in Narrative Games, Evolution of QTE (Quick Time Events), Branching Dialogue Tree Architecture, " +
                        "Relationship & Affection Meters, Timed Decisions and Player Panic, Inventory Puzzles vs Dialogue Puzzles, " +
                        "Episodic Pacing & Cliffhangers, Managing Exponential Branching Paths, Morality Systems without Good/Evil");

        // 5. Interactive Story (신규: 텔테일 스타일 스토리 보드/시나리오)
        CATEGORY_TOPICS.put("interactive_story",
                "Zombie Apocalypse with Found Family, Gritty Detective Noir with Anthropomorphic Animals, Betrayal in a Locked-Room Mystery, " +
                        "Time-loop Psychological Thriller, Dystopian Rebellion with a Child Companion, Cosmic Horror on an Abandoned Space Station, " +
                        "A Murder Mystery where the Player is the Killer, Moral Dilemmas with No Right Answer");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("실행 인자(카테고리명)가 필요합니다.");
        String categoryKey = args[0].toLowerCase();

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

        // 3. [수정됨] 셔플 후 메인 주제 딱 '1개'만 선택 (깊이 있는 포스팅을 위해)
        Collections.shuffle(topicList);
        String mainTopic = topicList.get(0);

        System.out.println("Selected Main Topic: " + mainTopic);

        long randomSeed = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();

        // -------------------------------------------------------------------
        // [프롬프트 개선: 1개의 주제에 집중하여 깊이와 가독성을 높임]
        // -------------------------------------------------------------------
        switch (categoryKey) {
            case "tech":
                sb.append("당신은 '실력 있는 시니어 게임 개발자'이자 '솔직하고 위트 있는 1티어 테크 블로거'입니다.\n");
                sb.append("이번 포스팅의 핵심 주제는 **[").append(mainTopic).append("]** 입니다.\n");
                sb.append("--- [Tech 작성 가이드라인] ---\n");
                sb.append("1. **서사적 도입**: 뻔한 교과서적 정의로 시작하지 마세요. 실무에서 겪을 법한 문제 상황이나 최근 트렌드를 짚으며 흥미롭게 시작하세요.\n");
                sb.append("2. **Deep Dive (깊이)**: 겉핥기식 설명은 피하고, 하나의 주제를 집요하게 파고들어 현업 개발자에게 진짜 도움이 되는 인사이트를 제공하세요.\n");
                sb.append("3. **Code & Logic**: 의사코드(Pseudo-code), 핵심 알고리즘 구조, 또는 아키텍처 예시를 반드시 포함해 실용성을 높이세요.\n");
                sb.append("4. **Tone**: 동료 개발자와 커피 한잔하며 썰을 푸는 듯한, 전문적이면서도 살짝 까칠하고 재치 있는 어조를 유지하세요.\n");
                break;

            case "art":
                sb.append("당신은 최신 트렌드를 선도하는 '힙한 게임 아트 디렉터'입니다.\n");
                sb.append("이번 포스팅의 핵심 주제는 **[").append(mainTopic).append("]** 입니다.\n");
                sb.append("--- [Art 작성 가이드라인] ---\n");
                sb.append("1. **시각적 몰입감**: 독자가 게임 화면을 상상할 수 있도록 질감, 렌더링 방식, 광원을 섬세하고 공감각적으로 묘사하세요.\n");
                sb.append("2. **Trend & Insight**: 왜 이 아트 스타일/디자인이 유저의 심리에 타격을 주는지 기획적/미학적 관점에서 깊이 분석하세요.\n");
                sb.append("3. **Tone**: 감각적이고 세련된 어휘를 사용하며, 미학적 분석과 기술적 한계의 타협점을 논하는 프로의 시선을 보여주세요.\n");
                break;

            case "lore":
                sb.append("당신은 방구석 톨킨이자, 설정 짜는 데 미친 '세계관 과몰입 장인'입니다.\n");
                sb.append("다음 주제를 바탕으로 매혹적인 세계관 설정을 작성하세요: **[").append(mainTopic).append("]**\n");
                sb.append("--- [Lore 작성 가이드라인] ---\n");
                sb.append("1. **클리셰 파괴**: 흔한 설정은 버리세요. 독자가 '이런 생각을 어떻게 했지?' 싶을 기괴하거나 독창적인 설정을 디테일하게 만드세요.\n");
                sb.append("2. **절대 규칙**: 이 세계를 관통하는 단 하나의 '절대 규칙'이나 '가혹한 제약'을 설정하고 그로 인해 파생되는 문화를 서술하세요.\n");
                sb.append("3. **Tone**: 다크소울의 아이템 텍스트나 흥미로운 미스터리 소설을 읽는 것처럼 신비롭고 몰입감 있게 작성하세요.\n");
                break;

            case "game_design":
                sb.append("당신은 'The Walking Dead', 'Sam & Max' 같은 텔테일 게임즈 스타일의 인터랙티브 게임 '시니어 기획자'입니다.\n");
                sb.append("이번 포스팅의 핵심 주제는 **[").append(mainTopic).append("]** 입니다.\n");
                sb.append("--- [Game Design 작성 가이드라인] ---\n");
                sb.append("1. **시스템 딥다이브**: 이 기획 요소가 기술적으로, 그리고 게임 디자인적으로 어떻게 작동하는지 상세히 분해하여 설명하세요.\n");
                sb.append("2. **선택과 결과 (Illusion of Choice)**: 이 시스템이 유저에게 어떻게 '선택의 무게감'을 느끼게 하고 감정을 조종하는지 분석하세요.\n");
                sb.append("3. **Tone**: 기획서를 브리핑하는 프로페셔널한 톤이되, '유저를 어떻게 딜레마에 빠뜨릴지' 고민하는 악동 같은 매력을 섞어주세요.\n");
                break;

            case "interactive_story":
                sb.append("당신은 'The Walking Dead' 같은 인터랙티브 무비 게임의 '수석 내러티브 디렉터'입니다.\n");
                sb.append("다음 테마를 바탕으로 게임의 핵심 에피소드 스토리보드를 기획하세요: **[").append(mainTopic).append("]**\n");
                sb.append("--- [Story 작성 가이드라인] ---\n");
                sb.append("1. **로그라인 & 빌드업**: 이 에피소드의 핵심 갈등을 한 줄로 요약하고, 텐션이 올라가는 과정을 서술하세요.\n");
                sb.append("2. **치명적인 딜레마 (The Ultimate Choice)**: 에피소드 클라이맥스에 등장할 '정답이 없는 도덕적 딜레마' 상황을 매우 구체적이고 잔인할 정도로 현실적으로 묘사하세요.\n");
                sb.append("3. **극단적 분기점**: 유저의 선택(Option A vs Option B)에 따라 나비효과처럼 결말이 어떻게 찢어지는지 상세히 서술하세요.\n");
                sb.append("4. **Tone**: 당장이라도 게임 패드를 쥐고 싶어 지도록, 긴장감 넘치고 하드보일드한 분위기로 몰입감 있게 작성하세요.\n");
                break;

            default:
                throw new IllegalArgumentException("정의되지 않은 카테고리입니다.");
        }

        sb.append("\n\n--- [시스템 필수 제약사항] ---\n");
        sb.append("1. **NO HTML**: <div>, <span> 등 태그 절대 사용 금지 (오직 Markdown 문법만 사용).\n");
        sb.append("2. **Language**: 한국어(Korean)로 자연스럽게 작성하되, 가독성을 위해 적절한 소제목(##, ###), 글머리기호, 볼드체를 적극 활용하세요.\n");
        sb.append("3. **IMAGE_PROMPT**: 글의 분위기를 완벽하게 대변하는 고퀄리티 영문 이미지 프롬프트를 작성하세요. (Cinematic lighting, Unreal Engine 5 render, concept art 등 포함).\n");
        sb.append("4. **Random Seed**: ").append(randomSeed).append("\n");
        sb.append("5. **출력 형식(Strict format)**:\n\n");
        sb.append("TITLE: [클릭을 유도하는 매력적이고 자극적인 제목]\n");
        sb.append("IMAGE_PROMPT: [영어 이미지 프롬프트]\n");
        sb.append("BODY:\n");
        sb.append("[본문 내용. 서론/본론/결론이 잘 나뉘어진 깔끔한 마크다운 형태]");

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

        try {
            int textStart = jsonResponse.indexOf("\"text\": \"");
            if (textStart > -1) {
                String temp = jsonResponse.substring(textStart + 9);
                String rawText = temp.split("\"\\s*\\n*\\s*}")[0];
                String unescaped = rawText.replace("\\n", "\n").replace("\\\"", "\"");

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

        String encodedPrompt = URLEncoder.encode(imagePrompt, StandardCharsets.UTF_8);
        int randomSeed = (int)(Math.random() * 10000);
        String imageUrl = "https://image.pollinations.ai/prompt/" + encodedPrompt + "?width=800&height=450&nologo=true&seed=" + randomSeed;
        boolean isImageAvailable = checkImageAvailability(imageUrl);

        ZoneId kstZone = ZoneId.of("Asia/Seoul");
        LocalDate now = LocalDate.now(kstZone);
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
            content.append("
                    ```\n\n");
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
