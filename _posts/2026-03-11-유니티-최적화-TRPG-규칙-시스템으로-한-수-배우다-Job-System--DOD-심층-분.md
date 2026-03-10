---
layout: post
title: "유니티 최적화, TRPG 규칙 시스템으로 한 수 배우다: Job System & DOD 심층 분석"
categories: tech
---

### ⚠️ Image Generation Failed
```text
Prompt: Abstract, highly detailed, cinematic lighting, a complex web of interconnected nodes representing game rules and data flow, glowing lines indicating optimization pathways, digital magic, cyberpunk aesthetic with subtle fantasy elements, Unity engine logo subtly integrated into the network, deep blues and purples with bright electric accents, focus on intricate logic and performance, artstation trending.
```

안녕하세요, 동료 개발자 여러분! 오늘도 코드 한 줄에 영혼을 갈아 넣고 계실 여러분을 위해, 실력 있는 시니어 개발자이자 영원한 테크 블로거, 제가 다시 찾아왔습니다. 오늘의 커피챗 주제는 다름 아닌 **['Unity Engine Optimization Tricks' AND 'TRPG Rule Systems implemented in Code']** 이 두 키워드의 절묘한 만남입니다.

"TRPG 규칙 시스템이 유니티 최적화와 무슨 상관이 있느냐?" 하고 고개를 갸웃거릴 분도 계실 겁니다. 하지만 깊이 파고들면 이 둘은 떼려야 뗄 수 없는 관계라는 걸 알게 되실 거예요. TRPG 규칙 시스템은 본질적으로 복잡한 상태 변화, 조건부 로직, 그리고 수많은 계산을 동반합니다. 이걸 유니티 게임 안에 녹여낼 때, 제대로 설계하지 않으면 프레임 드랍과 GC 스파이크의 주범이 되기 십상이죠. 오늘은 이 '복잡한 규칙 시스템'이라는 만만찮은 녀석을 유니티 안에서 어떻게 하면 효율적이고 우아하게 다룰 수 있을지, 제 경험과 함께 실무적인 팁들을 공유해 보려 합니다.

### 문제: 턴마다 프레임을 잡아먹는 규칙 엔진, 이대로 괜찮은가?

상상해 보세요. 여러분이 개발하는 RPG 게임에 플레이어의 스탯, 아이템 효과, 스킬 쿨타임, 몬스터의 행동 패턴, 환경 디버프 등 수십, 수백 가지의 TRPG에서 영감을 받은 규칙들이 실시간으로 작동하고 있습니다. 각 턴이 시작되거나, 캐릭터가 행동할 때마다 이 모든 규칙을 순차적으로 평가해야 합니다.

초기에는 오브젝트 지향적으로 멋지게 설계했다고 자부할지도 모릅니다. `Character` 클래스는 `MonoBehaviour`를 상속받고, 각 `Buff`나 `Debuff`는 또 다른 `MonoBehaviour` 컴포넌트로 붙어 자신의 로직을 `Update()`나 이벤트 리스너를 통해 처리하죠.
하지만 몬스터 떼거리가 몰려나오고, 파티원들이 스킬을 난사하며, 화면 가득 이펙트가 터져 나올 때... 어느 순간 게임은 '슬로우 모션'이 되어버립니다. CPU 프로파일러를 돌려보면, 특정 `Update()` 메서드나 이벤트 처리 로직에서 예상치 못한 시간을 잡아먹고 있고, GC Alloc이 튀어 오르는 것을 발견하게 될 겁니다.

**이게 바로 문제입니다.** TRPG 규칙은 그 자체로 방대한 정보와 복잡한 연산을 요구합니다. 이러한 시스템을 유니티의 전통적인 OOP (Object-Oriented Programming) 패러다임에 맞춰 `MonoBehaviour` 중심으로 구현할 경우, 다음과 같은 병목 현상이 발생하기 쉽습니다:

