---
layout: post
title: "Telltale의 서사, AI 행동 트리로 숨결을 불어넣다: 관계 지향적 AI 설계 심층 분석"
categories: tech
---

### ⚠️ Image Generation Failed
```text
Prompt: An abstract, high-tech depiction of a complex, glowing AI behavior tree structure, intricately intertwined with translucent narrative threads that visually represent character relationships and player choices. The scene is bathed in cinematic lighting with deep blues, purples, and neon accents, suggesting a fusion of logic and emotion. Stylized character silhouettes are subtly woven into the narrative threads, surrounded by subtle digital glitches and futuristic UI elements, illustrating the dynamic nature of story-driven AI.
```

여러분, 안녕하세요! 오랜만에 커피 한잔 하면서 기술 수다를 떨어볼까 합니다. 오늘은 살짝 추상적으로 들릴 수 있지만, 현업에서 정말 유용하게 써먹을 수 있는 아주 흥미로운 주제를 들고 왔습니다. 바로 **'AI Behavior Trees'**와 **'Narrative Design Techniques used in Telltale Games'**의 만남이죠. 이 두 키워드가 어떻게 시너지를 낼 수 있을지, 시니어 개발자의 짬에서 나오는 바이브로 풀어볼까 합니다.

### 문제 정의: AI, 왜 맨날 "그 놈이 그 놈" 같을까?

솔직히 말하면, 게임 AI 개발은 늘 고통의 연속입니다. 특히 NPC가 플레이어와 상호작용할 때마다, 미리 정해진 스크립트의 굴레를 벗어나지 못하는 듯한 답답함을 느낄 때가 많죠. 전투 AI는 길 찾기나 공격 패턴으로 그럴듯하게 만들 수 있지만, **"이 NPC가 플레이어의 과거 행동을 기억하고, 그에 따라 감정적으로 반응하며, 심지어는 플레이어와의 관계에 따라 목표나 행동 패턴을 바꾼다"**는 건 말처럼 쉽지 않습니다.

대부분의 AI 행동 트리는 특정 조건(예: "적을 발견했는가?", "체력이 낮은가?")에 따라 미리 정의된 행동을 수행하는 데 초점을 맞춥니다. 이는 효율적이고 예측 가능하지만, 치명적인 단점이 있습니다. 바로 NPC가 **'맥락'**과 **'서사적 무게'**를 이해하지 못한다는 점이죠. 플레이어가 특정 NPC를 구했든, 배신했든, 아니면 그냥 지나쳤든, AI는 그저 "플레이어가 시야에 들어왔으니 인사한다" 혹은 "공격한다" 같은 단순한 로직을 반복할 뿐입니다. 결과적으로 NPC는 스토리에 따라 변화하거나 깊이 있는 관계를 형성하는 존재라기보다는, 그저 정해진 대로 움직이는 '기계'처럼 느껴지기 일쑤죠.

### Telltale의 서사 기술에서 영감을 얻다: 선택과 결과의 심장 박동

여기서 바로 Telltale 게임의 서사 디자인 기법이 우리에게 기가 막힌 영감을 줍니다. Telltale 게임들은 플레이어의 '선택'이 '결과'로 이어지고, 그 결과가 NPC와의 '관계'에 지대한 영향을 미친다는 것을 기가 막히게 구현해냈죠. 물론, 그들의 방식은 주로 철저히 스크립팅된 대화와 이벤트에 의존했습니다. "누구를 구할 것인가?", "어떤 말을 할 것인가?" 같은 순간적인 선택들이 다음 챕터의 특정 대화나 상황을 결정하는 식으로 말이죠.

하지만 핵심은 이겁니다. Telltale은 플레이어가 **'자신의 행동이 세상과 캐릭터들에게 의미 있는 영향을 미친다'**고 느끼게 만드는 데 천재적이었습니다. 비록 그 영향이 거대한 이야기의 흐름을 바꾸기보다는 주로 캐릭터 관계나 작은 서브플롯에 집중되었지만, 그 감성적인 연결고리는 매우 강력했습니다.

