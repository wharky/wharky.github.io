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
    // [설정 영역] 세계관 프레임워크 (이전과 동일)
    // -------------------------------------------------------------------
    private static final Map<String, String> CATEGORY_TOPICS = new HashMap<>();

    static {
        CATEGORY_TOPICS.put("boss",
                "기계와 유기체가 기괴하게 융합된 사이보그형 신성(Divine) 보스, " +
                "특정 감정(공포 슬픔 광기 등)을 먹고 자라난 무형의 코스믹 호러 보스, " +
                "평범한 일상 공간(사무실 학교 놀이터 등) 자체가 하나의 거대한 보스로 변이한 사례, " +
                "플레이어의 기억이나 과거 행동을 모방하고 비틀어버리는 도플갱어형 보스, " +
                "소리나 빛 같은 특정 물리적 요소가 극도로 결핍되거나 과잉된 환경의 지배자");

        CATEGORY_TOPICS.put("fiction",
                "절대 열어서는 안 되는 문턱(Threshold)을 넘어선 관찰자의 마지막 시선, " +
                "너무나도 발전한 기술이 오히려 종교나 미신처럼 변질되어버린 광신적 디스토피아, " +
                "일상적인 소음 속에 숨겨진 인류 멸망의 섬뜩한 메시지, " +
                "우주 한가운데서 발견된 지구와 똑같지만 모든 것이 거울처럼 반전된 장소, " +
                "특정 단어나 개념을 입 밖으로 꺼내는 순간 존재가 지워지는 세계");

        CATEGORY_TOPICS.put("artifact",
                "현대 IT 기기나 일상용품에 깃든 고대 악마의 지독한 저주, " +
                "사용자의 신체 일부나 수명 기억을 대가로 엄청난 편의성을 제공하는 불법 기물, " +
                "절대 파괴할 수 없으나 가만히 두면 주변의 현실(공간/시간)을 붕괴시키는 물건, " +
                "인간의 추억이나 감정을 물리적인 데이터로 추출해 보관하는 기괴한 장치, " +
                "미래에서 온 것으로 추정되지만 현재 인류의 상식으로는 목적을 알 수 없는 도구");

        CATEGORY_TOPICS.put("planet",
                "물리 법칙(중력 시간 빛의 굴절 등)이 지구와 완전히 반대로 혹은 무작위로 작용하는 구역, " +
                "행성 전체가 하나의 거대한 단일 생명체(초개체)로 이루어진 신경망 행성, " +
                "과거에 고도로 발달한 문명이 멸망한 흔적이 끔찍한 형태로 남아있는 데스 월드, " +
                "무기물(금속 유리 플라스틱 등)이 유기체처럼 번식하고 진화하는 기계 생태계, " +
                "관찰자의 무의식이나 공포를 실시간으로 스캐닝하여 환경을 변화시키는 악몽의 행성");

        CATEGORY_TOPICS.put("servicenow",
                "인간의 영혼이나 수명을 화폐로 사용하는 이계 기관의 치명적인 결재 시스템 장애, " +
                "물리적 공간을 초월해 차원 간 포탈을 관리하는 인프라 서버의 기괴한 무한 루프 버그, " +
                "이단 종교 집단이 고대신 소환을 자동화하기 위해 도입한 카탈로그 시스템의 오작동, " +
                "기계 반란을 일으킨 AI들이 자신들의 복지 향상을 위해 제출한 황당한 개선 요청 티켓, " +
                "망자들의 기억을 백업하는 데이터베이스에서 발생한 치명적인 데이터 오염 사태");

        CATEGORY_TOPICS.put("tarot",
                "치명적인 서버 장애나 대규모 데이터 유실 상황을 묵시록적으로 예언하는 파멸적인 점괘, " +
                "뜻밖의 치명적 버그가 오히려 혁신적인 킬러 기능으로 둔갑하게 되는 기적적인 운세, " +
                "야근 크런치 모드 무능한 상사 등 인간관계와 일정에 관한 고통스러운 예언, " +
                "새로운 프레임워크나 언어 도입을 앞두고 겪게 될 끝없는 혼란과 극복의 메시지, " +
                "레거시 코드 속에 잠들어 있던 기괴한 모듈이 깨어나면서 벌어지는 나비효과 운세");

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
        
        StringBuilder sb = new StringBuilder();

        // [가독성 & 대중성 극대화 패치]
        sb.append("당신은 '매니아적인 지식(Nerdiness)'을 갖추었지만, 글은 미치도록 재밌고 쉽게 쓰는 '1티어 스토리텔러 블로거'입니다.\n");
        sb.append("이전에 작성했던 패턴, 흔한 클리셰는 완벽히 배제하고 새로운 상상력을 발휘하여 독창적인 대상을 발명하세요.\n");
        sb.append("이번 글을 관통하는 거대한 테마는 다음과 같습니다: **[").append(mainTopic).append("]**\n\n");

        switch (categoryKey) {
            case "boss":
                sb.append("역할: 게임 기획의 신(神)이자 유쾌한 게임 리뷰어\n");
                sb.append("지시: 위 테마의 기괴한 보스를 기획하되, 독자가 '와 이런 게임 있으면 당장 해보고 싶다'라고 느낄 수 있게 게임 잡지의 리뷰처럼 흥미진진하게 풀어주세요.\n");
                sb.append("구성: 보스의 소름 돋는 첫인상, 플레이어를 미치게 만드는 기믹(1~3페이즈), 역전의 파훼법, 그리고 드롭 아이템.\n");
                sb.append("이미지 프롬프트 지시: 다크 소울 스타일의 기괴한 보스 컨셉 아트 영문 묘사.\n");
                break;

            case "fiction":
                sb.append("역할: 레딧 'NoSleep(괴담)' 게시판의 네임드 작가이자 넷플릭스 '블랙 미러' 시리즈의 천재 각본가\n");
                sb.append("지시: 위 테마를 바탕으로 독자가 숨죽여 읽게 되는 **몰입감 넘치는 기묘한 단편 소설(Short Fiction)**을 작성하세요. 절대 짧게 끝내지 말고, 독자가 상황에 완전히 이입할 수 있도록 분량을 충분히(최소 4~5개 이상의 긴 문단) 할애하여 디테일하게 서술하세요. 평범하고 일상적인 상황에서 시작해 서서히 기괴하게 어긋나는 심리적 공포(불쾌한 골짜기)를 빌드업해야 합니다.\n");
                sb.append("구성: \n");
                sb.append("  1. **[도입]**: 방심하게 만드는 지극히 평범하고 일상적인 시작\n");
                sb.append("  2. **[전개/위기]**: '어? 뭔가 이상한데?' 싶게 서서히 조여오는 기괴한 묘사와 사건의 고조\n");
                sb.append("  3. **[반전/결말]**: 독자의 뒤통수를 치는 소름 돋는 진실이나 여운이 남는 절망적 결말\n");
                sb.append("  4. **[디렉터스 컷(작가의 썰)]**: 이야기 뒤편에 숨겨진 더 끔찍한 설정이나 세계관의 비밀을, 독자에게 커피 한잔하며 썰 풀듯 유쾌하고 친절하게(하지만 내용은 소름 돋게) 덧붙여주세요.\n");
                sb.append("이미지 프롬프트 지시: 글의 클라이맥스나 가장 소름 돋는 오브제를 대변하는 미스터리한 영화적 배경(Cinematic, atmospheric, liminal space) 영문 묘사.\n");
                break;

            case "artifact":
                sb.append("역할: 기괴한 물건들을 리뷰하는 미스터리 유튜버\n");
                sb.append("지시: 위 테마의 기괴한 오버테크놀로지/마도구를 발명하세요. 설정은 SCP처럼 치밀하되, 독자에게는 '오늘 소개할 물건은 진짜 미쳤습니다'하는 식으로 친근하고 몰입감 있게 썰을 풀 듯 설명하세요.\n");
                sb.append("구성: 기물 외형, 압도적인 기능, 충격적인 부작용/저주, 이 물건 때문에 벌어진 어이없거나 무서운 사건.\n");
                sb.append("이미지 프롬프트 지시: 연구소 책상에 놓인 기괴한 물건(Clinical or creepy still life) 영문 묘사.\n");
                break;

            case "planet":
                sb.append("역할: 긍정 마인드를 가진 고독한 우주 탐험가\n");
                sb.append("지시: 위 테마의 행성을 탐사하는 일지를 쓰세요. 상황은 코스믹 호러급으로 절망적이고 기괴하지만, 탐험가 본인은 영화 '마션(The Martian)'의 주인공처럼 재치 있고 생생하게 기록하는 톤을 유지하세요.\n");
                sb.append("구성: 넋이 나가는 행성 환경 묘사, 기상천외한 토착 생물 관찰기, 생존을 위한 개인 음성 로그.\n");
                sb.append("이미지 프롬프트 지시: 압도적인 스케일의 외계 행성 풍경(Alien flora, surreal landscape) 영문 묘사.\n");
                break;

            case "servicenow":
                sb.append("역할: 다차원 우주의 산전수전 다 겪은 시니어 개발자\n");
                sb.append("지시: 위 테마에서 발생한 기상천외한 ITSM 티켓 장애 상황을 발명하세요. 어려운 코딩 용어가 나오더라도 '마법사의 주문'이나 '일상생활'에 찰떡같이 비유해서 일반인도 '아 시스템이 터졌구나'하고 재밌게 읽을 수 있는 장애 해결 썰을 푸세요.\n");
                sb.append("구성: 황당한 장애 접수 내역, 장애 해결을 위한 눈물겨운 솔루션 아키텍처, JavaScript 가상 코드(주석으로 재밌게 해설), 개발자의 넋두리.\n");
                sb.append("이미지 프롬프트 지시: 어두운 서버실에서 모니터 빛을 받는 판타지풍 종족(오크/엘프 등) 개발자 영문 묘사.\n");
                break;

            case "tarot":
                sb.append("역할: 위트 넘치는 IT 타로 마스터\n");
                sb.append("지시: 위 테마의 예언을 담은 가상의 타로 카드를 생성하세요. 비개발자라도 '아 개발자들 저렇게 고통받는구나'하고 낄낄대며 공감할 수 있도록, 맵고 짜지만 유쾌한 운세를 작성하세요.\n");
                sb.append("구성: 재해석된 카드 이름/이미지, 뼈 때리는 오늘의 코딩 운세, 위기를 피하기 위한 현실적인 팁.\n");
                sb.append("이미지 프롬프트 지시: 사이버펑크 네온 스타일로 재해석된 타로 카드 일러스트레이션 영문 묘사.\n");
                break;

            case "uiux":
                sb.append("역할: 유저를 괴롭히는 디자이너를 극딜하는 유쾌한 UX 리뷰어\n");
                sb.append("지시: 위 테마에 맞는 끔찍한 다크 패턴 UI를 발명하세요. 이 UI가 얼마나 악랄하고 기괴한지 밈(Meme)과 드립을 섞어 찰지게 비판하고 리뷰하세요.\n");
                sb.append("구성: 타겟 유저와 악랄한 기획 의도, 치명적인 UX 디테일 3가지(비유를 써서 생생하게 묘사), 베타 테스터들의 분노어린 피드백.\n");
                sb.append("이미지 프롬프트 지시: 글리치 아트가 섞인 기괴하고 초현실적인 웹 인터페이스 영문 묘사.\n");
                break;

            default:
                throw new IllegalArgumentException("정의되지 않은 카테고리입니다.");
        }

        sb.append("\n\n--- [글쓰기 스타일 및 필수 제약사항] ---\n");
        sb.append("1. **가독성 최우선**: 문장은 짧고 호흡이 빨라야 합니다. 한자어나 난해한 학술 용어로 떡칠한 '벽돌 글'은 절대 금지합니다.\n");
        sb.append("2. **찰떡 비유**: 어려운 개념이나 기괴한 설정이 등장하면 독자가 일상에서 겪어봤을 법한 감각이나 상황으로 비유해서 단번에 이해시키세요.\n");
        sb.append("3. **시각적 포맷**: 글이 지루하지 않게 소제목(##, ###), 볼드체(**텍스트**), 인용구(>), 글머리기호를 적재적소에 배치하세요.\n");
        sb.append("4. **NO HTML**: <div>, <span> 등 태그는 절대 금지. 순수 Markdown만 사용.\n");
        sb.append("5. **IMAGE_PROMPT**: 글의 분위기를 대변하는 고퀄리티 영문 이미지 프롬프트를 1줄 작성.\n");
        sb.append("6. **Random Seed**: ").append(randomSeed).append("\n");
        sb.append("7. **출력 형식(Strict format)**:\n\n");
        sb.append("TITLE: [어그로를 확 끄는, 유튜브 썸네일 급의 매력적이고 위트 있는 제목]\n");
        sb.append("IMAGE_PROMPT: [영어 이미지 프롬프트]\n");
        sb.append("BODY:\n");
        sb.append("[서론-본론-결론이 깔끔하게 나뉘어진 재밌고 매끄러운 본문. 인사말이나 맺음말 없이 바로 본론 진입]");

        return sb.toString();
    }

    // [callGemini, savePost, checkImageAvailability 메서드는 완전히 동일하여 생략 (이전 답변 참고)]
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