1.  **캐시 미스 (Cache Misses) 증대**: 관련 데이터가 메모리 곳곳에 흩어져 있어 CPU 캐시 효율이 떨어집니다.
2.  **GC 스파이크 (Garbage Collection Spikes)**: 규칙 평가 과정에서 임시 객체나 컬렉션이 빈번하게 생성되어 가비지 컬렉터가 수시로 작동합니다.
3.  **메인 스레드 병목**: 모든 중요한 규칙 평가가 메인 스레드에서 순차적으로 이루어져 프레임 타임을 늘립니다.
4.  **과도한 추상화 및 런타임 오버헤드**: 불필요한 가상 함수 호출이나 리플렉션 등으로 인한 오버헤드가 누적됩니다.

마치 던전 마스터가 50명의 플레이어와 100마리의 몬스터 각각의 모든 스탯 변화와 버프/디버프를 실시간으로 주판 튕겨가며 계산하려 하는 것과 같습니다. 비효율적이죠.

### 해결책: TRPG 규칙 엔진을 고성능 유니티 코드로 재설계하다

자, 그럼 이 복잡한 규칙 시스템을 어떻게 유니티의 성능 친화적인 방식으로 전환할 수 있을까요? 핵심은 **데이터 중심 설계(Data-Oriented Design, DOD)**와 **유니티 Job System 및 Burst Compiler**의 적극적인 활용입니다.

#### 1. 데이터를 Rule의 뼈대로 삼다: Data-Oriented Thinking

우선 규칙 시스템을 바라보는 관점부터 바꿔야 합니다. '행동'보다는 '데이터'에 집중하는 거죠. TRPG 규칙은 결국 어떤 데이터(캐릭터 스탯, 스킬 레벨 등)를 입력받아, 특정 조건(HP < 50%, 특정 버프 보유 등)을 만족하면, 또 다른 데이터(대미지 증가, 상태 이상 적용 등)를 출력하는 과정입니다.

**Problem**: `MonoBehaviour`를 상속받는 `Character` 클래스 안에 모든 스탯과 버프/디버프 로직이 섞여 있고, 각 `Buff` 인스턴스가 `Update()`를 통해 자신의 로직을 처리하는 방식은 데이터를 흩어지게 만듭니다.
**Solution**: 규칙 평가에 필요한 핵심 데이터를 평범한 C# `struct`나 `ScriptableObject`를 활용하여 콤팩트하게 관리합니다. 로직과 데이터를 분리하여, 로직은 데이터를 순회하며 처리하는 형태로 변경합니다.

**의사 코드: 규칙 평가를 위한 데이터 구조**

```csharp
// 캐릭터의 핵심 스탯을 나타내는 struct
public struct CharacterRuntimeData
{
    public int EntityID; // 고유 ID
    public float CurrentHP;
    public float MaxHP;
    public int Strength;
    public int Dexterity;
    public int Intelligence;
    // ... 기타 스탯 및 상태
    public uint ActiveEffectMask; // 현재 활성화된 효과를 비트마스크로 관리
    public float[] EffectDurations; // 활성화된 효과별 남은 시간 (인덱스 매핑)
}

// 스킬/버프/디버프 등의 규칙 정의 (ScriptableObject로 관리)
[CreateAssetMenu(fileName = "NewRule", menuName = "TRPG/Rule Definition")]
public class RuleDefinition : ScriptableObject
{
    public string RuleName;
    public RuleConditionType Condition; // 특정 스탯, 버프 유무 등
    public RuleEffectType Effect;       // 대미지, 회복, 스탯 변화 등
    public float EffectValue;
    public int DurationTurns; // 턴 기반 게임에서 지속 턴 수
    // ... 필요에 따라 더 복잡한 파라미터들
}

// 모든 캐릭터의 데이터를 NativeArray로 관리 (Job System과 연동 용이)
NativeArray<CharacterRuntimeData> allCharactersData;
// 모든 활성화된 규칙 정의 (읽기 전용으로 NativeArray/HashSet 등 사용)
NativeArray<RuleDefinition> allActiveRules;
```

이렇게 데이터를 구조화하면, 규칙 평가 로직은 이 `NativeArray`를 순회하며 필요한 계산을 수행할 수 있게 됩니다. 데이터를 연속적인 메모리 공간에 배치함으로써 CPU 캐시 효율을 극대화할 수 있죠.