자, 여기서 질문 하나 던져볼까요? 우리는 이 **'선택과 결과, 그리고 관계 변화'의 핵심 아이디어**를 Telltale처럼 스크립트에만 의존하지 않고, **AI 행동 트리를 통해 NPC 스스로 이 맥락을 인지하고 자율적으로 반응하게 만들 수는 없을까요?** 저는 충분히 가능하다고 봅니다!

### 해결책: '서사 지향적 행동 트리 (Narrative-Aware Behavior Trees)' 설계

핵심은 AI 행동 트리가 단순히 게임 월드의 물리적 상태(적의 위치, 체력 등)뿐만 아니라, **'서사적 상태(Narrative State)'**를 인지하고 이에 따라 자신의 목표와 행동을 동적으로 변경하도록 만드는 것입니다. 이를 위해 몇 가지 핵심 컴포넌트와 새로운 노드 타입을 행동 트리에 추가해야 합니다.

1.  **Narrative State Manager (NSM)**:
    *   이 친구는 게임의 모든 서사적 정보를 중앙에서 관리하는 허브입니다.
    *   **글로벌 내러티브 플래그 (Global Narrative Flags)**: "플레이어가 아크튜러스를 배신했다", "메인 퀘스트 챕터 3 완료" 등.
    *   **관계 점수 (Relationship Scores)**: `Player`와 `NPC_A` 사이의 `신뢰(Trust)`, `우정(Friendship)`, `경계심(Caution)` 같은 수치.
    *   **과거 플레이어 선택 기록 (Player Choice Log)**: "플레이어가 과거에 NPC_B를 도왔다", "특정 대화에서 A 옵션을 선택했다" 등.
    *   **NPC 개별 특성 (NPC Traits)**: 각 NPC가 가진 고유한 성격이나 가치관(예: "정의로움", "이기적", "겁 많음").

2.  **새로운 행동 트리 노드 & 데코레이터**:
    NSM이 제공하는 정보를 활용할 수 있도록 행동 트리에 새로운 추상화 계층을 추가합니다.

    *   **`Condition: HasNarrativeFlag(flag_name, expected_state)`**: 특정 내러티브 플래그가 활성화되어 있는지 확인합니다.
    *   **`Condition: HasRelationshipScore(target_entity, relationship_type, comparison_operator, threshold)`**: 대상 엔티티와의 특정 관계 점수가 기준치를 만족하는지 확인합니다.
        *   예: `HasRelationshipScore(Player, "Trust", >=, 50)`
    *   **`Decorator: BasedOnRelationship(target_entity, relationship_type, min_threshold, max_threshold, priority_multiplier)`**: 관계 점수에 따라 하위 노드의 우선순위를 동적으로 조절합니다.
    *   **`Action: UpdateRelationshipScore(target_entity, relationship_type, amount)`**: 특정 행동 후 관계 점수를 업데이트합니다. (예: 플레이어가 NPC를 구하면 신뢰도 +10)
    *   **`Action: TriggerNarrativeEvent(event_id)`**: AI의 행동이 특정 서사 이벤트를 발동시킬 수 있습니다.

### 아키텍처 및 의사코드 예시

자, 그럼 이 아이디어가 어떻게 실제 코드에 녹아들 수 있을지 간단한 의사코드와 아키텍처 다이어그램으로 살펴볼까요?

**1. Narrative State Manager (NSM) - 싱글턴 패턴**

