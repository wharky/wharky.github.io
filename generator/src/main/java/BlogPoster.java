import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;

public class BlogPoster {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    public static void main(String[] args) throws Exception {
        // 1. 실행 인자 확인
        if (args.length == 0) {
            throw new IllegalArgumentException("실행 인자로 'tech' 또는 'art'가 필요합니다.");
        }
        String category = args[0].toLowerCase();

        // 2. 환경변수 로드
        String apiKey = System.getenv("GAK");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("API Key (GAK)가 환경변수에 없습니다.");
        }

        // 3. 프롬프트 생성
        String prompt = generatePrompt(category);
        System.out.println("Category: " + category);
        System.out.println("Prompt sent to Gemini");

        // 4. API 호출 (헤더 방식 적용)
        String responseText = callGemini(apiKey, prompt);

        // 5. 응답 파싱 및 파일 저장
        // (JSON 파싱 로직은 동일합니다. 응답 구조가 같기 때문입니다.)
        savePost(category, responseText);
    }

    private static String generatePrompt(String category) {
        // 프롬프트는 기존과 동일하게 유지하되, 마크다운 형식을 더 강력하게 요청합니다.
        String baseRequest = "너는 전문 테크니컬 블로거이자 예술 평론가야. 아래 주제에 대해 한국어로 블로그 포스팅을 작성해줘.\n" +
                "반드시 다음 형식을 엄격히 지켜서 출력해.\n\n" +
                "형식:\n" +
                "TITLE: [제목]\n" +
                "BODY:\n" +
                "[본문 내용]\n\n";

        if ("tech".equals(category)) {
            return baseRequest + "주제: 게임 기획, 게임 개발 방법론, 게임 세계관 설정, 레트로 게임의 역사, 인디 게임 개발 팁 중 하나를 랜덤하게 골라서 깊이 있고 전문적인(Tech) 내용을 작성해.";
        } else if ("art".equals(category)) {
            return baseRequest + "주제: 매직 더 개더링(MTG) 일러스트 스타일, 엘든링의 아트워크 분석, TRPG 크툴루 신화의 삽화 스타일, 디지털 페인팅 채색 기법, 작화 스타일 분석 중 하나를 랜덤하게 골라서 예술적이고 감각적인(Art) 내용을 작성해.";
        } else {
            throw new IllegalArgumentException("지원하지 않는 카테고리입니다: " + category);
        }
    }

    private static String callGemini(String apiKey, String prompt) throws IOException, InterruptedException {
        // JSON 생성 (특수문자 이스케이프 처리)
        // Gemini API 규격: { "contents": [{ "parts": [{ "text": "..." }] }] }
        String safePrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n");
        String jsonBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + safePrompt + "\"}]}]}";

        HttpClient client = HttpClient.newHttpClient();

        // [변경 2] 헤더에 API 키 추가 (x-goog-api-key)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey) // 여기가 핵심 변경 사항입니다!
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API 호출 실패 : " + response.statusCode() + " / " + response.body());
        }
        return response.body();
    }

    private static void savePost(String category, String jsonResponse) throws IOException {
        // 응답 파싱 및 파일 저장 로직 (이전과 동일하게 처리하되 통합함)
        String title = "제목 없음";
        String body = "";

        try {
            // 1. JSON에서 "text" 필드 추출 (간이 파싱)
            int textStart = jsonResponse.indexOf("\"text\": \"");
            if (textStart > -1) {
                int start = textStart + 9;
                String temp = jsonResponse.substring(start);
                // 닫는 따옴표 찾기 (단순화: text 필드가 JSON의 마지막 부분에 가깝다고 가정)
                // 실제 응답에는 usageMetadata 등이 뒤에 올 수 있으므로 "}" 전의 따옴표를 찾습니다.
                // 안전하게: 다음 "}" 가 오기 전 마지막 따옴표
                int end = temp.indexOf("\"");
                // 더 안전하게: 이스케이프 된 따옴표(\")가 아닌 진짜 따옴표 찾기 루프가 필요할 수 있으나,
                // Gemini 응답이 깔끔하다는 가정하에 간단히 처리합니다.
                // (만약 복잡해지면 여기서 파싱 에러가 날 수 있으니 유의)

                // 임시 방편: text 필드 뒤에 오는 "role" 이나 닫는 괄호를 기준으로 자릅니다.
                // 보통 text 필드 내용은 길기 때문에 단순 index 찾기는 위험할 수 있습니다.
                // 여기서는 "text": "..." 구조만 추출합니다.

                // 가장 간단한 방법: text 필드 시작부터 끝까지 가져오고 이스케이프 해제
                // (정규식이나 라이브러리 없이 완벽한 JSON 파싱은 어렵지만 시도합니다)
                String rawText = temp.split("\"\\s*\\n*\\s*}")[0]; // 뒷부분 자르기 시도

                // 이스케이프 복원
                String unescaped = rawText.replace("\\n", "\n").replace("\\\"", "\"");

                // 제목/본문 분리
                String[] lines = unescaped.split("\n");
                boolean bodyStarted = false;
                StringBuilder bodyBuilder = new StringBuilder();

                for (String line : lines) {
                    if (line.startsWith("TITLE:") && !bodyStarted) {
                        title = line.replace("TITLE:", "").trim();
                    } else if (line.startsWith("BODY:")) {
                        bodyStarted = true;
                        continue; // BODY: 라인은 제외
                    } else if (bodyStarted) {
                        bodyBuilder.append(line).append("\n");
                    }
                }
                body = bodyBuilder.toString();
                if (body.isEmpty()) body = unescaped; // 파싱 실패 시 원문 저장
            }
        } catch (Exception e) {
            System.err.println("파싱 에러 (내용은 저장됨): " + e.getMessage());
            body = "파싱 실패. 원본 응답: \n" + jsonResponse;
        }

        // 파일 저장 (Jekyll Front Matter)
        ZoneId kstZone = ZoneId.of("Asia/Seoul");
        LocalDate now = LocalDate.now(kstZone);
        String date = now.toString();

        String safeTitle = title.replaceAll("[^a-zA-Z0-9가-힣\\s]", "").replace(" ", "-");
        if (safeTitle.length() > 50) safeTitle = safeTitle.substring(0, 50);
        String fileName = "_posts/" + date + "-" + safeTitle + ".md";

        StringBuilder content = new StringBuilder();
        content.append("---\n");
        content.append("layout: post\n");
        content.append("title: \"" + title.replace("\"", "\\\"") + "\"\n");
        content.append("categories: " + category + "\n");
        content.append("---\n\n");
        content.append(body);

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content.toString());
        }
        System.out.println("Saved: " + fileName);
    }
}