#### 2. 규칙 평가를 Job으로 묶어 던지다: Unity Job System & Burst Compiler

TRPG 규칙 시스템의 핵심은 많은 엔티티(캐릭터, 몬스터 등)에 대해 거의 동일한, 그러나 독립적인 계산을 반복적으로 수행한다는 점입니다. 이는 Unity Job System의 `IJobParallelFor`와 `Burst Compiler`를 위한 완벽한 시나리오입니다!

**Problem**: 메인 스레드에서 수십/수백 개의 캐릭터에 대한 규칙 평가를 순차적으로 처리하면 프레임 드랍이 발생합니다.
**Solution**: 각 캐릭터 또는 특정 규칙 그룹에 대한 평가 로직을 Job으로 캡슐화하고, 이를 Unity Job System을 통해 멀티 스레드로 병렬 처리합니다. `Burst Compiler`는 이 Job들을 놀라울 정도로 최적화된 네이티브 코드로 변환하여 CPU 연산 속도를 비약적으로 향상시킵니다.

**의사 코드: Job System을 활용한 규칙 평가**

```csharp
using Unity.Collections;
using Unity.Jobs;
using Unity.Burst;
using UnityEngine;

// Burst 컴파일을 위한 Job Struct
[BurstCompile]
public struct ProcessCharacterRulesJob : IJobParallelFor
{
    // 읽기/쓰기 접근이 필요한 데이터는 `ref` 키워드를 사용하거나, NativeArray를 ReadWrite로 전달.
    // 여기서는 ReadWrite 접근이 필요한 CharacterRuntimeData를 NativeArray로 받습니다.
    public NativeArray<CharacterRuntimeData> CharacterData;

    // 규칙 정의 데이터는 읽기 전용으로 받습니다.
    [ReadOnly] public NativeArray<RuleDefinitionData> RuleDefinitions; // RuleDefinition을 Burst 호환 struct로 변환했다고 가정

    // 현재 턴, 시간 등 규칙 평가에 필요한 전역 상태
    public int CurrentTurn;
    public float DeltaTime;

    // IJobParallelFor의 Execute 메서드: 각 스레드에서 특정 인덱스(캐릭터)에 대한 작업 수행
    public void Execute(int index)
    {
        // 각 캐릭터의 데이터 복사 (또는 ref로 직접 접근)
        CharacterRuntimeData character = CharacterData[index];

        // 1. 캐릭터 상태 업데이트 (예: 버프/디버프 지속 턴 감소)
        UpdateEffectDurations(ref character);

        // 2. 활성화된 모든 규칙을 순회하며 평가
        foreach (var rule in RuleDefinitions)
        {
            // 이 캐릭터가 해당 규칙의 조건을 만족하는가?
            if (EvaluateCondition(ref character, rule, CurrentTurn))
            {
                // 규칙의 효과를 적용
                ApplyEffect(ref character, rule);
            }
        }

        // 3. 변경된 캐릭터 데이터를 다시 NativeArray에 저장
        CharacterData[index] = character;
    }

    // 조건 평가 로직 (Burst friendly하게 작성)
    private bool EvaluateCondition(ref CharacterRuntimeData character, in RuleDefinitionData rule, int currentTurn)
    {
        // ... 실제 조건 평가 로직 (스탯 비교, 비트마스크 체크 등)
        return true; // 예시
    }

    // 효과 적용 로직 (Burst friendly하게 작성)
    private void ApplyEffect(ref CharacterRuntimeData character, in RuleDefinitionData rule)
    {
        // ... 실제 효과 적용 로직 (HP 감소, 스탯 증가 등)
    }

    // 버프/디버프 지속 시간 업데이트 로직
    private void UpdateEffectDurations(ref CharacterRuntimeData character)
    {
        // ...
    }
}

// 메인 스레드에서 Job 스케줄링 및 완료 대기
public class RuleEngineManager : MonoBehaviour
{
    private NativeArray<CharacterRuntimeData> _characterData;
    private NativeArray<RuleDefinitionData> _ruleDefinitions; // ScriptableObject에서 Burst 호환 struct로 변환 후 사용

    void Start()
    {
        // 데이터 초기화 (예: 캐릭터 1000개, 규칙 50개)
        _characterData = new NativeArray<CharacterRuntimeData>(1000, Allocator.Persistent);
        _ruleDefinitions = new NativeArray<RuleDefinitionData>(50, Allocator.Persistent);
        // ... 데이터 채우기
    }

    void OnDestroy()
    {
        _characterData.Dispose();
        _ruleDefinitions.Dispose();
    }

    public void ProcessAllRulesForTurn(int currentTurn)
    {
        ProcessCharacterRulesJob job = new ProcessCharacterRulesJob
        {
            CharacterData = _characterData,
            RuleDefinitions = _ruleDefinitions,
            CurrentTurn = currentTurn,
            DeltaTime = Time.deltaTime // 필요한 경우
        };

        // Job 스케줄링. 캐릭터 수만큼 병렬 처리
        JobHandle handle = job.Schedule(_characterData.Length, 64); // 64개 단위로 배치 처리
        
        // Job 완료 대기 (필요하다면 다른 작업과 병렬로 수행 후 나중에 완료 대기)
        handle.Complete(); 

        // _characterData에 변경된 결과가 반영되어 있음.
        // 이제 이 데이터를 활용하여 게임 로직 업데이트
    }
}
```

