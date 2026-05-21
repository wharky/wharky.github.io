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

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-3.5-flash:generateContent";

    // -------------------------------------------------------------------
    // [설정 영역] 반복 실행에도 중복을 방지하는 거대한 세계관/제약 프레임워크
    // -------------------------------------------------------------------
    private static final Map<String, String> CATEGORY_TOPICS = new HashMap<>();

    static {
        // 1. boss (보스전 기획) - 구체적 이름 대신 '컨셉'과 '기믹의 방향성'을 제공
        CATEGORY_TOPICS.put("boss",
                "기계와 유기체가 기괴하게 융합된 사이보그형 신성(Divine) 보스, " +
                "특정 감정(공포 슬픔 광기 등)을 먹고 자라난 무형의 코스믹 호러 보스, " +
                "평범한 일상 공간(사무실 학교 놀이터 등) 자체가 하나의 거대한 보스로 변이한 사례, " +
                "플레이어의 기억이나 과거 행동을 모방하고 비틀어버리는 도플갱어형 보스, " +
                "소리나 빛 같은 특정 물리적 요소가 극도로 결핍되거나 과잉된 환경의 지배자");

        // 2. fiction (마이크로 픽션) - 사물 대신 '서사적 상황'을 제공
        CATEGORY_TOPICS.put("fiction",
                "절대 열어서는 안 되는 문턱(Threshold)을 넘어선 관찰자의 마지막 시선, " +
                "너무나도 발전한 기술이 오히려 종교나 미신처럼 변질되어버린 광신적 디스토피아, " +
                "일상적인 소음 속에 숨겨진 인류 멸망의 섬뜩한 메시지, " +
                "우주 한가운데서 발견된 지구와 똑같지만 모든 것이 거울처럼 반전된 장소, " +
                "특정 단어나 개념을 입 밖으로 꺼내는 순간 존재가 지워지는 세계");

        // 3. artifact (마도구/아티팩트) - 형태 대신 '저주와 대가'의 프레임워크 제공
        CATEGORY_TOPICS.put("artifact",
                "현대 IT 기기나 일상용품에 깃든 고대 악마의 지독한 저주, " +
                "사용자의 신체 일부나 수명 기억을 대가로 엄청난 편의성을 제공하는 불법 기물, " +
                "절대 파괴할 수 없으나 가만히 두면 주변의 현실(공간/시간)을 붕괴시키는 물건, " +
                "인간의 추억이나 감정을 물리적인 데이터로 추출해 보관하는 기괴한 장치, " +
                "미래에서 온 것으로 추정되지만 현재 인류의 상식으로는 목적을 알 수 없는 도구");

        // 4. planet (행성 탐사) - 환경 대신 '물리 법칙의 이질성' 제공
        CATEGORY_TOPICS.put("planet",
                "물리 법칙(중력 시간 빛의 굴절 등)이 지구와 완전히 반대로 혹은 무작위로 작용하는 구역, " +
                "행성 전체가 하나의 거대한 단일 생명체(초개체)로 이루어진 신경망 행성, " +
                "과거에 고도로 발달한 문명이 멸망한 흔적이 끔찍한 형태로 남아있는 데스 월드, " +
                "무기물(금속 유리 플라스틱 등)이 유기체처럼 번식하고 진화하는 기계 생태계, " +
                "관찰자의 무의식이나 공포를 실시간으로 스캐닝하여 환경을 변화시키는 악몽의 행성");

        // 5. servicenow (ITSM x 판타지) - 기관 대신 '발생한 시스템적 재앙' 제공
        CATEGORY_TOPICS.put("servicenow",
                "인간의 영혼이나 수명을 화폐로 사용하는 이계 기관의 치명적인 결재 시스템 장애, " +
                "물리적 공간을 초월해 차원 간 포탈을 관리하는 인프라 서버의 기괴한 무한 루프 버그, " +
                "이단 종교 집단이 고대신 소환을 자동화하기 위해 도입한 카탈로그 시스템의 오작동, " +
                "기계 반란을 일으킨 AI들이 자신들의 복지 향상을 위해 제출한 황당한 개선 요청 티켓, " +
                "망자들의 기억을 백업하는 데이터베이스에서 발생한 치명적인 데이터 오염 사태");

        // 6. tarot (개발자 타로) - 특정 카드 대신 '점괘의 성격' 제공
        CATEGORY_TOPICS.put("tarot",
                "치명적인 서버 장애나 대규모 데이터 유실 상황을 묵시록적으로 예언하는 파멸적인 점괘, " +
                "뜻밖의 치명적 버그가 오히려 혁신적인 킬러 기능으로 둔갑하게 되는 기적적인 운세, " +
                "야근, 크런치 모드, 무능한 상사 등 인간관계와 일정에 관한 고통스러운 예언, " +
                "새로운 프레임워크나 언어 도입을 앞두고 겪게 될 끝없는 혼란과 극복의 메시지, " +
                "레거시 코드 속에 잠들어 있던 기괴한 모듈이 깨어나면서 벌어지는 나비효과 운세");

        // 7. uiux (다크 패턴) - UI 요소 대신 '사용자를 괴롭히는 심리적 목적' 제공
        CATEGORY_TOPICS.put("uiux",
                "사용자의 개인정보를 넘어 은밀한 기억이나 수치심까지 요구하는 악마적인 입력 폼, " +
                "끝없이 뻗어 나가는 비유클리드 기하학 구조로 설계되어 탈출이 불가능한 네비게이션, " +
                "사용자가 잘못된 클릭을 할 때마다 현실 세계나 하드웨어에 물리적 타격을 주는 피드백, " +
                "너무나도 친절하고 완벽하게 사용자의 의도를 예측해서 오히려 등골이 오싹해지는 불쾌한 인터페이스, " +
                "목적을 달성하기 위해 사용자가 다른 사람을 희생양으로 초대해야만 하는 다단계형 UI");
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
        String allTopicsString = CATEGORY_TOPICS.get(categoryKey);
        String[] topicArray = allTopicsString.split(",");
        List<String> topicList = new ArrayList<>();
        for (String t : topicArray) {
            if (!t.trim().isEmpty()) {
                topicList.add(t.trim());
            }
        }

        Collections.shuffle(topicList);
        String mainTopic = topicList.get(0);
        long randomSeed = System.currentTimeMillis();
        
        System.out.println("Selected Core Framework: " + mainTopic);

        StringBuilder sb = new StringBuilder();

        // [핵심 변경 사항] 매번 다른 글이 나오도록 AI에게 자율성과 '클리셰 배제'를 강력히 지시
        sb.append("당신은 매우 창의적인 작가이자 전문가입니다. 이전에 작성했던 패턴, 흔한 클리셰, 뻔한 전개는 완벽히 배제하세요.\n");
        sb.append("매번 새로운 상상력을 발휘하여 **아무도 예상하지 못한 독창적인 구체적 대상(이름, 설정 등)을 스스로 창조**해야 합니다.\n");
        sb.append("이번 글을 관통하는 거대한 테마 및 프레임워크는 다음과 같습니다: **[").append(mainTopic).append("]**\n");
        sb.append("이 프레임워크 안에서 완전히 새로운 사건, 사물, 시스템을 구체적으로 발명하여 작성하세요.\n\n");

        switch (categoryKey) {
            case "boss":
                sb.append("역할: 하드코어 RPG 수석 기획자 (다크 판타지/코스믹 호러 전문)\n");
                sb.append("지시: 위 테마에 맞는 독창적인 보스의 이름, 기괴한 외형, 전투 페이즈(1~3), 전멸기 파훼법, 저주받은 드롭 아이템을 기획하세요.\n");
                sb.append("이미지 프롬프트 지시: 다크 소울이나 블러드본 스타일의 기괴한 보스 컨셉 아트를 영문으로 묘사하세요.\n");
                break;

            case "fiction":
                sb.append("역할: 휴고상 수상 마이크로 픽션 작가\n");
                sb.append("지시: 위 테마에 맞는 초단편 소설을 작성하세요. 상황만으로 공포와 여운을 줘야 합니다.\n");
                sb.append("구성: 1. 본문 (인용구 `>` 사용, 공백 포함 150~250자), 2. 작가의 노트 (숨겨진 끔찍한 진실 2문장).\n");
                sb.append("이미지 프롬프트 지시: 글의 분위기를 대변하는 스산하고 영화적인 배경(Cinematic, atmospheric)을 영문으로 묘사하세요.\n");
                break;

            case "artifact":
                sb.append("역할: SCP 재단 스타일의 '심연의 감정사'\n");
                sb.append("지시: 위 테마에 부합하는 기괴한 오버테크놀로지/마도구를 발명하여 건조하고 관료적인 어투로 감정 리포트를 쓰세요.\n");
                sb.append("구성: 기물 번호, 외형, 기이한 기능, 치명적 부작용, 최근 사건 리포트.\n");
                sb.append("이미지 프롬프트 지시: SCP 재단의 증거물 사진이나 연구소 책상에 놓인 기괴한 물건(Clinical or creepy still life)을 영문으로 묘사하세요.\n");
                break;

            case "planet":
                sb.append("역할: 심우주를 홀로 탐사하는 고독한 우주 생물학자\n");
                sb.append("지시: 위 테마가 적용된 기괴한 행성을 창조하여 탐사 일지를 쓰세요. 이성을 잃어가는 뉘앙스가 들어가야 합니다.\n");
                sb.append("구성: 행성 환경, 토착 생명체/현상 묘사, 개인 음성 기록 로그.\n");
                sb.append("이미지 프롬프트 지시: H.R. 기거 스타일이나 압도적인 스케일의 외계 행성 풍경(Alien flora, surreal landscape)을 영문으로 묘사하세요.\n");
                break;

            case "servicenow":
                sb.append("역할: 다차원 우주의 ServiceNow 시니어 아키텍트\n");
                sb.append("지시: 위 테마에서 발생할 법한 황당한 고객사 티켓 상황을 발명하고, 실제 ServiceNow 기능(Business Rule 등)으로 트러블슈팅 하세요.\n");
                sb.append("구성: 티켓 개요, 솔루션 아키텍처, JavaScript 가상 코드, 개발자 불평.\n");
                sb.append("이미지 프롬프트 지시: 사이버펑크와 판타지가 섞인 어두운 서버실이나 피곤한 오크/악마 개발자의 모습(Cyberpunk fantasy office)을 영문으로 묘사하세요.\n");
                break;

            case "tarot":
                sb.append("역할: 뒷골목 타로 마스터 겸 만렙 시니어 개발자\n");
                sb.append("지시: 위 테마의 예언을 담고 있는, 개발 용어로 재해석된 가상의 타로 카드를 생성해 오늘의 운세를 쓰세요.\n");
                sb.append("구성: 재해석된 카드 이름과 이미지 묘사, 코딩 운세, 시니어의 조언.\n");
                sb.append("이미지 프롬프트 지시: 사이버펑크 네온 스타일로 재해석된 타로 카드 일러스트레이션(Cyberpunk tarot card design, neon lighting)을 영문으로 묘사하세요.\n");
                break;

            case "uiux":
                sb.append("역할: 악신들을 위해 서비스를 개발하는 미친 UI/UX 디자이너\n");
                sb.append("지시: 위 테마에 맞춰 사용자의 고통을 극대화하는 세상에서 가장 끔찍한 다크 패턴 UI 컴포넌트를 발명하고 리뷰하세요.\n");
                sb.append("구성: 디자인 철학, 치명적 UX 디테일 3가지, 기괴한 베타 테스터 피드백.\n");
                sb.append("이미지 프롬프트 지시: 글리치 아트가 섞인 기괴하고 초현실적인 웹 인터페이스(Surreal interface, glitch art, dark pattern)를 영문으로 묘사하세요.\n");
                break;

            default:
                throw new IllegalArgumentException("정의되지 않은 카테고리입니다.");
        }

        sb.append("\n\n--- [시스템 필수 제약사항] ---\n");
        sb.append("1. **절대 규칙**: 똑같은 이름, 똑같은 전개, 뻔한 결말은 용납되지 않습니다. 텍스트를 생성할 때 무작위성(Entropy)을 극대화하세요.\n");
        sb.append("2. **NO HTML**: <div>, <span> 등 태그 절대 사용 금지 (오직 Markdown 문법만 사용).\n");
        sb.append("3. **Language**: 한국어(Korean)로 자연스럽게 작성하되, 가독성을 위해 적절한 소제목(##, ###), 글머리기호, 볼드체를 적극 활용하세요. 결론을 짓는 진부한 표현(결론적으로~ 등)은 피하세요.\n");
        sb.append("4. **IMAGE_PROMPT**: 앞서 지시한 내용에 맞춰 글의 분위기를 완벽하게 대변하는 고퀄리티 영문 이미지 프롬프트를 작성하세요. (Unreal Engine 5 render, highly detailed 등 품질 수식어 포함).\n");
        sb.append("5. **Random Seed**: ").append(randomSeed).append("\n");
        sb.append("6. **출력 형식(Strict format)**:\n\n");
        sb.append("TITLE: [클릭을 유도하는 매력적이고 자극적인 제목]\n");
        sb.append("IMAGE_PROMPT: [영어 이미지 프롬프트]\n");
        sb.append("BODY:\n");
        sb.append("[본문 내용. 인사말이나 맺음말 없이 바로 본문 시작]");

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
        // [이하 기존 savePost 로직과 동일하여 생략 없이 유지]
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
 