```csharp
// C# 유사 코드
public class NarrativeStateManager
{
    private static NarrativeStateManager instance;
    public static NarrativeStateManager Instance
    {
        get
        {
            if (instance == null) instance = new NarrativeStateManager();
            return instance;
        }
    }

    private Dictionary<string, bool> _narrativeFlags;
    private Dictionary<string, Dictionary<string, int>> _relationshipScores; // <source_id, <target_id, score>>

    private NarrativeStateManager()
    {
        _narrativeFlags = new Dictionary<string, bool>();
        _relationshipScores = new Dictionary<string, Dictionary<string, int>>();
        // 초기 플래그 및 관계 점수 로드
    }

    public bool GetFlag(string flagName) => _narrativeFlags.GetValueOrDefault(flagName, false);
    public void SetFlag(string flagName, bool value) => _narrativeFlags[flagName] = value;

    public int GetRelationshipScore(string sourceId, string targetId, string type)
    {
        if (_relationshipScores.ContainsKey(sourceId) && _relationshipScores[sourceId].ContainsKey(targetId + "_" + type))
            return _relationshipScores[sourceId][targetId + "_" + type];
        return 0; // 기본값
    }

    public void UpdateRelationshipScore(string sourceId, string targetId, string type, int change)
    {
        if (!_relationshipScores.ContainsKey(sourceId))
            _relationshipScores[sourceId] = new Dictionary<string, int>();

        string key = targetId + "_" + type;
        _relationshipScores[sourceId][key] = _relationshipScores[sourceId].GetValueOrDefault(key, 0) + change;
        // 특정 임계치 도달 시, 관련 플래그 업데이트 등 추가 로직
        // 예: 신뢰도가 매우 낮아지면 "Player_Betrayed_NPCX" 플래그 활성화
    }
}
```

**2. Narrative-Aware Behavior Tree Snippet**

```
// NPC 'Kael'의 행동 트리 예시
Root
|-- Selector (우선순위: 적대적 -> 위기 상황 도움 -> 일반 상호작용 -> 일상 루틴)
    |-- Sequence (플레이어가 Kael에게 적대적 태도를 보였는가?)
    |   |-- Condition: HasNarrativeFlag("Player_Betrayed_Kael", true) // 플레이어가 Kael을 배신했음
    |   |-- OR
    |   |-- Condition: HasRelationshipScore(Player, "Trust", <=, -20) // 플레이어와의 신뢰도가 매우 낮음
    |   |-- Action: Kael_AvoidPlayer() // 플레이어를 피한다
    |   |-- Action: Kael_GrumbleAboutPlayer() // 플레이어에게 불평한다 (상호작용 시)
    |
    |-- Sequence (플레이어가 위험에 처했고 Kael과의 관계가 좋은가?)
    |   |-- Condition: HasNarrativeFlag("Player_In_Immediate_Danger", true)
    |   |-- Condition: HasRelationshipScore(Player, "Trust", >=, 50) // 플레이어와의 신뢰도가 충분히 높음
    |   |-- Action: Kael_GoToPlayerAndAssist() // 플레이어를 돕기 위해 달려간다
    |   |-- Action: UpdateRelationshipScore(Player, "Friendship", +5) // 도움 후 우정 점수 상승
    |
    |-- Sequence (플레이어가 근처에 있고 일반적인 관계인가?)
    |   |-- Condition: IsPlayerNearby()
    |   |-- Condition: HasNarrativeFlag("Player_Betrayed_Kael", false) // 배신 플래그가 없음
    |   |-- Condition: HasRelationshipScore(Player, "Trust", >, 0) // 신뢰도가 긍정적
    |   |-- Selector (대화 시점의 관계 점수에 따라 다른 인사말 선택)
    |       |-- Condition: HasRelationshipScore(Player, "Friendship", >=, 30) // 우정 높음
    |       |-- Action: Kael_GreetPlayer_Warmly() // 따뜻하게 인사
    |       |-- ELSE (우정 보통)
    |       |-- Action: Kael_GreetPlayer_Normally() // 평범하게 인사
    |
    |-- Action: Kael_PerformDailyRoutine() // 평소 일상 루틴 수행
```

