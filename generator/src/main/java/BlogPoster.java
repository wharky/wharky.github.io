import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId; // 추가

public class BlogPoster {
    public static void main(String[] args) throws Exception {

        // 0. 한국 시간대 설정 (이게 없으면 GitHub 서버 시간인 UTC로 나옵니다)
        ZoneId kstZone = ZoneId.of("Asia/Seoul");

        // 1. 오늘 날짜 구하기 (한국 시간 기준)
        LocalDate now = LocalDate.now(kstZone);
        String date = now.toString();
        String title = "auto-post-" + date;

        // 경로: 프로젝트 루트에서 실행하므로 _posts/ 바로 접근 (정확함!)
        String fileName = "_posts/" + date + "-" + title + ".md";

        // 2. 마크다운 내용 생성
        StringBuilder content = new StringBuilder();
        content.append("---\n");
        content.append("layout: post\n");
        content.append("title: \"자동 생성된 일지 " + date + "\"\n");
        content.append("---\n\n");
        content.append("## 오늘의 자동 기록\n");

        // 본문 시간도 한국 시간으로 표시
        content.append("이 글은 Java 프로그램에 의해 " + LocalDateTime.now(kstZone) + " (KST)에 생성되었습니다.");

        // 3. 파일 쓰기
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content.toString());
        }
        System.out.println("New post created: " + fileName);
    }
}