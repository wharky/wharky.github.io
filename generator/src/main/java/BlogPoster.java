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

                "게임과 개발을 좋아하는 선배가 친근하고 재미있게 설명하는 분위기." // 추가 지침
        ));

        // 2. Art 설정 (범주 확장: 특정 게임 -> 예술 이론 및 시각적 스토리텔링 전반)
        CATEGORY_MAP.put("art", new CategoryConfig(
                // 주제: 게임/판타지 아트의 거시적인 이론과 역사, 미학을 다루도록 키워드 확장
                "Color Psychology in Game Design, The Evolution of Pixel Art to Photorealism, " +
                "Shape Language in Character Design (Round vs Sharp), " +
                "Environmental Storytelling: How Level Design Tells a Story, " +
                "UI/UX Aesthetics: Diegetic vs Non-Diegetic Interfaces, " +
                "The Aesthetics of Horror: Uncanny Valley & Liminal Spaces, " +
                "Fantasy Cartography & Map Making Art, " +
                "Architectural Styles in Video Games (Gothic, Brutalist, Cyberpunk), " +
                "The Philosophy of Lighting and Mood",

                // 톤: 아트 디렉터(Art Director)나 시각 디자인 교수가 강의하는 톤
                "아트 디렉터가 지망생들에게 '보는 법'을 가르쳐주는 듯한 통찰력 있는 톤. " +
                "단순한 감상이 아니라, 시각적 요소가 어떻게 심리적 효과를 내는지 분석적으로 접근.",

                // 지침: 이미지가 없어도 이해되도록 '이론'과 '심리'에 집중
                "작성 시 다음 원칙을 반드시 지키세요:\n" +
                "1. **시각적 묘사보다는 '이론'과 '의도'에 집중**: '무엇이 그려져 있나'보다 '왜 그렇게 디자인했나'를 설명하세요.\n" +
                "2. **보편적인 예시 활용**: 특정 게임 하나만 파고들기보다, 여러 장르를 아우르는 공통적인 법칙(예: 색채 심리학, 조형 언어)을 논하세요.\n" +
                "3. **게이머/창작자 관점**: 이 글을 읽는 독자가 게임을 하거나 TRPG를 할 때, '아, 이게 그래서 이렇게 생겼구나!'라고 깨닫게 만드세요.\n" +
                "4. **이미지 의존도 낮추기**: 텍스트만으로도 충분히 상상력이 자극되도록, 추상적인 개념(공포, 평화, 긴장감)과 시각적 요소의 관계를 서술하세요."
        ));

        // 3. [New] Lore (세계관) 설정
        CATEGORY_MAP.put("lore", new CategoryConfig(
                "High Fantasy, Cyberpunk Dystopia, Steampunk Floating Islands, " +
                "Post-Apocalyptic Overgrown Cities, Deep Sea Civilization, " +
                "Cosmic Horror Space Opera, Subterranean Kingdom, Time-Loop World," +
                "Classic Space Opera TRPG Base",

                "전지전능한 창조주(Creator) 혹은 고대 도서관의 기록관이 비사를 읊어주는 듯한 장엄하고 신비로운 톤. 어느 정도 유머러스함도 있음",

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

        return "당신은 해당 분야에서 가장 존경받는 '전설적인 에디터'이자 '심층 분석가'입니다.\n" +
                "단순한 정보 나열이나 겉핥기 식의 글은 독자들에게 외면받습니다.\n" +
                "아래 [제공된 재료]를 바탕으로, 독자의 지적 호기심을 자극하고 영감을 주는 '마스터피스(Masterpiece)'를 작성하세요.\n\n" +

                "--- [제공된 재료] ---\n" +
                "1. **핵심 키워드(Seeds)**: " + config.topics + "\n" +
                "   (위 키워드 중 1~2개를 선택하거나 창의적으로 연결하여 독창적인 주제를 선정하세요.)\n" +
                "2. **글의 어조(Tone)**: " + config.tone + "\n" +
                "3. **필수 미션**: " + config.instructions + "\n\n" +

                "--- [작성 가이드라인] ---\n" +
                "1. **도발적인 도입부**: 뻔한 정의(Definition)로 시작하지 마세요. 질문을 던지거나, 통념을 깨는 문장으로 시작하여 독자를 사로잡으세요.\n" +
                "2. **깊이 있는 본문(Deep Dive)**:\n" +
                "   - '무엇(What)'보다 **'왜(Why)'**와 **'어떻게(How)'**에 집중하세요.\n" +
                "   - 추상적인 설명 대신, 구체적인 예시(게임 타이틀, 역사적 사건, 기술적 사례)를 반드시 드세요.\n" +
                "   - 필요하다면 비유와 은유를 사용하여 복잡한 개념을 명쾌하게 설명하세요.\n" +
                "3. **구조적인 마크다운**: 긴 글을 읽기 편하게 만드세요.\n" +
                "   - **소제목(##)**을 적절히 배치하여 호흡을 조절하세요.\n" +
                "   - **강조(**굵게**)**, **리스트(-)**, **인용문(>)**을 적극 활용하세요.\n" +
                "   - 기술적인 내용이 있다면 **코드 블록(```)**을 사용하여 전문성을 드러내세요.\n" +
                "4. **여운이 남는 결론**: 단순 요약이 아니라, 독자에게 생각할 거리를 던지거나 행동을 촉구하며 마무리하세요.\n\n" +

                "--- [시스템 제약사항 (엄수)] ---\n" +
                "1. **NO HTML**: <div>, <span>, <font> 등 HTML 태그와 색상 코드를 절대 사용하지 마세요.\n" +
                "2. **IMAGE_PROMPT**: 글의 주제를 가장 상징적으로 보여주는 '예술 작품'을 생성할 수 있도록,\n" +
                "   반드시 **영어(English)**로 작성하세요. (조명, 화풍, 구도, 질감 등을 매우 구체적으로 묘사)\n" +
                "3. **출력 형식 유지**: 아래 형식을 토씨 하나 틀리지 않고 지키세요.\n\n" +

                "TITLE: [여기에 제목]\n" +
                "IMAGE_PROMPT: [여기에 영어 이미지 프롬프트]\n" +
                "BODY:\n" +
                "[여기에 본문 내용]";
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