보시다시피, AI는 이제 단순히 물리적 환경에 반응하는 것을 넘어, **플레이어의 '선택'과 그로 인한 '관계 변화'라는 서사적 맥락을 자신의 행동 결정에 반영**하기 시작합니다. Telltale식 서사의 핵심인 '선택과 결과'를 AI의 자율적인 행동 패턴에 녹여내는 것이죠. 플레이어가 특정 NPC를 구했을 때, 그 NPC는 단순히 고맙다는 대사 한마디를 내뱉는 것을 넘어, 실제로 플레이어에게 더 우호적인 태도를 보이거나, 나중에 위험할 때 플레이어를 돕는 등의 '자율적 행동'을 할 수 있게 됩니다.

### 시니어 개발자의 짬바에서 나오는 꿀팁과 고민

제가 이런 시스템을 실제로 적용하면서 느낀 점은, **NPC에게 '가상 인격(Virtual Persona)'을 부여하는 것**이 굉장히 중요하다는 겁니다. 각 NPC에게 "이 NPC는 정의를 중시한다", "이 NPC는 겁이 많다", "이 NPC는 타인의 약점을 이용하려 한다" 같은 특성을 부여하고, 이 특성들이 관계 점수 업데이트나 특정 플래그 반응에 영향을 미치도록 설계하면 훨씬 더 설득력 있는 AI가 탄생합니다.

예를 들어, "정의로움" 특성을 가진 NPC는 플레이어가 부도덕한 행동을 했을 때, `Trust` 점수가 더 크게 떨어지도록 로직을 추가할 수 있습니다. 반면, "이기적" 특성을 가진 NPC는 플레이어가 자신에게 이득을 가져다주는 행동을 했을 때, `Trust` 점수가 크게 오르거나, `Gratitude` 점수가 높아지는 식이죠.

물론, 이 방식은 복잡성을 증가시킵니다.
*   **디버깅의 난이도**: NPC가 왜 그런 행동을 했는지 추적하기가 훨씬 어려워집니다. 좋은 로깅 시스템과 디버그 툴은 필수입니다.
*   **설계의 복잡성**: 모든 NPC의 관계와 특성을 어떻게 관리하고 밸런싱할지 깊은 고민이 필요합니다. 잘못하면 플레이어에게 일관성 없는 경험을 줄 수 있습니다.
*   **성능 문제**: NSM이 너무 많은 정보를 관리하거나, 행동 트리가 너무 자주 복잡한 쿼리를 날리면 성능 저하가 올 수 있습니다.

그럼에도 불구하고, 저는 이 접근 방식이 미래 게임 AI의 중요한 방향이라고 확신합니다. 플레이어가 단순히 스토리를 따라가는 것이 아니라, **자신의 선택이 살아 숨 쉬는 게임 월드와 캐릭터들에게 실제적인 영향을 미치고 있다**고 느끼게 하는 것. 이것이야말로 Telltale이 만들어낸 마법을 AI의 자율성으로 한 차원 더 끌어올리는 방법이 아닐까요?

### 마무리하며

결국, AI Behavior Trees는 단순히 "어떻게 움직일까?"를 넘어, "이 NPC는 지금 '나'를 어떻게 생각하고, 그래서 '나'에게 어떻게 반응해야 할까?"라는 서사적 질문에 답할 수 있는 강력한 도구가 될 수 있습니다. Telltale 게임이 보여준 감동적인 서사의 힘을, 이제는 AI의 손으로 조금 더 역동적이고 유기적으로 만들어낼 때입니다.

여러분도 이 아이디어를 가지고 여러분의 게임에 숨결을 불어넣어 보시길 바랍니다. 분명 플레이어들이 여러분의 NPC를 그저 '코드로 짜인 오브젝트'가 아닌, '살아있는 캐릭터'로 느끼게 될 겁니다. 그럼, 다음 포스팅에서 또 만나요! 😉
