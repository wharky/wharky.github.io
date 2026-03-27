---
layout: post
title: "Telltale의 선택 뒤에 숨겨진 TRPG 마법: 코드로 구현하는 몰입형 내러티브 시스템 딥다이브"
categories: tech
---

### ⚠️ Image Generation Failed
```text
Prompt: Abstract representation of complex, interwoven narrative paths, glowing with different colors symbolizing player choices and their consequences. The paths converge and diverge like branches of a digital tree, with subtle lines of pseudo-code shimmering along their edges. In the background, faint, stylized silhouettes of dice and character profiles are visible. Emphasize cinematic lighting, deep shadows, and neon accents against a dark, futuristic backdrop. Digital art, high detail, thought-provoking.
```

자, 오늘의 커피 한 잔과 함께 풀어볼 이야기는 말이죠. 우리가 게임 개발 현장에서 늘 고민하는 '어떻게 플레이어가 자신의 선택에 깊이 몰입하게 만들 것인가'라는 근본적인 질문과 맞닿아 있습니다. 특히, **'Telltale Games의 내러티브 디자인 기법'**은 그 답을 찾는 데 있어 빼놓을 수 없는 중요한 사례죠. 그리고 오늘 저는 여기에 한 발 더 나아가, 이 섬세한 내러티브의 심장부에 **'코드에 구현된 TRPG 룰 시스템'**의 정수가 어떻게 숨 쉬고 있는지, 그리고 우리가 이를 어떻게 실제 개발에 적용할 수 있을지 심층적으로 파고들어 볼까 합니다.

### 문제: '선택'이 '의미'가 되기까지, 그 보이지 않는 벽

대부분의 개발자가 꿈꾸는 것은 플레이어의 선택이 실제로 게임 세계와 스토리에 영향을 미치는, 살아 숨 쉬는 내러티브입니다. 하지만 현실은 녹록지 않죠. 너무 많은 분기점은 개발 비용과 복잡성을 천문학적으로 늘리고, 결국 '선택의 허상(Illusion of Choice)'이라는 비판을 받기 십상입니다.
우리는 고민합니다. "플레이어가 '내 선택이 중요했어!'라고 느끼게 하려면 어떻게 해야 할까?" 그리고 "이 모든 복잡한 내러티브 로직을 어떻게 코드로 효과적으로 관리할 수 있을까?"

Telltale Games는 이 질문에 대한 가장 성공적인 답변 중 하나를 제시했습니다. 그들은 플레이어가 중요하다고 느끼는 순간을 절묘하게 설계하고, 그 순간의 여파를 추적하며 다음 내러티브에 반영하는 데 탁월했습니다. 하지만 이것이 단순히 '복잡한 IF/ELSE' 문으로 이루어졌다고 생각한다면 오산입니다. 그들의 시스템은 마치 숙련된 TRPG 마스터(DM)가 플레이어의 행동과 캐릭터 시트를 바탕으로 즉흥적이면서도 일관된 스토리를 이끌어가는 방식과 놀랍도록 닮아 있습니다.

### Telltale의 내러티브 연금술: 선택의 마법은 어디에서 오는가?

Telltale 게임의 핵심은 세 가지로 요약할 수 있습니다.

1.  **시간제한 선택 (Timed Choices):** 플레이어가 심사숙고할 시간을 주지 않고 감정적으로 중요한 결정을 내리게 만듭니다. 이는 실제 위기 상황에서의 의사결정 과정을 모방하여 몰입감을 극대화합니다.
2.  **'X는 당신의 선택을 기억할 것입니다 (X will remember that)':** 이 문구는 플레이어에게 자신의 결정이 장기적인 결과를 초래할 것이라는 강력한 인상을 심어줍니다. 비록 실제 분기가 생각보다 적을지라도, 이 장치는 플레이어의 행동 하나하나에 무게감을 부여합니다.
3.  **캐릭터 관계 및 내부 플래그 시스템:** Telltale 게임의 진짜 '마법'은 눈에 보이지 않는 곳에서 발동합니다. 각 캐릭터는 플레이어와의 상호작용에 따라 관계 점수(Relationship Score)를 가지고 있으며, 플레이어의 주요 결정은 '내부 플래그(Internal Flags)'를 설정하거나 변경합니다. 이 플래그와 점수들이 다음 에피소드나 특정 장면의 대화 내용, 심지어는 캐릭터의 생사까지 결정하는 중요한 변수로 작용합니다.