이 방식의 장점은 명확합니다. 수백, 수천 개의 캐릭터에 대한 규칙 평가가 CPU 코어 수에 따라 거의 선형적으로 빨라집니다. 또한 Burst Compiler가 생성하는 극도로 최적화된 코드는 C++에 버금가는 성능을 자랑하죠.

**시니어 개발자의 잔소리**: `RuleDefinition` 자체를 `ScriptableObject`로 만들었다면, `Job` 안에서는 직접 사용할 수 없습니다. `ScriptableObject`는 Managed Heap에 존재하고, Job System은 Native Memory에서 작동하기 때문이죠. 따라서 `RuleDefinition`에서 `Burst` 호환 가능한 `struct` 타입의 `RuleDefinitionData`를 추출하여 `NativeArray`에 복사해서 사용하는 전처리 과정이 필요합니다. 이거 놓치면 나중에 골치 아픕니다!

#### 3. GC를 피하는 현자의 지혜: Allocation-Free Rule Execution

Job System과 DOD가 성능의 양대 산맥이라면, GC 스파이크는 그 틈을 노리는 독사 같은 존재입니다. 규칙 평가 과정에서 `new List<T>()`나 `string` 연산, LINQ 쿼리 등을 남발하면, 아무리 Job System으로 빠르게 돌려도 결과적으로 GC가 게임을 뚝뚝 끊기게 만들 수 있습니다.

**Problem**: 규칙 평가 도중 발생하는 임시 객체 할당 (`new List<T>`, `string` 빌더 등)이 GC 스파이크를 유발합니다.
**Solution**: 할당을 최소화하거나 완전히 없애는 방향으로 코드를 작성합니다.
*   **`NativeArray<T>` 또는 `Span<T>` 활용**: 임시 컬렉션이 필요하다면 `NativeArray`를 미리 할당해 재사용하거나, 스택에 할당되는 `Span<T>`를 적극 활용합니다.
*   **객체 풀링 (Object Pooling)**: 재사용 가능한 객체들을 미리 만들어 두었다가 필요할 때 꺼내 쓰고, 다 쓴 후에는 반환하는 패턴을 적용합니다. 특히 규칙 평가 결과로 생성되는 `EffectInstance` 같은 객체에 유용합니다.
*   **`StringBuilder` 사용**: `string` 연산이 필요할 경우, `+` 연산자 대신 `StringBuilder`를 사용합니다. (다만 Job 안에서는 `StringBuilder` 사용에 제약이 있을 수 있으니 주의)
*   **LINQ 자제**: LINQ는 편리하지만, 대부분의 경우 숨겨진 할당을 일으킵니다. 성능이 중요한 코드에서는 직접 루프를 돌리는 것이 좋습니다.