이러한 요소들은 단순한 '스크립트'를 넘어, 마치 TRPG의 룰 시스템처럼 정교하게 맞물려 돌아갑니다.

### 코드로 구현하는 TRPG: Telltale 내러티브의 보이지 않는 심장

그렇다면 이 Telltale의 내러티브 기법을 TRPG 룰 시스템의 관점에서 코드로 어떻게 구현할 수 있을까요? 핵심은 **월드 상태(World State)와 조건부 내러티브 진행(Conditional Narrative Progression)**을 체계화하는 것입니다.

**문제:** 플레이어의 선택과 그로 인한 파급 효과를 어떻게 유연하고 확장 가능하며 관리하기 쉬운 형태로 코드로 구현할 것인가?
**해결책:** TRPG의 '캐릭터 시트', '스킬 체크', 'DM의 판단' 개념을 코드로 번역하여, 내러티브 시스템을 하나의 잘 정의된 데이터 기반 규칙 엔진으로 만드는 것입니다.

#### 1. 월드 상태 관리자 (World State Manager): 게임의 '기억' 저장소

TRPG의 '캐릭터 시트'나 '세션 노트'와 같습니다. 모든 내러티브에 중요한 정보는 이곳에 저장됩니다.

```cpp
// Pseudo-code (C# or similar)

public class WorldState
{
    // 특정 이벤트 발생 여부, 캐릭터의 상태 등을 나타내는 플래그
    public Dictionary<string, bool> StoryFlags { get; private set; } = new Dictionary<string, bool>();

    // 플레이어와 NPC 간의 관계 점수 (우호도, 신뢰도 등)
    public Dictionary<string, int> CharacterRelationships { get; private set; } = new Dictionary<string, int>();

    // 기타 중요한 전역 변수 (예: 보유 아이템, 진행된 서브 퀘스트 등)
    public Dictionary<string, object> GlobalVariables { get; private set; } = new Dictionary<string, object>();

    public void SetFlag(string flagName, bool value) => StoryFlags[flagName] = value;
    public bool GetFlag(string flagName) => StoryFlags.TryGetValue(flagName, out bool value) ? value : false;

    public void AdjustRelationship(string characterId, int amount)
    {
        if (!CharacterRelationships.ContainsKey(characterId))
            CharacterRelationships[characterId] = 0;
        CharacterRelationships[characterId] += amount;
    }
    public int GetRelationship(string characterId) => CharacterRelationships.TryGetValue(characterId, out int score) ? score : 0;
}
```
`WorldState`는 게임의 모든 '기억'을 저장하는 핵심입니다. Telltale의 "X will remember that"은 곧 `StoryFlags`나 `CharacterRelationships`의 변경으로 이어집니다.

#### 2. 내러티브 노드 & 선택지 (Narrative Node & Choice): DM의 시나리오 카드

각 대화나 장면은 하나의 '내러티브 노드'이며, 플레이어가 할 수 있는 행동은 '선택지'로 정의됩니다.

```cpp
// Pseudo-code

public class Choice
{
    public string ChoiceText { get; set; }
    public string NextNodeId { get; set; } // 이 선택 후 다음으로 이어질 노드 ID

    public List<Condition> Conditions { get; set; } = new List<Condition>(); // 이 선택지를 활성화/비활성화하는 조건
    public List<Consequence> Consequences { get; set; } = new List<Consequence>(); // 이 선택으로 인해 발생하는 결과
}

public class NarrativeNode
{
    public string NodeId { get; set; }
    public string PromptText { get; set; } // 플레이어에게 보여줄 대화나 상황 설명
    public List<Choice> Choices { get; set; } = new List<Choice>();
    public bool IsTimed { get; set; } // 시간제한 선택 여부
    public float TimeLimitSeconds { get; set; } // 시간 제한
}
```
여기서 `Conditions`는 TRPG의 '스킬 체크'나 '능력치 요구사항'과 같습니다. 특정 선택지를 고르려면 특정 플래그가 참이어야 하거나, 특정 NPC와의 관계 점수가 일정 이상이어야 하는 식이죠. `Consequences`는 선택의 결과로 월드 상태가 어떻게 변경될지 정의합니다.

#### 3. 조건 (Condition): TRPG의 '스킬 체크' 로직

선택지가 활성화될 수 있는지 여부를 판별합니다. 마치 DM이 "지혜 체크 12 이상!"이라고 요구하는 것과 같습니다.

```cpp
// Pseudo-code

public class Condition
{
    public enum ConditionType { HasFlag, MinRelationship, MaxRelationship, CustomCheck }
    public ConditionType Type { get; set; }
    public string TargetId { get; set; } // 플래그 이름, 캐릭터 ID 등
    public object Value { get; set; } // 플래그 값, 관계 점수 임계값 등

    public bool Evaluate(WorldState worldState)
    {
        switch (Type)
        {
            case ConditionType.HasFlag:
                return worldState.GetFlag(TargetId) == (bool)Value;
            case ConditionType.MinRelationship:
                return worldState.GetRelationship(TargetId) >= (int)Value;
            case ConditionType.MaxRelationship:
                return worldState.GetRelationship(TargetId) <= (int)Value;
            // ... 추가 조건 타입
            default: return true;
        }
    }
}
```
플레이어가 특정 대화 선택지를 고를 때, 시스템은 백그라운드에서 `Condition`을 `Evaluate`하여 선택지를 보여줄지 말지, 혹은 선택했을 때 성공/실패 메시지를 다르게 보여줄지 결정합니다. 이것이 바로 Telltale이 플레이어에게 인지시키지 않고 내러티브를 조작하는 한 방법입니다.

#### 4. 결과 (Consequence): DM의 '판단'을 코드로

플레이어의 선택으로 인해 월드 상태가 어떻게 변할지 정의합니다. "그대의 설득이 성공하여, 마을 사람들이 그대를 신뢰하게 되었다!"를 코드로 나타내는 것이죠.

```cpp
// Pseudo-code

public class Consequence
{
    public enum EffectType { SetFlag, AdjustRelationship, TriggerEvent, CustomAction }
    public EffectType Type { get; set; }
    public string TargetId { get; set; } // 플래그 이름, 캐릭터 ID, 이벤트 ID 등
    public object Value { get; set; } // 변경될 값, 이벤트 데이터 등

    public void Apply(WorldState worldState)
    {
        switch (Type)
        {
            case EffectType.SetFlag:
                worldState.SetFlag(TargetId, (bool)Value);
                break;
            case EffectType.AdjustRelationship:
                worldState.AdjustRelationship(TargetId, (int)Value);
                break;
            case EffectType.TriggerEvent:
                // 특정 인게임 이벤트 트리거 (예: UI 메시지, 컷씬 등)
                // EventBus.Publish(TargetId, Value);
                break;
            // ... 추가 결과 타입
        }
    }
}
```
이 `Consequence`를 통해 Telltale의 "X will remember that"이 실제 게임 로직에 반영됩니다. 단순한 문구가 아니라, `WorldState`의 영구적인 변경을 의미하는 것이죠.

#### 5. 내러티브 엔진 (Narrative Engine): DM 역할 수행

이 모든 요소를 결합하여 플레이어의 선택을 처리하고 다음 내러티브로 진행시키는 핵심 로직입니다.

```cpp
// Pseudo-code

public class NarrativeEngine
{
    private WorldState _worldState = new WorldState();
    private Dictionary<string, NarrativeNode> _nodes = new Dictionary<string, NarrativeNode>();
    private NarrativeNode _currentNode;

    public void LoadNarrativeData(string jsonPath)
    {
        // JSON 등 외부 파일에서 NarrativeNode와 Choice 데이터 로드
        // _nodes 딕셔너리에 채워넣기
    }

    public void StartGame(string initialNodeId)
    {
        _currentNode = _nodes[initialNodeId];
        DisplayCurrentNode();
    }

    private void DisplayCurrentNode()
    {
        // UI에 _currentNode.PromptText 출력
        // 활성화 가능한 선택지만 필터링하여 UI에 표시
        List<Choice> availableChoices = _currentNode.Choices
                                                 .Where(c => c.Conditions.All(cond => cond.Evaluate(_worldState)))
                                                 .ToList();
        // UI에 availableChoices 출력 (시간 제한 처리 포함)
    }

    public void HandlePlayerChoice(Choice chosen)
    {
        // 1. 결과 적용 (월드 상태 업데이트)
        foreach (var consequence in chosen.Consequences)
        {
            consequence.Apply(_worldState);
        }

        // 2. 다음 내러티브 노드로 이동
        if (_nodes.TryGetValue(chosen.NextNodeId, out NarrativeNode nextNode))
        {
            _currentNode = nextNode;
            DisplayCurrentNode();
        }
        else
        {
            // 게임 종료 또는 오류 처리
            Debug.LogError($"NextNodeId '{chosen.NextNodeId}' not found!");
        }
    }
}
```
이 `NarrativeEngine`은 TRPG에서 DM이 플레이어의 행동을 듣고, 룰북을 참고하여 (Conditions 평가), 결과에 따라 캐릭터 시트를 업데이트하고 (Consequences 적용), 다음 장면으로 넘어가는 (NextNodeId로 이동) 모든 과정을 코드로 구현한 것입니다.