```csharp
// (Job 내부 혹은 Job이 처리할 데이터를 준비하는 메인 스레드 로직)
// 잘못된 예시: 매번 새로운 리스트 할당
// List<CharacterRuntimeData> affectedCharacters = allCharactersData.ToList().Where(c => c.CurrentHP < 100).ToList();

// 좋은 예시: NativeArray와 Job을 활용하거나, 미리 할당된 NativeList/NativeQueue 등 사용
// ProcessCharacterRulesJob 내에서 직접 조건을 확인하고 처리
// 또는 메인 스레드에서 특정 조건을 만족하는 ID만 NativeArray로 미리 뽑아 Job에 전달
public struct FindLowHPCharactersJob : IJobParallelFor
{
    [ReadOnly] public NativeArray<CharacterRuntimeData> CharacterData;
    public NativeList<int> LowHPCharacterIDs; // Job에서 쓰기 위해 NativeList로

    public void Execute(int index)
    {
        if (CharacterData[index].CurrentHP < 100)
        {
            // Add는 내부적으로 Lock이 필요하거나, 병렬 쓰기를 위한 별도 처리가 필요.
            // 여기서는 예시를 위해 단순화. 실제로는 NativeQueue 등을 사용하는 것이 안전.
            LowHPCharacterIDs.Add(CharacterData[index].EntityID); 
        }
    }
}
```

제 경험상, GC 스파이크를 잡는 것이 체감 성능 향상에 가장 큰 영향을 주었습니다. 특히 턴제 게임처럼 특정 시점에 대규모 계산이 집중되는 경우, 이 '현자의 지혜'가 빛을 발하죠.

### 시니어 개발자의 의견: TRPG 규칙은 성능 최적화의 훌륭한 교보재다

TRPG 규칙 시스템을 코드로 구현하고 최적화하는 과정은 단순히 게임의 특정 기능을 만드는 것을 넘어섭니다. 이는 곧 '복잡한 시스템을 어떻게 설계하고, 관리하며, 고성능으로 작동시킬 것인가'에 대한 근본적인 고민과 맞닿아 있습니다.

TRPG 규칙은 다음과 같은 특성 때문에 유니티 최적화의 훌륭한 교보재가 됩니다:
*   **명확한 입출력**: 조건이 명확하고, 효과도 명확합니다. 이는 데이터 중심 설계를 적용하기 좋게 만듭니다.
*   **병렬화 가능성**: 많은 규칙 평가가 서로 독립적으로 이루어질 수 있어 Job System을 적용하기 좋습니다.
*   **예측 가능한 변화**: 규칙 정의는 한 번 만들어지면 잘 변하지 않으므로, 사전 처리(pre-computation)나 캐싱 전략을 적용하기 좋습니다.

결국, TRPG 규칙 시스템을 최적화한다는 것은 게임 내의 모든 복잡한 로직을 바라보는 시야를 넓히는 것과 같습니다. 여러분의 게임이 수많은 규칙과 상호작용으로 가득 찬 거대한 던전이라고 생각해 보세요. 우리는 그 던전의 효율적인 길을 만들고, 몬스터들이 제때 등장하고 사라지며, 보상이 매끄럽게 지급되도록 설계하는 던전 마스터이자 건축가인 셈이죠.

### 마무리하며

오늘 우리는 유니티에서 TRPG 규칙 시스템을 고성능으로 구현하기 위한 세 가지 핵심 전략, 즉 **데이터 중심 설계**, **Unity Job System & Burst Compiler 활용**, 그리고 **GC 할당 최소화**에 대해 깊이 있게 탐구해 보았습니다. 이 팁들이 여러분의 유니티 프로젝트에 작은 영감이라도 되었기를 바랍니다.

복잡한 시스템과의 씨름은 때론 고통스럽지만, 이를 효율적인 코드로 길들이는 과정에서 오는 성취감은 정말 짜릿합니다. 다음 포스팅에서는 또 다른 흥미로운 주제로 찾아올 테니, 그때까지 모두 즐거운 코딩 라이프 되시길! 궁금한 점이나 여러분의 경험담이 있다면 댓글로 공유해 주세요. 언제든 환영입니다!