### Opnionated: 선택의 허상을 넘어서는 현실적인 마법

많은 개발자가 Telltale 게임을 '선택의 허상'이라고 비판하곤 합니다. 하지만 저는 그들의 가장 큰 마법이 바로 이 '허상'을 현실처럼 느끼게 만드는 기술이라고 생각합니다. 무한한 분기를 만드는 것은 불가능하지만, 플레이어의 중요한 결정을 몇 개의 핵심 플래그와 관계 점수로 응축하여 이후 내러티브에 의미 있는 영향을 주도록 설계하는 것. 이것이야말로 TRPG의 숙련된 DM이 '레일로드'를 깔면서도 플레이어에게 깊은 자유와 영향력을 느끼게 하는 방식과 같습니다.

핵심은 모든 선택이 거대한 분기를 만들 필요는 없다는 것입니다. 어떤 선택은 대화의 톤을 바꾸거나, 특정 캐릭터의 감정을 미묘하게 변화시키거나, 나중에 작은 보상이나 페널티로 돌아오는 것으로 충분합니다. 중요한 건 플레이어가 '내 선택이 중요했다'고 느끼는 그 순간의 경험입니다. 그리고 이러한 경험은 위에서 설명한 TRPG 룰 시스템 기반의 내러티브 코딩을 통해 충분히 구현 가능합니다.

### 개발자를 위한 실용적인 조언: 당신의 내러티브를 코드로 길들이는 법

1.  **내러티브를 '데이터'로 생각하세요:** 모든 대화, 선택지, 조건, 결과는 코드 내부에 하드코딩하기보다 JSON, XML, 또는 커스텀 스크립팅 언어 등을 통해 외부 데이터로 관리해야 합니다. 이는 디자이너가 직접 내러티브를 수정하고 테스트할 수 있게 하여 개발 효율을 극대화합니다.
2.  **월드 상태는 가장 중요한 '캐릭터 시트'입니다:** `WorldState` 클래스를 최대한 유연하고 확장 가능하게 설계하세요. 플래그, 관계 점수 외에도 시간의 흐름, 날씨, 지역별 상태 등 다양한 요소를 추적할 수 있도록 준비해 두는 것이 좋습니다.
3.  **'조건'과 '결과'를 명확히 정의하세요:** TRPG의 룰북처럼, 어떤 조건이 만족되었을 때 어떤 결과가 발생할지 명확하게 정의해야 합니다. 이는 내러티브 로직의 예측 가능성을 높이고 버그를 줄이는 데 도움이 됩니다.
4.  **'선택의 무게'를 조절하세요:** 모든 선택이 거대한 분기를 만들 필요는 없습니다. 어떤 선택은 `CharacterRelationships`만 조절하고, 어떤 선택은 `StoryFlags`를 크게 변경하여 이후 큰 줄기에 영향을 미치도록 설계하세요. Telltale처럼 중요한 순간에만 "X will remember that"을 보여주는 것도 좋은 전략입니다.

### 마무리하며

Telltale Games의 내러티브 디자인은 단순히 멋진 스토리를 들려주는 것을 넘어, 플레이어가 스토리를 '만들어간다'고 느끼게 하는 기술적 비법이 숨어 있습니다. 그리고 그 비법의 뿌리에는 TRPG의 룰 시스템이 코드로 정교하게 구현되어 있다는 것을 알 수 있습니다.
우리가 TRPG의 DM처럼 플레이어의 행동을 관찰하고, 룰북에 따라 결과를 판정하며, 다음 이야기를 이끌어가는 시스템을 코드로 구축한다면, 단순한 '선택지 게임'을 넘어 플레이어에게 진정으로 몰입감 있는 경험을 선사할 수 있을 겁니다. 여러분의 다음 프로젝트에서 이 깊은 통찰이 영감이 되기를 바라며, 오늘의 이야기를 마칩니다!

다음 포스팅에서 또 만나요!
